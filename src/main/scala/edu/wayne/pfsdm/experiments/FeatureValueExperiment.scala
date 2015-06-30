package edu.wayne.pfsdm.experiments

import java.io._

import edu.wayne.pfsdm.ParametrizedFSDMTraversal
import edu.wayne.pfsdm.feature.FieldFeature
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
  val mainParameters = Arguments.parse(args)
  parameters.copyFrom(mainParameters)
  val fields: Seq[String] = parameters.getList("fields", classOf[String])

  val queries: Seq[(String, String)] = Source.fromFile("data/sigir2013-dbpedia/queries.txt").getLines().
    map { line => line.split("\t") match {
    case Array(qId, qText) => (qId, qText)
  }
  }.toSeq
  val tokenizedQueries: Seq[(String, Seq[String])] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.tokenize(qText).filterNot(Util.isStopWord))
  }
  val uniBiGrams: Seq[(String, Seq[Seq[String]])] = tokenizedQueries.map { case (qId, qTokens) =>
    (qId, qTokens.map(Seq(_)) union (if (qTokens.size >= 2) qTokens.sliding(2).toSeq else Seq.empty))
  }

  parameters.set("fieldFeatures", List())
  val retrieval: Retrieval = RetrievalFactory.create(parameters)
  val traversal: ParametrizedFSDMTraversal = new ParametrizedFSDMTraversal(retrieval)
  val feature: FieldFeature = FieldFeature(mainParameters.getString("feature"), retrieval)

  val output = new PrintWriter(mainParameters.getString("output"))
  output.println((Seq("ngramtype", "qid", "gram") ++ fields).mkString("\t"))
  for ((qId, grams) <- uniBiGrams; gram <- grams) {
    val ngramtype = gram.length match {
      case 1 => "unigram";
      case 2 => "bigram"
    }
    output.println(s"$ngramtype\t$qId\t${gram.mkString(" ")}\t${getFeatureValues(gram, feature, fields).mkString("\t")}")
  }
  output.close()
}
