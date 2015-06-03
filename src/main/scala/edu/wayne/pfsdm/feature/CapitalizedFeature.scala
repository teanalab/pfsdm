package edu.wayne.pfsdm.feature

import edu.wayne.pfsdm.FieldFeature

/**
 * Created by fsqcds on 6/2/15.
 */
class CapitalizedFeature extends FieldFeature {
  override def getPhi(tokens: Seq[String], fieldName: String): Double = {
    if (tokens.forall { token: String => Character.isUpperCase(token.codePointAt(0)) }) 1.0 else 0.0;
  }
}
