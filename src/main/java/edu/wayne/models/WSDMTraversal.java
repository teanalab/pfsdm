package edu.wayne.models;

import edu.wayne.pfsdm.feature.importance.ImportanceFeature;
import edu.wayne.pfsdm.feature.importance.ImportanceFeature$;
import fj.P;
import org.lemurproject.galago.core.retrieval.GroupRetrieval;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.MalformedQueryException;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.traversal.Traversal;
import org.lemurproject.galago.utility.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static fj.data.List.list;

/**
 * Created by fsqcds on 8/18/15.
 */
public class WSDMTraversal extends Traversal {
    public static final String UNIGRAM_FIELD_PREFIX = "uni-";
    public static final String BIGRAM_FIELD_PREFIX = "bi-";

    private static final Logger logger = Logger.getLogger("WSDM");
    private Retrieval retrieval;
    private GroupRetrieval gRetrieval;
    private Parameters globalParams;
    private boolean verbose;

    protected final fj.data.List<String> importanceFeatureNames;
    protected final Parameters importanceFeatureWeights;
    protected final fj.data.HashMap<String, ImportanceFeature> importanceFeatures;

    protected boolean featuresScaling = false;
    private fj.data.HashMap<String, Double> featureMin = fj.data.HashMap.hashMap();
    private fj.data.HashMap<String, Double> featureRange = fj.data.HashMap.hashMap();


    private ImportanceFeature constructImportanceFeature(String featureName) {
        return ImportanceFeature$.MODULE$.apply(featureName, retrieval);
    }

    public WSDMTraversal(Retrieval retrieval) throws Exception {
        if (retrieval instanceof GroupRetrieval) {
            gRetrieval = (GroupRetrieval) retrieval;
        }
        this.retrieval = retrieval;

        this.globalParams = retrieval.getGlobalParameters();

        verbose = globalParams.get("verboseWSDM", false);

        if (globalParams.isList("importanceFeatures", String.class)) {
            this.importanceFeatureNames = list(globalParams.getAsList("importanceFeatures", String.class));
        } else {
            throw new IllegalArgumentException("WPFSDMTraversal requires having 'importanceFeatures' parameter initialized");
        }

        this.importanceFeatureWeights = globalParams;
        importanceFeatures = fj.data.HashMap.from(importanceFeatureNames.map(featureName -> P.p(featureName, constructImportanceFeature(featureName))));

        if (globalParams.isList("featuresScaling", Parameters.class)) {
            logger.info("Initializing featuresScaling");
            java.util.List<Parameters> scales = globalParams.getList("featuresScaling", Parameters.class);
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
    }

    @Override
    public void beforeNode(Node original, Parameters queryParameters) throws Exception {
    }

    @Override
    public Node afterNode(Node original, Parameters queryParams) throws Exception {
        if (original.getOperator().equals("mywsdm")) {

            NodeParameters np = original.getNodeParameters();

            // First check format - should only contain text node children
            List<Node> children = original.getInternalNodes();
            for (Node child : children) {
                if (child.getOperator().equals("text") == false) {
                    throw new MalformedQueryException("wsdm operator requires text-only children");
                }
            }

            // formatting is ok - now reassemble
            ArrayList<Node> newChildren = new ArrayList();
            NodeParameters newWeights = new NodeParameters();

            for (Node child : children) {
                String term = child.getDefaultParameter();

                double weight = computeWeight(term, np, queryParams);
                newWeights.set(Integer.toString(newChildren.size()), weight);
                newChildren.add(child.clone());
            }

//            if (!biFeatures.isEmpty()) {
            for (int i = 0; i < (children.size() - 1); i++) {
                ArrayList<Node> pair = new ArrayList();
                pair.add(new Node("extents", children.get(i).getDefaultParameter()));
                pair.add(new Node("extents", children.get(i + 1).getDefaultParameter()));

                double weight = computeWeight(pair.get(0).getDefaultParameter(), pair.get(1).getDefaultParameter(), np, queryParams);

                newWeights.set(Integer.toString(newChildren.size()), weight);
                newChildren.add(new Node("od", new NodeParameters(1), Node.cloneNodeList(pair)));

                newWeights.set(Integer.toString(newChildren.size()), weight);
                newChildren.add(new Node("uw", new NodeParameters(8), Node.cloneNodeList(pair)));
            }
//            }


            Node wsdm = new Node("combine", newWeights, newChildren, original.getPosition());

            if (verbose) {
                System.err.println(wsdm.toPrettyString());
            }

            return wsdm;
        } else {
            return original;
        }
    }

    private double getImportanceFeatureWeight(String depType, String featureName, Parameters queryParameters) {
        String paramName = depType + featureName;
        if (importanceFeatureWeights != null && importanceFeatureWeights.containsKey(paramName)) {
            return importanceFeatureWeights.getDouble(paramName);
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

    private double getScaledImportanceFeatureValue(String featureName, Iterable<String> terms, String queryId) {
        double phi = importanceFeatures.get(featureName).some().getPhi(terms, queryId);
        double scaledPhi = scaleFeatureValue(featureName, phi);
        assert scaledPhi >= 0 : scaledPhi;
        return scaledPhi;
    }

    protected double getLabmda(String depType, Iterable<String> terms, Parameters queryParameters) {
        if (terms == null || list(terms).exists(term -> term == null)) {
            System.out.println(queryParameters.getString("number") + " " + queryParameters.getString("text"));
            throw new IllegalArgumentException("terms shouldn't be null");
        }
        double labmda = 0.0;
        for (String featureName : importanceFeatureNames) {
            double featureWeight = getImportanceFeatureWeight(depType, featureName, queryParameters);
            if (featureWeight != 0.0) {
                labmda += featureWeight * getScaledImportanceFeatureValue(featureName, terms, queryParameters.getString("number"));
                if (verbose) {
                    logger.info(String.format("%s -- feature:%s:%g * %g = %g", String.join(", ", terms), featureName, featureWeight, getScaledImportanceFeatureValue(featureName, terms, queryParameters.getString("number")), featureWeight * getScaledImportanceFeatureValue(featureName, terms, queryParameters.getString("number"))));
                }

            }
        }
        logger.info(String.format("%s\t%s\t%s\t%g", queryParameters.getString("number"), String.join(" ", terms), "lambda for " + depType, labmda));
        return labmda;
    }

    protected double computeWeight(String term, NodeParameters np, Parameters qp) throws Exception {
        return getLabmda(UNIGRAM_FIELD_PREFIX, list(term), qp);
    }

    protected double computeWeight(String term1, String term2, NodeParameters np, Parameters qp) throws Exception {
        fj.data.List<String> terms = list(term1, term2);
        return getLabmda(BIGRAM_FIELD_PREFIX, terms, qp);
    }
}
