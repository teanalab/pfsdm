package edu.wayne.pfsdm.auxiliary

import java.io.PrintWriter

import edu.wayne.pfsdm.Util
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/** Finds TF in wikipedia of concepts from queries using output from https://github.com/idio/wiki2vec */
object WikiWordCount {
  def main(args: Array[String]): Unit = {
    val pathToWiki2VecOutput = args(0)
    val pathToOutput = args(1)
    val gramsFromQueries = (for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams)
      yield gram).toList

    val conf = new SparkConf().setAppName("WikiWordCount")
    val sc = new SparkContext(conf)
    val tokenizedArticles: RDD[String] = sc.textFile(pathToWiki2VecOutput)
    val wordCount = tokenizedArticles.map(line => line.trim.split(" +").tail).
      filter(tokenList => tokenList.length > 0 && tokenList(0) != "redirect").flatMap(tokens => {
      val filteredTokens = tokens.filterNot(_.startsWith("DBPEDIA_ID/")).
        map(token => token.replaceAll( """[\p{Punct}]""", ""))
      val stemmedTokens = Util.filterTokens(filteredTokens.mkString(" "))
      val grams = stemmedTokens.map(Seq(_)) union stemmedTokens.sliding(2).toSeq
      val filteredGrams = grams.filter(gramsFromQueries.contains(_))
      filteredGrams
    }).map(gram => (gram, 1)).reduceByKey(_ + _).collect().toMap

    val output = new PrintWriter(pathToOutput)

    for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams) {
      output.print(qId)
      output.print("\t")
      output.print(gram.mkString(" "))
      output.print("\t")
      output.print(wordCount.getOrElse(gram, 0))
      output.print("\t")
      output.println(FileBasedFeatureBlank.queries.toMap.get(qId).get)
    }
  }
}
