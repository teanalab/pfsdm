package edu.wayne.pfsdm.auxiliary

import java.io.PrintStream

import org.lemurproject.galago.contrib.learning.LearnQueryParameters
import org.lemurproject.galago.utility.Parameters
import org.lemurproject.galago.utility.tools.Arguments

/**
 * Created by fsqcds on 7/20/15.
 */
object LearnAndSave extends LearnQueryParameters {
  def main(args: Array[String]) {
    val p: Parameters = Arguments.parse(args)
    val pw = new PrintStream(p.getString("output"))
    new LearnQueryParameters().run(p, pw)
    pw.close()
  }
}