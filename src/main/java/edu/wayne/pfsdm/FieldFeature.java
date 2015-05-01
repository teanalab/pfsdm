package edu.wayne.pfsdm;

import org.lemurproject.galago.core.retrieval.Retrieval;

import java.util.List;

/**
 * Created by fsqcds on 5/1/15.
 */
public abstract class FieldFeature {
    protected ParametrizedFSDMTraversal traversal;
    protected String fieldName;
    public FieldFeature(String fieldName, ParametrizedFSDMTraversal traversal) {
        this.fieldName = fieldName;
        this.traversal = traversal;
    }
    public abstract double getPhi(List<String> tokens);
}
