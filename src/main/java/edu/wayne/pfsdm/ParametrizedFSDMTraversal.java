package edu.wayne.pfsdm;

import edu.wayne.pfsdm.feature.field.FieldFeature;
import edu.wayne.pfsdm.feature.field.FieldFeature$;
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
import java.util.logging.Logger;

import static fj.data.List.list;

/**
 * For queries like "#pfsdm(president barack obama)"
 *
 * @author Fedor Nikolaev
 */
public class ParametrizedFSDMTraversal extends FieldedSequentialDependenceTraversal {

    public static final String BIGRAM_FIELD_PREFIX = "bi-";

    protected final List<String> fieldFeatureNames;
    protected final Parameters fieldFeatureWeights;
    protected final HashMap<String, FieldFeature> fieldFeatures;
    protected boolean featuresScaling = false;
    private HashMap<String, Double> featureMin = HashMap.hashMap();
    private HashMap<String, Double> featureRange = HashMap.hashMap();

    protected final Logger logger;

    private FieldFeature constructFeature(String featureName) {
        return FieldFeature$.MODULE$.apply(featureName, retrieval);
    }

    public ParametrizedFSDMTraversal(Retrieval retrieval) {
        super(retrieval);
        logger = Logger.getLogger(this.getClass().getName());
        logger.info("Initializing ParametrizedFSDMTraversal");
        if (globals.isList("fieldFeatures", String.class)) {
            this.fieldFeatureNames = list(globals.getAsList("fieldFeatures", String.class));
        } else {
            throw new IllegalArgumentException("ParametrizedFSDMTraversal requires having 'fieldFeatures' parameter initialized");
        }
        this.fieldFeatureWeights = globals;

        if (globals.isList("featuresScaling", Parameters.class)) {
            logger.info("Initializing featuresScaling");
            java.util.List<Parameters> scales = globals.getList("featuresScaling", Parameters.class);
            for (Parameters param : scales) {
                String name = param.getString("name");
                double max, min;
                if (param.isDouble("max")) {
                    max = param.getDouble("max");
                } else {
                    logger.info(String.format("Param %s max value not specified using 1.0", name));
                    max = 1.0;
                }
                if (param.isDouble("min")) {
                    min = param.getDouble("min");
                } else {
                    logger.info(String.format("Param %s min value not specified using 0.0", name));
                    min = 0.0;
                }
                assert max >= min : "Range of parameter " + name + " is zero or negative.";
                featureMin.set(name, min);
                featureRange.set(name, max - min);
            }
            featuresScaling = true;
        }

        fieldFeatures = HashMap.from(fieldFeatureNames.map(featureName -> P.p(featureName, constructFeature(featureName))));
        logger.info("Done initializing ParametrizedFSDMTraversal");
    }

    @Override
    public Node afterNode(Node original, Parameters qp) throws Exception {
        if (original.getOperator().equals("pfsdm")) {
            return buildSDMNode(original, qp);
        } else {
            return original;
        }
    }

    private double getFeatureWeight(String depType, String fieldName, String featureName, Parameters queryParameters) {
        String paramName = depType + fieldName + "-" + featureName;
        if (fieldFeatureWeights != null && fieldFeatureWeights.containsKey(paramName)) {
            return fieldFeatureWeights.getDouble(paramName);
        } else {
            return queryParameters.get(paramName, 0.0);
        }
    }

    protected double scaleFeatureValue(String featureName, double phi) {
        if (featuresScaling) {
            if (phi == Double.NEGATIVE_INFINITY) {
                return 0.0;
            } else {
                return (phi - featureMin.get(featureName).some()) / featureRange.get(featureName).some();
            }
        } else {
            return phi;
        }
    }

    private double getScaledFeatureValue(String featureName, Iterable<String> terms, String fieldName, String queryId) {
        double phi = fieldFeatures.get(featureName).some().getPhi(terms, fieldName, queryId);
        double scaledPhi = scaleFeatureValue(featureName, phi);
        assert scaledPhi >= 0 : scaledPhi;
        return scaledPhi;
    }

    protected double getFieldWeight(String depType, Iterable<String> terms, String fieldName, Parameters queryParameters) {
        if (terms == null || list(terms).exists(term -> term == null)) {
            System.out.println(queryParameters.getString("number") + " " + queryParameters.getString("text"));
            throw new IllegalArgumentException("terms shouldn't be null");
        }
        double fieldWeight = 0.0;
        for (String featureName : fieldFeatureNames) {
            double featureWeight = getFeatureWeight(depType, fieldName, featureName, queryParameters);
            if (featureWeight != 0.0) {
                fieldWeight += featureWeight * getScaledFeatureValue(featureName, terms, fieldName, queryParameters.getString("number"));
            }
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
                termFieldCounts = new Node("counts", par1, new ArrayList<>());
            } else {
                // otherwise use an #inside op
                NodeParameters par1 = new NodeParameters();
                par1.set("default", term);
                termExtents = new Node("extents", par1, new ArrayList<>());
                termExtents = TextPartAssigner.assignPart(termExtents, globals, this.retrieval.getAvailableParts());

                termFieldCounts = new Node("inside");
                termFieldCounts.addChild(StructuredQuery.parse("#extents:part=extents:" + field + "()"));
                termFieldCounts.addChild(termExtents);
            }

            double fieldWeight = getFieldWeight(UNIGRAM_FIELD_PREFIX, list(term), field, queryParameters);
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
                double normalizedWeight = nodeweights.getDouble(key) / normalizer;
                logger.info(String.format("%s\t%s\t%s\t%g", queryParameters.getString("number"), term, fields.get(i), normalizedWeight));
                nodeweights.set(key, normalizedWeight);
            }
        } else {
            for (i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                double normalizedWeight = 1.0 / fields.size();
                logger.info(String.format("%s\t%s\t%s\t%g", queryParameters.getString("number"), term, fields.get(i), normalizedWeight));
                nodeweights.set(key, normalizedWeight);
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
            double fieldWeight = getFieldWeight(BIGRAM_FIELD_PREFIX, terms, fields.get(i), qp);
            fieldWeights.set(Integer.toString(i), fieldWeight);
            normalizer += fieldWeight;
        }
        // normalize field weights
        if (normalizer != 0) {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                double normalizedWeight = fieldWeights.getDouble(key) / normalizer;
                logger.info(String.format("%s\t%s\t%s\t%g", qp.getString("number"), String.join(" ", terms), fields.get(i), normalizedWeight));
                fieldWeights.set(key, normalizedWeight);
            }
        } else {
            for (int i = 0; i < fields.size(); i++) {
                String key = Integer.toString(i);
                double normalizedWeight = 1 / fields.size();
                logger.info(String.format("%s\t%s\t%s\t%g", qp.getString("number"), String.join(" ", terms), fields.get(i), normalizedWeight));
                fieldWeights.set(key, normalizedWeight);
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
