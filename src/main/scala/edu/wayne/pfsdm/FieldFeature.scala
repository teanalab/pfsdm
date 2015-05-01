package edu.wayne.pfsdm

/**
 * Created by fsqcds on 5/1/15.
 */
trait FieldFeature {
  val fieldName: String
  val traversal: ParametrizedFSDMTraversal

  def getPhi(tokens: Seq[String]): Double
}

object FieldFeature {
  def apply(fieldFeatureName: String, traversal: ParametrizedFSDMTraversal) = fieldFeatureName match {
    case "baselinetopscore" => new BaselineTopScoreFieldFeature(fieldFeatureName, traversal)
  }
}