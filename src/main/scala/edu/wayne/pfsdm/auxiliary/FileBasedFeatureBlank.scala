package edu.wayne.pfsdm.auxiliary

import java.io._

import edu.wayne.pfsdm.Util

import scala.io._

/**
 * Created by fsqcds on 6/10/15.
 */
object FileBasedFeatureBlank extends App {
  val queries: Seq[(String, String)] = Source.fromFile("data/sigir2013-dbpedia/queries.txt").getLines().
    map { line => line.split("\t") match {
    case Array(qId, qText) => (qId, qText)
  }
  }.toSeq
  val tokenizedQueries: Seq[(String, Seq[String])] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.filterTokens(qText))
  }
  val uniBiGrams: Seq[(String, Seq[Seq[String]])] = tokenizedQueries.map { case (qId, qTokens) =>
    (qId, qTokens.map(Seq(_)) union (if (qTokens.size >= 2) qTokens.sliding(2).toSeq else Seq.empty))
  }

  val output = new PrintWriter("./output/file-based-feature-blank.tsv")

  for ((qId, grams) <- uniBiGrams; gram <- grams) {
    output.print(qId)
    output.print("\t")
    output.print(gram.mkString(" "))
    output.print("\t")
    output.print("\t")
    output.println(queries.toMap.get(qId).get)
  }
  output.close()
}
