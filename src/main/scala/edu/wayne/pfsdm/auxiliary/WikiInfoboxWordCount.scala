package edu.wayne.pfsdm.auxiliary

import java.io.PrintWriter

import edu.wayne.pfsdm.Util
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.log
import scala.util.matching.Regex.Match

/** Extract infoboxes from Readable Wikipedia and count words in them */
object WikiInfoboxWordCount {
  private def getPairRDD(articlesLines: RDD[String]) = {
    articlesLines.map { line =>
      val splitLine = line.split("\t")
      try {
        val wikiTitle = splitLine(0)
        val articleText = splitLine(1)
        (wikiTitle, articleText)
      } catch {
        case _: Throwable => ("", "")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val pathToReadableWiki = args(0)
    val pathToOutput = args(1)
    val binary = args(2) == "binary"
    val gramsFromQueries = (for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams)
      yield gram).toList

    val conf = new SparkConf().setAppName("WikiExtractInfobox")
    val sc = new SparkContext(conf)

    // RDD of a readableWikipedia where each line follows the format :
    //  article Title <tab> article text
    val readableWikipedia = sc.textFile(pathToReadableWiki)
    // RDD (WikiTitle, Article Text)
    val wikiTexts = getPairRDD(readableWikipedia)

    val wordCount = wikiTexts.flatMap {
      case (title, text) =>
        val infobox = """\{\{Infobox """.r findFirstMatchIn text
        infobox.flatMap { infoboxMatch =>
          var depth = 1
          var lastMatch: Option[Match] = None
          val parenses = """(\{\{|\}\})""".r
          parenses.findAllMatchIn(infoboxMatch.after).takeWhile { _ => depth > 0 }.foreach { parens =>
            parens.matched match {
              case "{{" => depth = depth + 1
              case "}}" => {
                depth = depth - 1
                lastMatch = Some(parens)
              }
            }
          }
          lastMatch.map(lastMatch => text.substring(infoboxMatch.start, infoboxMatch.end + lastMatch.end))
        }
    }.flatMap { line =>
      val stemmedTokens = Util.filterTokens(line).tail
      val grams = stemmedTokens.map(Seq(_)) union Util.unorderedBigrams(stemmedTokens)
      val filteredGrams = grams.filter(gramsFromQueries.contains(_))
      filteredGrams
    }.map(gram => (gram, 1)).reduceByKey(_ + _).collectAsMap()

    val output = new PrintWriter(pathToOutput)

    for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams) {
      output.print(qId)
      output.print("\t")
      output.print(gram.mkString(" "))
      output.print("\t")
      if (binary) {
        output.print(if (wordCount.getOrElse(gram, 0) > 0) 1 else 0)
      } else {
        output.print(log(wordCount.getOrElse(gram, 0).toDouble))
      }
      output.print("\t")
      output.println(FileBasedFeatureBlank.queries.toMap.get(qId).get)
    }
    output.close()
  }
}
