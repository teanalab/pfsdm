package edu.wayne.pfsdm;

import edu.wayne.pfsdm.feature.importance.ImportanceFeature;
import edu.wayne.pfsdm.feature.importance.ImportanceFeature$;
import fj.P;
import fj.data.HashMap;
import fj.data.List;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.utility.Parameters;

import static fj.data.List.list;

/**
 * Created by fsqcds on 8/18/15.
 */
public class WPFSDMTraversal extends ParametrizedFSDMTraversal {
    protected final List<String> importanceFeatureNames;
    protected final Parameters importanceFeatureWeights;
    protected final HashMap<String, ImportanceFeature> importanceFeatures;

    private ImportanceFeature constructImportanceFeature(String featureName) {
        return ImportanceFeature$.MODULE$.apply(featureName, retrieval);
    }

    public WPFSDMTraversal(Retrieval retrieval) {
        super(retrieval);
        if (globals.isList("importanceFeatures", String.class)) {
            this.importanceFeatureNames = list(globals.getAsList("importanceFeatures", String.class));
        } else {
            throw new IllegalArgumentException("WPFSDMTraversal requires having 'importanceFeatures' parameter initialized");
        }
        this.importanceFeatureWeights = globals;

        importanceFeatures = HashMap.from(importanceFeatureNames.map(featureName -> P.p(featureName, constructImportanceFeature(featureName))));
        logger.info("Done initializing WPFSDMTraversal");

    }

    @Override
    public Node afterNode(Node original, Parameters qp) throws Exception {
        if (original.getOperator().equals("wpfsdm")) {
            return buildSDMNode(original, qp);
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
            }
        }
        logger.info(String.format("%s\t%s\t%s\t%g", queryParameters.getString("number"), String.join(" ", terms), "lambda for " + depType, labmda));
        return labmda;
    }

    @Override
    protected double computeWeight(String term, NodeParameters np, Parameters qp) throws Exception {
        return getLabmda(UNIGRAM_FIELD_PREFIX, list(term), qp);
    }

    @Override
    protected double computeWeight(java.util.List<Node> bigram, NodeParameters np, Parameters qp, boolean isOrdered) throws Exception {
        List<String> terms = list(bigram).map(Node::getDefaultParameter);
        if (isOrdered) {
            return getLabmda(ORDERED_FIELD_PREFIX, terms, qp);
        } else {
            return getLabmda(UNWINDOW_FIELD_PREFIX, terms, qp);
        }
    }
}
