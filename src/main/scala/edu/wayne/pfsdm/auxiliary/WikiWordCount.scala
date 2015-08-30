package edu.wayne.pfsdm.auxiliary

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
      yield gram.mkString(" ")).toList
    val gramsToQids: Map[String, Seq[String]] = (for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams)
      yield (gram.mkString(" "), qId)).groupBy(_._1).map { case (x, xs) => (x, xs.map(_._2)) }

    val conf = new SparkConf().setAppName("WikiWordCount")
    val sc = new SparkContext(conf)
    val tokenizedArticles: RDD[String] = sc.textFile(pathToWiki2VecOutput)
    val output = tokenizedArticles.map(line => line.trim.split(" +").tail).
      filter(tokenList => tokenList.length > 0 && tokenList(0) != "redirect").flatMap(tokens => {
      val filteredTokens = tokens.filterNot(_.startsWith("DBPEDIA_ID/")).
        map(token => token.replaceAll( """[\p{Punct}]""", ""))
      val stemmedTokens = Util.filterTokens(filteredTokens.mkString(" "))
      val grams = stemmedTokens union stemmedTokens.sliding(2).map(_.mkString(" ")).toSeq
      val filteredGrams = grams.filter(gramsFromQueries.contains(_))
      filteredGrams
    }).map(gram => (gram, 1)).reduceByKey(_ + _).
      flatMap { case (word, count) => gramsToQids(word).map(qid => ((qid, word), count)) }.sortByKey().
      map { case ((qid, word), count) => qid + "\t" + word + "\t" + count }
    output.saveAsTextFile(pathToOutput)
  }
}
