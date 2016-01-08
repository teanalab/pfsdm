package edu.wayne.pfsdm.auxiliary

import java.io.PrintWriter

import edu.wayne.pfsdm.Util

import scala.io._

/**
 * Created by fsqcds on 10/15/15.
 */
object ConvertRawQueriesToTSV extends App {
  val inputPath = args(0)
  val outputPath = args(1)

  def queries: Seq[(String, String)] = Source.fromFile(inputPath).getLines().
    map { line => line.split("\t") match {
    case Array(qId, qText) => (qId, qText)
  }
  }.toSeq

  def tokenizedQueries: Seq[(String, Seq[String])] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.filterTokens(qText))
  }

  val output = new PrintWriter(outputPath)

  for ((qId, tokens) <- tokenizedQueries) {
    output.println(qId + "\t" +  tokens.mkString(" "))
  }
  output.close()
}
