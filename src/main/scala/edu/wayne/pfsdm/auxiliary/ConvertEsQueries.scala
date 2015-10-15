package edu.wayne.pfsdm.auxiliary

import java.io.PrintWriter

import edu.wayne.pfsdm.Util

import scala.io._

/**
 * Created by fsqcds on 10/15/15.
 */
object ConvertEsQueries extends App {
  val inputPath = args(0)
  val outputPath = args(1)
  val operator = args(2)

  def queries: Seq[(String, String)] = Source.fromFile(inputPath).getLines().
    map { line => line.split("\t") match {
    case Array(qId, qText) => ("q" + qId, qText)
  }
  }.toSeq

  def tokenizedQueries: Seq[(String, Seq[String])] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.filterTokens(qText))
  }

  val output = new PrintWriter(outputPath)
  output.println("{\n\"queries\" : [")

  for ((qId, tokens) <- tokenizedQueries) {
    output.println("{")
    output.println("\"number\": \"" + qId + "\",")
    output.println("\"text\": \"#" + operator + "(" + tokens.mkString(" ") + ")\"")
    output.println("},")
  }
  output.println("]\n}")
  output.close()
}
