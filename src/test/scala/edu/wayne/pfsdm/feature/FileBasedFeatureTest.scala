package edu.wayne.pfsdm.feature

import org.scalatest.FunSuite

/**
 * Created by fsqcds on 7/3/15.
 */
class FileBasedFeatureTest extends FunSuite {
  val testFileFeature = FieldFeature("/test-file-feature", null)
  test("getPhi") {
    assert(testFileFeature.getPhi(Seq("vietnam"), "", "INEX_LD-20120111") === 0.0)
    assert(testFileFeature.getPhi(Seq("war"), "", "INEX_LD-20120111") === 1.0)
    assert(testFileFeature.getPhi(Seq("vietnam", "war"), "", "INEX_LD-20120111") === 0.0)
    assert(testFileFeature.getPhi(Seq("award", "wikileak"), "", "QALD2_tr-29") === 1.0)
  }

  test("getPhi should throw exception if value not found") {
    intercept[Exception] {
      testFileFeature.getPhi(Seq("award", "wikileak"), "", "QALD2_tr-28")
    }
  }
}
