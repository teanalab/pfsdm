package edu.wayne.pfsdm;

import nzhiltsov.fsdm.FieldedSequentialDependenceTraversal;
import org.apache.commons.lang.math.NumberUtils;
import static fj.data.List.list;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.util.TextPartAssigner;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For queries like "#pfsdm:uw.attributes.width=8:uw.width=4(president barack obama)"
 *
 * @author Nikita Zhiltsov
 * @author Fedor Nikolaev
 */
public class ParametrizedFSDMTraversal extends FieldedSequentialDependenceTraversal {

    protected final List<String> unigramFieldFeatureNames;
    protected final List<String> orderedFieldFeatureNames;
    protected final List<String> unorderedFieldFeatureNames;
    protected final Parameters unigramFieldFeatureWeights;
    protected final Parameters orderedFieldFeatureWeights;
    protected final Parameters unorderedFieldFeatureWeights;
    protected final Map<String, edu.wayne.pfsdm.scala.FieldFeature> unigramFieldFeatures = new HashMap<>();
    protected final Map<String, edu.wayne.pfsdm.scala.FieldFeature> orderedFieldFeatures = new HashMap<>();
    protected final Map<String, edu.wayne.pfsdm.scala.FieldFeature> unorderedFieldFeatures = new HashMap<>();

    public ParametrizedFSDMTraversal(Retrieval retrieval) {
        super(retrieval);
        if (globals.isList("fieldFeatures", String.class)) {
            this.unigramFieldFeatureNames = this.orderedFieldFeatureNames = this.unorderedFieldFeatureNames =
                    (List<String>) globals.getAsList("fieldFeatures");

            this.unigramFieldFeatureWeights = this.orderedFieldFeatureWeights = this.unorderedFieldFeatureWeights =
                    globals.getMap("fieldFeatureWeights");
        } else if (globals.isList("unigramFieldFeatures", String.class) &&
                globals.isList("orderedFieldFeatures", String.class) &&
                globals.isList("unorderedFieldFeatures", String.class)) {
            this.unigramFieldFeatureNames = (List<String>) globals.getAsList("unigramFieldFeatures");
            this.orderedFieldFeatureNames = (List<String>) globals.getAsList("orderedFieldFeatures");
            this.unorderedFieldFeatureNames = (List<String>) globals.getAsList("unorderedFieldFeatures");

            this.unigramFieldFeatureWeights = globals.getMap("unigramFieldFeatureWeights");
            this.orderedFieldFeatureWeights = globals.getMap("orderedFieldFeatureWeights");
            this.unorderedFieldFeatureWeights = globals.getMap("unorderedFieldFeatureWeights");
        } else {
            throw new IllegalArgumentException("MLMTraversal requires having 'fields' parameter initialized");
        }

        for (String unigramFieldFeatureName : unigramFieldFeatureNames) {
            unigramFieldFeatures.put(unigramFieldFeatureName, FieldFeatureFactory.getFeature(unigramFieldFeatureName));
        }
        for (String orderedFieldFeatureName : orderedFieldFeatureNames) {
            orderedFieldFeatures.put(orderedFieldFeatureName, FieldFeatureFactory.getFeature(orderedFieldFeatureName));
        }
        for (String unorderedFieldFeatureName : unorderedFieldFeatureNames) {
            unorderedFieldFeatures.put(unorderedFieldFeatures, FieldFeatureFactory.getFeature(unorderedFieldFeatureName));
        }
    }

    public List<String> getFields() {
        return fields;
    }

    public Retrieval getRetrieval() {
        return retrieval;
    }

    public Parameters getGlobals() {
        return globals;
    }

    @Override
    public Node afterNode(Node original, Parameters qp) throws Exception {
        if (original.getOperator().equals("pfsdm")) {
            return buildSDMNode(original, qp);
        } else {
            return original;
        }
    }

    private double getUnigramFeatureWeight(String term, String field) {
        return unigramFieldFeatures.get(field).getPhi(list(term), this.retrieval);
    }

