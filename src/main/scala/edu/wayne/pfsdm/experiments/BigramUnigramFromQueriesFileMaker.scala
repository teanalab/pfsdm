package edu.wayne.pfsdm.experiments

import java.io.PrintWriter

import scala.io.Source

/**
 * Created by fsqcds on 6/10/15.
 */
object BigramUnigramFromQueriesFileMaker extends App {
  val queries: Seq[(String, String)] = Source.fromInputStream(
    getClass.getResourceAsStream("/sigir2013-dbpedia/queries.txt")).getLines().map {
    line => line.split("\t") match {
      case Array(qId, qText) => (qId, qText)
    }
  }.toSeq
  val queriesMap: Map[String, String] = queries.toMap
  val tokenizedQueries: Seq[(String, Seq[String])] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.tokenize(qText).filterNot(Util.isStopWord))
  }
  val uniBiGrams: Seq[(String, Seq[Seq[String]])] = tokenizedQueries.map { case (qId, qTokens) =>
    (qId, qTokens.map(Seq(_)) union (if (qTokens.size >= 2) qTokens.sliding(2).toSeq else Seq.empty))
  }
  val output = new PrintWriter("output/unibigrams-types.tsv")
  output.println(Seq("qid", "text", "gram", "type").mkString("\t"))
  for ((qId, grams) <- uniBiGrams; gram <- grams) {
    output.println(s"$qId\t${queriesMap(qId)}\t${gram.mkString(" ")}\t")
  }
  output.close()
  println("Now manually categorize unibigrams-types.tsv and move it to resources")
}