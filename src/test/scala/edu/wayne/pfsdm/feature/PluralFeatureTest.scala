package edu.wayne.pfsdm.feature

import org.scalatest.FunSuite

/**
 * Created by fsqcds on 6/2/15.
 */
class PluralFeatureTest extends FunSuite {
  val pf = new PluralFeature
  test("value is zero for singular") {
    assert(pf.getPhi(List("thing"), "") === 0.0)
    assert(pf.getPhi(List("one", "thing"), "") === 0.0)
  }

  test("value is positive and same for plural") {
    assert(pf.getPhi(List("things"), "") > 0.0)
    assert(pf.getPhi(List("one", "things"), "") > 0.0)
    assert(pf.getPhi(List("things"), "") === pf.getPhi(List("things"), ""))
  }
}
