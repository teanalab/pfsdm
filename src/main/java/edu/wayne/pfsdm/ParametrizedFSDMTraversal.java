package edu.wayne.pfsdm;

import fj.P;
import fj.data.HashMap;
import fj.data.List;
import nzhiltsov.fsdm.FieldedSequentialDependenceTraversal;
import org.apache.commons.lang.math.NumberUtils;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.util.TextPartAssigner;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;

import static fj.data.List.list;

/**
 * For queries like "#pfsdm:uw.attributes.width=8:uw.width=4(president barack obama)"
 *
 * @author Nikita Zhiltsov
 * @author Fedor Nikolaev
 */
public class ParametrizedFSDMTraversal extends FieldedSequentialDependenceTraversal {

    protected final List<String> fieldFeatureNames;
    protected final Parameters fieldFeatureWeights;
    protected final HashMap<String, FieldFeature> fieldFeatures;

    private FieldFeature constructFeature(String featureName) {
        return FieldFeature$.MODULE$.apply(featureName, this);
    }

    public ParametrizedFSDMTraversal(Retrieval retrieval) {
        super(retrieval);
        if (globals.isList("fieldFeatures", String.class)) {
            this.fieldFeatureNames = list(globals.getAsList("fieldFeatures", String.class));
        } else {
            throw new IllegalArgumentException("MLMTraversal requires having 'fields' parameter initialized");
        }
        this.fieldFeatureWeights = globals.getMap("fieldFeatureWeights");

        fieldFeatures = HashMap.from(fieldFeatureNames.map(featureName -> P.p(featureName, constructFeature(featureName))));
    }

    public java.util.List<String> getFields() {
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

    private double getFeatureWeight(String fieldName, String featureName, Parameters queryParameters) {
        if (fieldFeatureWeights != null && fieldFeatureWeights.containsKey(fieldName + "-" + featureName)) {
            return fieldFeatureWeights.getDouble(fieldName + "-" + featureName);
        } else {
            return queryParameters.get(fieldName + "-" + featureName, 0.0);
        }
    }

    private double getFeatureValue(String featureName, Iterable<String> terms, String fieldName) {
        return fieldFeatures.get(featureName).some().getPhi(terms, fieldName);
    }

    protected double getFieldWeight(Iterable<String> terms, String fieldName, Parameters queryParameters) {
        double fieldWeight = 0.0;
        for (String featureName : fieldFeatureNames) {
            fieldWeight += getFeatureWeight(fieldName, featureName, queryParameters) * getFeatureValue(featureName, terms, fieldName);
        }
        return fieldWeight;
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

            double fieldWeight = getFieldWeight(list(term), field, queryParameters);
            nodeweights.set(Integer.toString(i), fieldWeight);
            normalizer += fieldWeight;

            Node termScore = new Node(scorerType);
            termScore.getNodeParameters().set("lengths", field);
            termScore.addChild(fieldStats.getFieldLenNodes().get(field).clone());
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

    protected BigramNodes getBigramNodes(Node original, Parameters qp, java.util.List<Node> seq) throws Exception {
        NodeParameters np = original.getNodeParameters();

        NodeParameters fieldWeights = new NodeParameters();
        List<String> terms = list(seq).map(Node::getDefaultParameter);
        double normalizer = 0.0;
        for (int i = 0; i < fields.size(); i++) {
            double fieldWeight = getFieldWeight(terms, fields.get(i), qp);
            normalizer += fieldWeight;
        }
        // normalize field weights
        if (normalizer != 0) {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                fieldWeights.set(key, fieldWeights.getDouble(key) / normalizer);
            }
        }

        String scorerType = qp.get("scorer", globals.get("scorer", "dirichlet"));
        java.util.List<Node> orderedBigramFields = new ArrayList<Node>();
        java.util.List<Node> unorderedBigramFields = new ArrayList<Node>();
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

        Node orderedNode = new Node("wsum", fieldWeights, orderedBigramFields);
        Node unorderedNode = new Node("wsum", fieldWeights, unorderedBigramFields);
        return new BigramNodes(orderedNode, unorderedNode);
    }
}
