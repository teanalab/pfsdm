package edu.wayne.pfsdm.auxiliary

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/** Delete everything from Readable Wikipedia except infoboxes for further processing with Word2Vec from https://github.com/idio/wiki2vec */
object WikiExtractInfobox {
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

    val conf = new SparkConf().setAppName("WikiExtractInfobox")
    val sc = new SparkContext(conf)

    // RDD of a readableWikipedia where each line follows the format :
    //  article Title <tab> article text
    val readableWikipedia = sc.textFile(pathToReadableWiki)
    // RDD (WikiTitle, Article Text)
    val wikiTitleTexts = getPairRDD(readableWikipedia)

    wikiTitleTexts.map {
      case (title, text) =>
        val infobox = """\{\{(Infobox.* (?:\|.* )+)\}\}""".r

        title + "\t" + infobox
    }.saveAsTextFile(pathToOutput)
  }
}
