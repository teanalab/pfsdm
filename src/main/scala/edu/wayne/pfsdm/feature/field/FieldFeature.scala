package edu.wayne.pfsdm.feature.field

import edu.wayne.pfsdm.feature.FileBasedFeature
import org.lemurproject.galago.core.retrieval.Retrieval

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 5/1/15.
 */
trait FieldFeature {
  /**
   * Returns feature value for the field and tokens.
   *
   * @param tokens    terms for which feature is calculated
   * @param fieldName name of the field. This is ignored for features that doesn't depend on field
   * @return          feature value
   */
  def getPhi(tokens: Seq[String], fieldName: String, queryId: String): Double

  /**
   * Returns feature value for the field and tokens. This is a Java wrapper for getPhi(Seq[String], String, String).
   *
   * @param tokens    terms for which feature is calculated
   * @param fieldName name of the field. This is ignored for features that doesn't depend on field
   * @return          feature value
   */
  def getPhi(tokens: java.lang.Iterable[String], fieldName: String, queryId: String): Double = getPhi(tokens.toList, fieldName, queryId)
}

object FieldFeature {
  val FeaturesPath = """/(.+)""".r

  def apply(fieldFeatureName: String, retrieval: Retrieval): FieldFeature = fieldFeatureName match {
    case "baselinetopscore" => new BaselineTopScore(retrieval)
    case "fieldlikelihood" => new FieldLMTermLikelihood(retrieval)
    case "bigramcfratio" => new BigramCFRatio(retrieval)
    case "cf" => new NormalizedCF(retrieval)
    case FeaturesPath(path) => new FileBasedFeature(path)
  }
}