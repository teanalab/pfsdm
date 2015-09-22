package edu.wayne.pfsdm.auxiliary

import java.io.PrintWriter

import edu.wayne.pfsdm.Util
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.log

/** Finds TF or presence of concepts from queries in wikipedia titles using enwiki-latest-all-titles-in-ns0 */
object GoogleNgramsWordCount {
  def main(args: Array[String]): Unit = {
    val pathToGoogleNgrams = args(0)
    val pathToOutput = args(1)
    val binary = args(2) == "binary"
    val gramsFromQueries = (for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams)
      yield gram).toList

    val conf = new SparkConf().setAppName("GoogleNgramsWordCount")
    val sc = new SparkContext(conf)
    val ngrams = sc.textFile(pathToGoogleNgrams)

    val wordCount = ngrams.filter(_.contains("_")).flatMap { line =>
      val splitLine = line.split("\t")
      val stemmedTokens = Util.filterTokens(splitLine(0))
      val matchCount = splitLine(2).toLong
      if (gramsFromQueries.contains(stemmedTokens)) {
        Some((stemmedTokens, matchCount))
      } else {
        None
      }
    }.reduceByKey(_ + _).collectAsMap()

    val output = new PrintWriter(pathToOutput)

    for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams) {
      output.print(qId)
      output.print("\t")
      output.print(gram.mkString(" "))
      output.print("\t")
      if (binary) {
        output.print(if (wordCount.getOrElse(gram.toList, 0L) > 0) 1 else 0)
      } else {
        output.print(log(wordCount.getOrElse(gram.toList, 0L).toDouble))
      }
      output.print("\t")
      output.println(FileBasedFeatureBlank.queries.toMap.get(qId).get)
    }
    output.close()
  }
}
