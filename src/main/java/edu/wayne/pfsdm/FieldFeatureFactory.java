package edu.wayne.pfsdm;

/**
 * Created by fsqcds on 5/1/15.
 */
public class FieldFeatureFactory {
    public edu.wayne.pfsdm.scala.FieldFeature getFieldFeature(String fieldFeatureName, ParametrizedFSDMTraversal traversal) {
        if (fieldFeatureName == null) {
            return null;
        }
        if (fieldFeatureName.equalsIgnoreCase("baselinetopscore")) {
            return new BaselineTopScoreFeatureField(traversal);
        }

        return null;
    }
}
