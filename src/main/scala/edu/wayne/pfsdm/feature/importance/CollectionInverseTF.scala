package edu.wayne.pfsdm.feature.importance

import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}

import scala.math.log

/**
 * Created by fsqcds on 5/1/15.
 */
class CollectionInverseTF(val retrieval: Retrieval) extends MemoizedImportanceFeature {
  val fields = retrieval.getGlobalParameters.getAsList("fields", classOf[String])

  private def getTermFrequency(tokens: Seq[String]): Long = {
    val node: Node = (tokens.toList: @unchecked) match {
      case term :: Nil =>
        new Node("counts", term)
      case term1 :: term2 :: Nil =>
        val t1: Node = new Node("extents", term1)
        val t2: Node = new Node("extents", term2)
        val od1: Node = new Node("ordered")
        od1.getNodeParameters.set("default", 1)
        od1.addChild(t1)
        od1.addChild(t2)
        od1
    }
    retrieval.getNodeStatistics(node).nodeFrequency
  }


  private def getCollectionLength(fieldName: String): Long = {
    val fieldLen: Node = StructuredQuery.parse("#lengths:" + fieldName + ":part=lengths()")
    retrieval.getCollectionStatistics(fieldLen).collectionLength
  }

  override def getNewPhi(tokens: Seq[String]): Double = {
    val tf = getTermFrequency(tokens).toDouble
    if (tf == 0)
      log(tf)
    else
      -log(tf)
  }
}
