package edu.wayne.pfsdm.experiments

import java.io._

import nzhiltsov.fsdm.{FieldedSequentialDependenceTraversal, MLMTraversal}
import org.lemurproject.galago.core.retrieval.query.{StructuredQuery, Node}
import org.lemurproject.galago.core.retrieval.{RetrievalFactory, Retrieval}
import org.lemurproject.galago.utility.Parameters
import org.lemurproject.galago.utility.tools.Arguments

import scala.io._

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 4/25/15.
 */
object BaselineScores {
  val parameters = Parameters.parseFile(new File(getClass.getResource("/traversal-config.json").toURI))
  val fields: Seq[String] = parameters.getList("fields", classOf[String])

  def mlm(tokens: Seq[String]): String = {
    s"#mlm(${tokens.mkString(" ")})"
  }

  def getUniTopScore(qId: String, token: String, relevant: Boolean, qrels: Map[String, Seq[String]],
                     retrieval: Retrieval, parameters: Parameters): Double = {
    val root: Node = StructuredQuery.parse(mlm(Seq(token)))
    val queryParams: Parameters = Parameters.create()
    queryParams.copyFrom(parameters)
    val transformed: Node = retrieval.transformQuery(root, queryParams)
    val results = retrieval.executeQuery(transformed, queryParams).scoredDocuments
    val filteredResults = results.filter {result => qrels(qId).contains(result.getName) == relevant }
    if (filteredResults.size > 0) filteredResults.head.getScore
    else Double.NaN
  }

  def getUniTopScores(qId: String, token: String, relevant: Boolean, qrels: Map[String, Seq[String]],
                      retrieval: Retrieval, parameters: Parameters): Seq[Double] = {
    fields.map { field =>
      val fieldWeights: Parameters = Parameters.create()
      fields.foreach { weightedField =>
        fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField, if (weightedField == field) 1.0 else 0.0)
      }
      parameters.copyFrom(fieldWeights)
      getUniTopScore(qId, token, relevant, qrels, retrieval, parameters)
    }
  }

  def fieldedsdm(tokens: Seq[String]): String = {
    s"#fieldedsdm(${tokens.mkString(" ")})"
  }

  def getBiTopScore(qId: String, bigram: Seq[String], relevant: Boolean, qrels: Map[String, Seq[String]],
                    retrieval: Retrieval, parameters: Parameters): Double = {
    val root: Node = StructuredQuery.parse(fieldedsdm(bigram))
    val queryParams: Parameters = Parameters.create()
    queryParams.copyFrom(parameters)
    val transformed: Node = retrieval.transformQuery(root, queryParams)
    val results = retrieval.executeQuery(transformed, queryParams).scoredDocuments
    val filteredResults = results.filter {result => qrels(qId).contains(result.getName) == relevant }
    if (filteredResults.size > 0) filteredResults.head.getScore
    else Double.NaN
  }

  def getBiTopScores(qId: String, bigram: Seq[String], relevant: Boolean, qrels: Map[String, Seq[String]],
                     retrieval: Retrieval, parameters: Parameters): Seq[Double] = {
    fields.map { field =>
      val fieldWeights: Parameters = Parameters.create()
      fields.foreach { weightedField =>
        fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField, if (weightedField == field) 1.0 else 0.0)
        fieldWeights.set(FieldedSequentialDependenceTraversal.ORDERED_FIELD_PREFIX + weightedField, if (weightedField == field) 1.0 else 0.0)
        fieldWeights.set(FieldedSequentialDependenceTraversal.UNWINDOW_FIELD_PREFIX + weightedField, if (weightedField == field) 1.0 else 0.0)
      }
      parameters.copyFrom(fieldWeights)
      getBiTopScore(qId, bigram, relevant, qrels, retrieval, parameters)
    }
  }

  def main(args: Array[String]) {
    val mainParameters = Arguments.parse(args)
    val queries: Map[String, String] = Source.fromFile("data/sigir2013-dbpedia/queries.txt").getLines().
      map { line => line.split("\t") match {
      case Array(qId, qText) => (qId, qText)
    }
    }.toMap
    val tokenizedQueries: Map[String, Seq[String]] = queries.map { case (qId: String, qText: String) =>
      (qId, Util.tokenize(qText).filterNot(Util.isStopWord))
    }
    val unigramQueries: Map[String, String] = tokenizedQueries.filter { case (_, qTokens: Seq[String]) =>
      qTokens.length == 1
    }.map { case (qId: String, qTokens: Seq[String]) => (qId, qTokens.head) }
    val bigramQueries: Map[String, Seq[String]] = tokenizedQueries.filter { case (_, qTokens: Seq[String]) =>
      qTokens.length == 2
    }

    val qrels: Map[String, Seq[String]] = Source.fromFile("data/sigir2013-dbpedia/qrels.txt").getLines().
      map { line => line.split("\t") match {
      case Array(qId, _, document, _) => (qId, document)
    }
    }.toSeq.groupBy(_._1).map { case (qId: String, pairs: Seq[(String, String)]) => (qId, pairs.map(_._2)) }

    parameters.copyFrom(mainParameters)
    val retrieval: Retrieval = RetrievalFactory.create(parameters)
    val output = new PrintWriter("output/experiments/baseline_scores/baseline_scores.tsv")
    output.println((Seq("ngramtype", "qid", "tokens", "relevance") ++ fields).mkString("\t"))
    unigramQueries.foreach { case (qId: String, qToken: String) =>
      output.println(s"unigram\t$qId\t$qToken\trelevant\t${getUniTopScores(qId, qToken, true, qrels, retrieval, parameters).mkString("\t")}")
      output.println(s"unigram\t$qId\t$qToken\tnonrelevant\t${getUniTopScores(qId, qToken, false, qrels, retrieval, parameters).mkString("\t")}")
    }
    bigramQueries.foreach { case (qId: String, qBigram: Seq[String]) =>
      output.println(s"bigram\t$qId\t${qBigram.mkString(" ")}\trelevant\t${getBiTopScores(qId, qBigram, true, qrels, retrieval, parameters).mkString("\t")}")
      output.println(s"bigram\t$qId\t${qBigram.mkString(" ")}\tnonrelevant\t${getBiTopScores(qId, qBigram, false, qrels, retrieval, parameters).mkString("\t")}")
    }
    output.close()
  }
}