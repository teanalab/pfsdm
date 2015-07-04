package edu.wayne.pfsdm.feature

import org.lemurproject.galago.core.retrieval.Retrieval

/**
 * Created by fsqcds on 7/1/15.
 */
trait MemoizedFieldFeature extends FieldFeature {
  var memo = Map[(Seq[String], String), Double]()

  override def getPhi(tokens: Seq[String], fieldName: String, queryId: String): Double = {
    memo.get((tokens, fieldName)) match {
      case Some(phi) => phi
      case None =>
        val phi = getNewPhi(tokens, fieldName)
        memo += (tokens, fieldName) -> getNewPhi(tokens, fieldName)
        phi
    }
  }

  def getNewPhi(tokens: Seq[String], fieldName: String): Double
}
