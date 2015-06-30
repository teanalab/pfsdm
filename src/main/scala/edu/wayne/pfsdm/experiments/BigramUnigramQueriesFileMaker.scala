package edu.wayne.pfsdm.experiments

import java.io.PrintWriter

import scala.io.Source

/**
 * Created by fsqcds on 6/10/15.
 */
object BigramUnigramQueriesFileMaker extends App {
  val queries: Map[String, String] = Source.fromInputStream(
    getClass.getResourceAsStream("/sigir2013-dbpedia/queries.txt")).getLines.
    map { line => line.split("\t") match {
    case Array(qId, qText) => (qId, qText)
  }
  }.toMap
  val tokenizedQueries: Map[String, Seq[String]] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.tokenize(qText).filterNot(Util.isStopWord))
  }
  val uniBigramQueries: Map[String, Seq[String]] = tokenizedQueries.filter { case (_, qTokens: Seq[String]) =>
    qTokens.length == 1 || qTokens.length == 2
  }
  val output = new PrintWriter("output/unibigrams-named.tsv")
  output.println(Seq("qid", "text", "name").mkString("\t"))
  uniBigramQueries.foreach { case (qId: String, qToken: Seq[String]) =>
    output.println(s"$qId\t${queries(qId)}\t")
  }
  output.close()
  println("Now manually filter namequeris.tsv and move it to resources")
}