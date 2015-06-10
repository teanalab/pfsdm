package edu.wayne.pfsdm.feature

import edu.wayne.pfsdm.FieldFeature

/**
 * Created by fsqcds on 6/2/15.
 */
class CapitalizedFeature extends FieldFeature {
  private def firstLetterUpperCase(token: String): Boolean = {
    require(token.length > 0)
    Character.isUpperCase(token.codePointAt(0))
  }

  override def getPhi(tokens: Seq[String], fieldName: String): Double = {
    if (tokens.forall(firstLetterUpperCase)) 1.0 else 0.5
  }
}
