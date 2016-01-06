package edu.wayne.pfsdm.feature.field

import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}

import scala.collection.JavaConversions._
import scala.math.log

/**
 * Created by fsqcds on 5/1/15.
 */
class FieldLMTermLikelihood(val retrieval: Retrieval) extends MemoizedFieldFeature {
  val fields = retrieval.getGlobalParameters.getAsList("fields", classOf[String])

  private def getTermFieldFrequency(tokens: Seq[String], fieldName: String): Long = {
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
      case term1 :: term2 :: term3 :: Nil =>
        val t1: Node = new Node("extents", term1)
        val t2: Node = new Node("extents", term2)
        val t3: Node = new Node("extents", term3)
        val od1: Node = new Node("ordered")
        od1.getNodeParameters.set("default", 1)
        od1.addChild(t1)
        od1.addChild(t2)
        od1.addChild(t3)
        od1.getChild(0).getNodeParameters.set("part", "field." + fieldName)
        od1.getChild(1).getNodeParameters.set("part", "field." + fieldName)
        od1.getChild(2).getNodeParameters.set("part", "field." + fieldName)
        od1

    }
    retrieval.getNodeStatistics(node).nodeFrequency
  }

  private def getDivider(tokens: Seq[String]): Long = {
    val Freqs: List[Long] = for (field <- fields.toList) yield getTermFieldFrequency(tokens, field)
    Freqs.sum
  }

  private def getFieldLength(fieldName: String): Long = {
    val fieldLen: Node = StructuredQuery.parse("#lengths:" + fieldName + ":part=lengths()")
    retrieval.getCollectionStatistics(fieldLen).collectionLength
  }

  override def getNewPhi(tokens: Seq[String], fieldName: String): Double = {
    val divider = getDivider(tokens)
    if (divider == 0) {
      Double.NegativeInfinity
    } else {
      log(getTermFieldFrequency(tokens, fieldName).toDouble / getFieldLength(fieldName) /
        divider)
    }
  }
}
