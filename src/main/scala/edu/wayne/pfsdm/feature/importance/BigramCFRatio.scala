package edu.wayne.pfsdm.feature.importance

import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.Node

import scala.math.log

/**
 * Created by fsqcds on 5/1/15.
 */
class BigramCFRatio(val retrieval: Retrieval) extends MemoizedImportanceFeature {
  private def getTermFrequency(tokens: Seq[String]): Long = {
    val node: Node = (tokens.toList: @unchecked) match {
      case term :: Nil =>
        val node: Node = new Node("counts", term)
        node
      case term1 :: term2 :: Nil =>
        val t1: Node = new Node("extents", term1)
        val t2: Node = new Node("extents", term2)
        val od1: Node = new Node("unordered")
        od1.getNodeParameters.set("default", 16)
        od1.addChild(t1)
        od1.addChild(t2)
        od1
    }
    retrieval.getNodeStatistics(node).nodeFrequency
  }

  override def getNewPhi(tokens: Seq[String]): Double = {
    (tokens.toList: @unchecked) match {
      case term1 :: term2 :: Nil =>
        val term1Freq = getTermFrequency(Seq(term1))
        val term2Freq = getTermFrequency(Seq(term2))
        if (term1Freq == 0 || term2Freq == 0) {
          Double.NegativeInfinity
        } else {
          log(getTermFrequency(tokens).toDouble / (term1Freq * term2Freq))
        }
      case term1 :: Nil => Double.NegativeInfinity
    }
  }
}