package edu.wayne.pfsdm.feature

import edu.wayne.pfsdm.feature.field.FieldFeature
import edu.wayne.pfsdm.feature.importance.ImportanceFeature

import scala.io.Source

/**
 * Created by fsqcds on 7/3/15.
 */
class FileBasedFeature(val featureFileName: String) extends FieldFeature with ImportanceFeature {
  val fileBasedFeaturesDir = "/file-based-features/"
  val fileBasedFeaturesExt = ".tsv"

  val qIdGramToFeatureValue: Map[(String, Seq[String]), Double] =
    Source.fromURL(getClass.getResource(fileBasedFeaturesDir + featureFileName + fileBasedFeaturesExt)).getLines().
      map(_.split("\t")).map(t => (t(0), t(1).split(" ").toSeq) -> t(2).toDouble).toMap

  override def getPhi(tokens: Seq[String], fieldName: String, queryId: String): Double = {
    getPhi(tokens, queryId)
  }

  override def getPhi(tokens: Seq[String], queryId: String): Double = {
    qIdGramToFeatureValue.getOrElse((queryId, tokens), Double.NegativeInfinity)
  }
}
