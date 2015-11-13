package edu.wayne.pfsdm.auxiliary

import java.net.URL

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/** Count domains of entities in BTC */
object CountEntityDomains {
  def main(args: Array[String]): Unit = {
    val pathToEntityDescriptions = args(0)
    val pathToOutput = args(1)

    val conf = new SparkConf().setAppName("CountEntityDomains")
    val sc = new SparkContext(conf)
    val descriptions = sc.textFile(pathToEntityDescriptions)

    val wordCount = descriptions.flatMap { line =>
      val splitLine = line.split("\t")
      val hostSubj = Try {
        val url = splitLine(0).drop(1).dropRight(1)
        new URL(url)
      }.toOption.map {
        _.getHost.split('.').takeRight(2).mkString(".")
      }
      val hostObj = Try {
        val url = splitLine(2).drop(1).dropRight(1)
        new URL(url)
      }.toOption.map {
        _.getHost.split('.').takeRight(2).mkString(".")
      }
      Seq(hostSubj, hostObj).flatten
    }.map(domain => (domain, 1)).reduceByKey(_ + _).map { case (d, c) => s"$d\t$c" }.saveAsTextFile(pathToOutput)
  }
}
