package edu.wayne.pfsdm.feature

import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}

import scala.math.log

/**
 * Created by fsqcds on 5/1/15.
 */
class BigramCFRatio(val retrieval: Retrieval) extends MemoizedFieldFeature {
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

  override def getNewPhi(tokens: Seq[String], fieldName: String): Double = {
    (tokens.toList: @unchecked) match {
      case term :: Nil => 0
      case term1 :: term2 :: Nil =>
        val term1Freq = getTermFrequency(Seq(term1), fieldName)
        println(term1 + " " + term1Freq)
        val term2Freq = getTermFrequency(Seq(term2), fieldName)
        println(term2 + " " + term2Freq)
        if (term1Freq == 0 || term2Freq == 0) {
          Double.NegativeInfinity
        } else {
          println(getTermFrequency(tokens, fieldName))
          log(getTermFrequency(tokens, fieldName).toDouble / (term1Freq * term2Freq))
        }
    }
  }
}
