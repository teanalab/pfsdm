package edu.wayne.pfsdm

import org.scalatest.FunSuite

/**
 * Created by fsqcds on 7/1/15.
 */
class UtilTest extends FunSuite {
  test("filterTokens should lowercase and stem") {
    assert(Util.filterTokens("Alexander Nevsky Cathedral Bulgarian city liberation Turks").mkString(" ") ===
      "alexander nevsky cathedral bulgaria city liberation turk")
  }

  test("filterTokens should stem and filter") {
    assert(Util.filterTokens("birds cannot fly").mkString(" ") === "bird fly")
  }
}
