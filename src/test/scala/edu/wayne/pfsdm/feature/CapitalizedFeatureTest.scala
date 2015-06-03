package edu.wayne.pfsdm.feature

import org.scalatest.FunSuite

/**
 * Created by fsqcds on 6/2/15.
 */
class CapitalizedFeatureTest extends FunSuite {
  val cf = new CapitalizedFeature
  test("value is zero for non-capitalized") {
    assert(cf.getPhi(List("president"), "") === 0.0)
    assert(cf.getPhi(List("virtual", "museums"), "") === 0.0)
  }

  test("value is positive and same for capitalized") {
    assert(cf.getPhi(List("Florida"), "") > 0.0)
    assert(cf.getPhi(List("Nelson", "Mandela"), "") > 0.0)
    assert(cf.getPhi(List("Florida"), "") === cf.getPhi(List("Nelson", "Mandela"), ""))
  }
}
