package edu.wayne.pfsdm;

import nzhiltsov.fsdm.MLMTraversal;
import org.lemurproject.galago.utility.Parameters;

import java.util.List;

/**
 * Created by fsqcds on 5/1/15.
 */
public class BaselineTopScoreFeatureField extends edu.wayne.pfsdm.scala.FieldFeature {
    public BaselineTopScoreFeatureField(String fieldName, ParametrizedFSDMTraversal traversal) {
        super(fieldName, traversal);
    }

    @Override
    public double getPhi(List<String> tokens) {
        if (tokens.size() == 1) {
            String unigram = tokens.get(0);
            Parameters fieldWeights = Parameters.create();
            List<String> fields = traversal.getFields();
            for (String weightedField : fields) {
                fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField,
                        (weightedField == fieldName ? 1.0 : 0.0));
            }

        }
    }
}