    @Override
    protected Node getUnigramNode(Node original, Parameters queryParameters, String term) throws Exception {
        String scorerType = queryParameters.get("scorer", globals.get("scorer", "dirichlet"));

        ArrayList<Node> termFields = new ArrayList<Node>();
        NodeParameters nodeweights = new NodeParameters();
        int i = 0;
        double normalizer = 0.0;
        for (String field : fields) {
            Node termFieldCounts, termExtents;

            // if we have access to the correct field-part:
            if (this.retrieval.getAvailableParts().containsKey("field." + field)) {
                NodeParameters par1 = new NodeParameters();
                par1.set("default", term);
                par1.set("part", "field." + field);
                termFieldCounts = new Node("counts", par1, new ArrayList());
            } else {
                // otherwise use an #inside op
                NodeParameters par1 = new NodeParameters();
                par1.set("default", term);
                termExtents = new Node("extents", par1, new ArrayList());
                termExtents = TextPartAssigner.assignPart(termExtents, globals, this.retrieval.getAvailableParts());

                termFieldCounts = new Node("inside");
                termFieldCounts.addChild(StructuredQuery.parse("#extents:part=extents:" + field + "()"));
                termFieldCounts.addChild(termExtents);
            }

            double fieldWeight = 0.0;
            if (fieldWeights != null && fieldWeights.containsKey(UNIGRAM_FIELD_PREFIX + field)) {
                fieldWeight = fieldWeights.getDouble(UNIGRAM_FIELD_PREFIX + field);
            } else {
                fieldWeight = queryParameters.get(UNIGRAM_FIELD_PREFIX + field, 0.0);
            }
            nodeweights.set(Integer.toString(i), fieldWeight);
            normalizer += fieldWeight;

            Node termScore = new Node(scorerType);
            termScore.getNodeParameters().set("lengths", field);
            termScore.addChild(fieldStats.fieldLenNodes.get(field).clone());
            termScore.addChild(termFieldCounts);
            termFields.add(termScore);
            i++;
        }
        // normalize field weights
        if (normalizer != 0) {
            for (i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                nodeweights.set(key, nodeweights.getDouble(key) / normalizer);
            }
        }

        return new Node("wsum", nodeweights, termFields);
    }

    protected BigramNodes getBigramNodes(Node original, Parameters qp, List<Node> seq) throws Exception {
        NodeParameters np = original.getNodeParameters();

        NodeParameters orderedFieldWeights = new NodeParameters();
        double odNormalizer = 0.0;
        NodeParameters unwindowFieldWeights = new NodeParameters();
        double uwwNormalizer = 0.0;
        for (int i = 0; i < fields.size(); i++) {
            double odFieldWeight = 0.0;
            double uwdFieldWeight = 0.0;
            if (this.fieldWeights != null && this.fieldWeights.containsKey(ORDERED_FIELD_PREFIX + fields.get(i))) {
                odFieldWeight = this.fieldWeights.getDouble(ORDERED_FIELD_PREFIX + fields.get(i));
            } else {
                odFieldWeight = qp.get(ORDERED_FIELD_PREFIX + fields.get(i), 0.0);
            }
            if (this.fieldWeights != null && this.fieldWeights.containsKey(UNWINDOW_FIELD_PREFIX + fields.get(i))) {
                uwdFieldWeight = this.fieldWeights.getDouble(UNWINDOW_FIELD_PREFIX + fields.get(i));
            } else {
                uwdFieldWeight = qp.get(UNWINDOW_FIELD_PREFIX + fields.get(i), 0.0);
            }
            orderedFieldWeights.set(Integer.toString(i), odFieldWeight);
            odNormalizer += odFieldWeight;
            unwindowFieldWeights.set(Integer.toString(i), uwdFieldWeight);
            uwwNormalizer += uwdFieldWeight;
        }
        // normalize field weights
        if (odNormalizer != 0) {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                orderedFieldWeights.set(key, orderedFieldWeights.getDouble(key) / odNormalizer);
            }
        }
        if (uwwNormalizer != 0) {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                unwindowFieldWeights.set(key, unwindowFieldWeights.getDouble(key) / uwwNormalizer);
            }
        }

        String scorerType = qp.get("scorer", globals.get("scorer", "dirichlet"));
        List<Node> orderedBigramFields = new ArrayList<Node>();
        List<Node> unorderedBigramFields = new ArrayList<Node>();
        for (String field : fields) {
            Node orderedOperationNode = new Node(odOp, new NodeParameters(np.get("od.width", odWidth)));
            long unorderedWindow = np.get(("uw." + field + ".width"), np.get("uw.width", uwWidth));
            Node unorderedOperationNode = new Node(uwOp, new NodeParameters(unorderedWindow));
            for (Node t : seq) {
                String inFieldTerm = t.getNodeParameters().getAsSimpleString("default");
                if (NumberUtils.isNumber(inFieldTerm)) inFieldTerm = "@/" + inFieldTerm + "/";
                orderedOperationNode.addChild(StructuredQuery.parse("#extents:" + inFieldTerm + ":part=field." + field + "()"));
                unorderedOperationNode.addChild(StructuredQuery.parse("#extents:" + inFieldTerm + ":part=field." + field + "()"));
            }
            Node orderedBigramScore = new Node(scorerType);
            orderedBigramScore.getNodeParameters().set("lengths", field);
            orderedBigramScore.addChild(fieldStats.getFieldLenNodes().get(field).clone());
            orderedBigramScore.addChild(orderedOperationNode);
            orderedBigramFields.add(orderedBigramScore);

            Node unorderedBigramScore = new Node(scorerType);
            unorderedBigramScore.getNodeParameters().set("lengths", field);
            unorderedBigramScore.addChild(fieldStats.getFieldLenNodes().get(field).clone());
            unorderedBigramScore.addChild(unorderedOperationNode);
            unorderedBigramFields.add(unorderedBigramScore);
        }

        Node orderedNode = new Node("wsum", orderedFieldWeights, orderedBigramFields);
        Node unorderedNode = new Node("wsum", unwindowFieldWeights, unorderedBigramFields);
        return new BigramNodes(orderedNode, unorderedNode);
    }
}
