package edu.wayne.pfsdm.feature

import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}

/**
 * Created by fsqcds on 5/1/15.
 */
class FieldLMTermLikelihoodFeature(val retrieval: Retrieval) extends FieldFeature {
  var memo = Map[(Seq[String], String), Double]()
  val scale = 100

  private def getTermFrequency(tokens: Seq[String], fieldName: String): Long = {
    val node: Node = (tokens.toList: @unchecked) match {
      case term :: Nil =>
        val node: Node = new Node("counts", term)
        node.getNodeParameters.set("part", "field." + fieldName)
        node
      case term1 :: term2 :: Nil =>
        val t1: Node = new Node("extents", term1)
        val t2: Node = new Node("extents", term2)
        val od1: Node = new Node("ordered")
        od1.getNodeParameters.set("default", 1)
        od1.addChild(t1)
        od1.addChild(t2)
        od1.getChild(0).getNodeParameters.set("part", "field." + fieldName)
        od1.getChild(1).getNodeParameters.set("part", "field." + fieldName)
        od1
    }
    retrieval.getNodeStatistics(node).nodeFrequency
  }

  private def getFieldLength(fieldName: String): Long = {
    val fieldLen: Node = StructuredQuery.parse("#lengths:" + fieldName + ":part=lengths()")
    retrieval.getCollectionStatistics(fieldLen).collectionLength
  }

  override def getPhi(tokens: Seq[String], fieldName: String): Double = {
    memo.get((tokens, fieldName)) match {
      case Some(phi) => phi
      case None =>
        val phi = getTermFrequency(tokens, fieldName).toDouble / getFieldLength(fieldName) * scale
        memo += (tokens, fieldName) -> phi
        phi
    }
  }
}
