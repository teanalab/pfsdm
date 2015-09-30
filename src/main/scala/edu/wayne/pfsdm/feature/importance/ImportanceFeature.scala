package edu.wayne.pfsdm.feature.importance

import edu.wayne.pfsdm.feature.FileBasedFeature
import org.lemurproject.galago.core.retrieval.Retrieval

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 5/1/15.
 */
trait ImportanceFeature {
  /**
   * Returns feature value for the tokens.
   *
   * @param tokens    terms for which feature is calculated
   * @return          feature value
   */
  def getPhi(tokens: Seq[String], queryId: String): Double

  /**
   * Returns feature value for the tokens. This is a Java wrapper for getPhi(Seq[String], String).
   *
   * @param tokens    terms for which feature is calculated
   * @return          feature value
   */
  def getPhi(tokens: java.lang.Iterable[String], queryId: String): Double = getPhi(tokens.toList, queryId)
}

object ImportanceFeature {
  val FeaturesPath = """/(.+)""".r

  def apply(fieldFeatureName: String, retrieval: Retrieval): ImportanceFeature = fieldFeatureName match {
    case "collectiontf" => new CollectionTF(retrieval)
    case "collectionitf" => new CollectionInverseTF(retrieval)
    case FeaturesPath(path) => new FileBasedFeature(path)
  }
}