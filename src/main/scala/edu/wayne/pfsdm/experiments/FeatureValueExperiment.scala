package edu.wayne.pfsdm.experiments

import java.io._

import edu.wayne.pfsdm.{ParametrizedFSDMTraversal, FieldFeature}
import org.lemurproject.galago.core.retrieval.{Retrieval, RetrievalFactory}
import org.lemurproject.galago.utility.Parameters
import org.lemurproject.galago.utility.tools.Arguments
import BaselineScoreExperiment._

import scala.io._

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 6/10/15.
 */
object FeatureValueExperiment extends App {
  def getFeatureValues(tokens: Seq[String], feature: FieldFeature, fields: Seq[String]): Seq[Double] = {
    fields.map { field =>
      feature.getPhi(tokens, field)
    }
  }

  val parameters = Parameters.parseFile(new File(getClass.getResource("/traversal-config.json").toURI))
  val fields: Seq[String] = parameters.getList("fields", classOf[String])
  val mainParameters = Arguments.parse(args)
  parameters.copyFrom(mainParameters)

  val queries: Map[String, String] = Source.fromInputStream(
    getClass.getResourceAsStream("/sigir2013-dbpedia/queries.txt")).getLines().
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

  val qrels: Map[String, Seq[String]] = Source.fromInputStream(
    getClass.getResourceAsStream("/sigir2013-dbpedia/qrels.txt")).getLines().
    map { line => line.split("\t") match {
    case Array(qId, _, document, _) => (qId, document)
  }
  }.toSeq.groupBy(_._1).map { case (qId: String, pairs: Seq[(String, String)]) => (qId, pairs.map(_._2)) }

  parameters.set("fieldFeatures", List())
  val retrieval: Retrieval = RetrievalFactory.create(parameters)
  val traversal: ParametrizedFSDMTraversal = new ParametrizedFSDMTraversal(retrieval)
  val feature: FieldFeature = FieldFeature(mainParameters.getString("feature"), traversal)

  val output = new PrintWriter(mainParameters.getString("output"))
  output.println((Seq("ngramtype", "qid", "tokens") ++ fields).mkString("\t"))
  unigramQueries.foreach { case (qId: String, qToken: String) =>
    output.println(s"unigram\t$qId\t$qToken\t${getFeatureValues(List(qToken), feature, fields).mkString("\t")}")
  }
  bigramQueries.foreach { case (qId: String, qBigram: Seq[String]) =>
    println(qBigram.toList)
    output.println(s"bigram\t$qId\t${qBigram.mkString(" ")}\t${getFeatureValues(qBigram.toList, feature, fields).mkString("\t")}")
  }
  output.close()
}
