package edu.wayne.pfsdm.feature.importance

import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}
import org.lemurproject.galago.core.util.TextPartAssigner

import scala.math.log

/**
 * Created by fsqcds on 5/1/15.
 */
class CollectionInverseTF(val retrieval: Retrieval) extends MemoizedImportanceFeature {
  val fields = retrieval.getGlobalParameters.getAsList("fields", classOf[String])

  private def getTermFrequency(tokens: Seq[String]): Long = {
    val node: Node = (tokens.toList: @unchecked) match {
      case term :: Nil =>
        var t: Node = new Node("counts", term)
        t = TextPartAssigner.assignPart(t, retrieval.getGlobalParameters, retrieval.getAvailableParts)
        t
      case term1 :: term2 :: Nil =>
        var t1: Node = new Node("extents", term1)
        t1 = TextPartAssigner.assignPart(t1, retrieval.getGlobalParameters, retrieval.getAvailableParts)
        var t2: Node = new Node("extents", term2)
        t2 = TextPartAssigner.assignPart(t2, retrieval.getGlobalParameters, retrieval.getAvailableParts)
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
