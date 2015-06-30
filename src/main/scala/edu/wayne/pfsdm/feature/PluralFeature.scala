package edu.wayne.pfsdm.feature

import edu.wayne.pfsdm.feature.FieldFeature

/**
 * Created by fsqcds on 6/2/15.
 */
class PluralFeature extends FieldFeature {
  private def isPlural(token: String): Boolean = {
    // very straightforward implementation, should make mistakes often
    token.length > 3 && token.codePointAt(token.length - 1) == 's'
  }

  override def getPhi(tokens: Seq[String], fieldName: String): Double = {
    if (isPlural(tokens.last)) 1.0 else 0.5
  }
}
