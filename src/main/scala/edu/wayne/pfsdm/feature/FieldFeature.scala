package edu.wayne.pfsdm.feature

import org.lemurproject.galago.core.retrieval.Retrieval

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 5/1/15.
 */
trait FieldFeature {
  /**
   * Returns feature value for field and tokens.
   *
   * @param tokens    terms for which feature is calculated
   * @param fieldName name of the field. This is ignored for features that doesn't depend on field
   * @return          feature value
   */
  def getPhi(tokens: Seq[String], fieldName: String): Double

  /**
   * Returns feature value for field and tokens. This is Java wrapper for getPhi(Seq[String], String).
   *
   * @param tokens    terms for which feature is calculated
   * @param fieldName name of the field. This is ignored for features that doesn't depend on field
   * @return          feature value
   */
  def getPhi(tokens: java.lang.Iterable[String], fieldName: String): Double = getPhi(tokens.toList, fieldName: String)
}

object FieldFeature {
  def apply(fieldFeatureName: String, retrieval: Retrieval) = fieldFeatureName match {
    case "baselinetopscore" => new BaselineTopScoreFieldFeature(retrieval)
    case "capitalized" => new CapitalizedFeature
    case "fieldlikelihood" => new FieldLMTermLikelihoodFeature(retrieval)
    case "plural" => new PluralFeature
  }
}