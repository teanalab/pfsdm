package edu.wayne.pfsdm.auxiliary

import java.net.URL

import com.google.common.net.InternetDomainName
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
      val subj = Try {
        splitLine(0).drop(1).dropRight(1)
      }.toOption
      val obj = Try {
        splitLine(2).drop(1).dropRight(1)
      }
      Seq(subj, obj).flatten
    }.distinct().flatMap { url =>
      Try {
        new URL(url)
      }.toOption.map { url =>
        InternetDomainName.from(url.getHost).topPrivateDomain().name();
      }
    }.map(domain => (domain, 1)).reduceByKey(_ + _).map { case (d, c) => s"$d\t$c" }.saveAsTextFile(pathToOutput)
  }
}
