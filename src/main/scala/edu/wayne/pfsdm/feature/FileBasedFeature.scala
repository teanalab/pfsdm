package edu.wayne.pfsdm.feature

import scala.io.Source

/**
 * Created by fsqcds on 7/3/15.
 */
class FileBasedFeature(val featureFileName: String) extends FieldFeature {
  val fileBasedFeaturesDir = "/file-based-features/"

  val qIdGramToFeatureValue: Map[(String, Seq[String]), Double] =
    Source.fromURL(getClass.getResource(fileBasedFeaturesDir + featureFileName)).getLines().map(_.split("\t")).
      map(t => (t(0), t(1).split(" ").toSeq) -> t(2).toDouble).toMap

  override def getPhi(tokens: Seq[String], fieldName: String, queryId: String): Double = {
    qIdGramToFeatureValue(queryId, tokens)
  }
}
