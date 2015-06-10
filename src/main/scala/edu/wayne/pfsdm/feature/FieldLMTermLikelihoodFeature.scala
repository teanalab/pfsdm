package edu.wayne.pfsdm.feature

import edu.wayne.pfsdm.{FieldFeature, ParametrizedFSDMTraversal}
import org.lemurproject.galago.core.index.stats.FieldStatistics
import org.lemurproject.galago.core.retrieval.query.{StructuredQuery, Node}

/**
 * Created by fsqcds on 5/1/15.
 */
class FieldLMTermLikelihoodFeature(val traversal: ParametrizedFSDMTraversal) extends FieldFeature {
  var memo = Map[(Seq[String], String), Double]()

  private def getTermFrequency(tokens: Seq[String], fieldName: String): Long = {
    val node: Node = tokens match {
      case term :: Nil => new Node("counts", term)
      case term1 :: term2 :: Nil => new Node("counts", term1 + "~" + term2)
    }
    node.getNodeParameters.set("part", "field." + fieldName)
    traversal.getRetrieval.getNodeStatistics(node).nodeFrequency
  }

  private def getFieldLength(fieldName: String): Long = {
    val fieldLen: Node = StructuredQuery.parse("#lengths:" + fieldName + ":part=lengths()")
    traversal.getRetrieval.getCollectionStatistics(fieldLen).collectionLength
  }

  override def getPhi(tokens: Seq[String], fieldName: String): Double = {
    memo.get((tokens, fieldName)) match {
      case Some(phi) => phi
      case None =>
        val phi = getTermFrequency(tokens, fieldName).toDouble / getFieldLength(fieldName)
        memo += (tokens, fieldName) -> phi
        phi
    }
  }
}
