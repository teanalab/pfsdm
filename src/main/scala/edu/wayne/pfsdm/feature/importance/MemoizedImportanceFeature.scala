package edu.wayne.pfsdm.feature.importance

/**
 * Created by fsqcds on 7/1/15.
 */
trait MemoizedImportanceFeature extends ImportanceFeature {
  var memo = Map[Seq[String], Double]()

  override def getPhi(tokens: Seq[String], queryId: String): Double = {
    memo.get(tokens) match {
      case Some(phi) => phi
      case None =>
        val phi = getNewPhi(tokens)
        memo += tokens -> getNewPhi(tokens)
        phi
    }
  }

  def getNewPhi(tokens: Seq[String]): Double
}
