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
      case term :: Nil =>{
        val node: Node = new Node("counts", term)
        node.getNodeParameters.set("part", "field." + fieldName)
        node
      }
      case term1 :: term2 :: Nil => {
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
    }
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
