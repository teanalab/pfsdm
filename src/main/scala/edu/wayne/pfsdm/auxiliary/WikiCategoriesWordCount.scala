package edu.wayne.pfsdm.auxiliary

import java.io.PrintWriter

import edu.wayne.pfsdm.Util
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.log

/** Finds TF or presence of concepts from queries in wikipedia categories (enwiki-latest-category.sql) */
object WikiCategoriesWordCount {
  def main(args: Array[String]): Unit = {
    val pathToSql = args(0)
    val pathToOutput = args(1)
    val binary = args(2) == "binary"
    val gramsFromQueries = (for ((qId, grams) <- FileBasedFeatureBlank.uniBiGrams; gram <- grams)
      yield gram).toList

    val conf = new SparkConf().setAppName("WikiTitlesWordCount")
    val sc = new SparkContext(conf)
    val categoriesSql = sc.textFile(pathToSql)

    val wordCount = categoriesSql.filter(_.startsWith("INSERT INTO `category` VALUES")).flatMap{ insertLine =>
      val row = """\(\d+,'([^']+)',\d+,\d+,\d+\)""".r
      for (m <- row findAllMatchIn insertLine) yield m group 1
    }.flatMap { line =>
      val stemmedTokens = Util.filterTokens(line.replaceAll("_", ""))
      val grams = stemmedTokens.map(Seq(_)) union stemmedTokens.sliding(2).toSeq
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
