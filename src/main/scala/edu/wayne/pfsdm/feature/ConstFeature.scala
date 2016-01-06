package edu.wayne.pfsdm.feature

import edu.wayne.pfsdm.feature.field.FieldFeature
import edu.wayne.pfsdm.feature.importance.ImportanceFeature

import scala.io.Source

/**
 * Created by fsqcds on 7/3/15.
 */
class ConstFeature extends FieldFeature with ImportanceFeature {

  override def getPhi(tokens: Seq[String], fieldName: String, queryId: String): Double = {
    1
  }

  override def getPhi(tokens: Seq[String], queryId: String): Double = {
    1
  }
}
