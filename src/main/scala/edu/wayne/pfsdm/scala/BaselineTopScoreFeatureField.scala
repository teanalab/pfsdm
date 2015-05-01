package edu.wayne.pfsdm.scala

import edu.wayne.pfsdm.ParametrizedFSDMTraversal
import fsdm2.experiments.FieldExperiment._
import fsdm2.experiments.Util
import org.lemurproject.galago.core.retrieval.{RetrievalFactory, Retrieval}
import org.lemurproject.galago.core.retrieval.query.{StructuredQuery, Node}
import org.lemurproject.galago.utility.Parameters
import org.lemurproject.galago.utility.tools.Arguments
import scala.collection.JavaConversions._
import nzhiltsov.fsdm.{FieldedSequentialDependenceTraversal, MLMTraversal}

import scala.io.Source

/**
 * Created by fsqcds on 5/1/15.
 */
class BaselineTopScoreFieldFeature(val fieldName: String, val traversal: ParametrizedFSDMTraversal) extends FieldFeature {
  def mlm(tokens: Seq[String]): String = {
    s"#mlm(${tokens.mkString(" ")})"
  }

  def fieldedsdm(tokens: Seq[String]): String = {
    s"#fieldedsdm(${tokens.mkString(" ")})"
  }

  override def getPhi(tokens: Seq[String]): Double = {
    val fields = traversal.getFields
    val fieldWeights: Parameters = Parameters.create
    val root: Node =
      if (tokens.size == 1) {
        fields.foreach { weightedField =>
          fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField, if (weightedField == fieldName) 1.0 else 0.0)
        }
        StructuredQuery.parse(mlm(Seq(tokens(0))))
      } else if (tokens.size == 2) {
        fields.foreach { weightedField =>
          fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField, if (weightedField == fieldName) 1.0 else 0.0)
          fieldWeights.set(FieldedSequentialDependenceTraversal.ORDERED_FIELD_PREFIX + weightedField, if (weightedField == fieldName) 1.0 else 0.0)
          fieldWeights.set(FieldedSequentialDependenceTraversal.UNWINDOW_FIELD_PREFIX + weightedField, if (weightedField == fieldName) 1.0 else 0.0)
        }
        StructuredQuery.parse(fieldedsdm(tokens))
      } else {
        throw new IllegalArgumentException("Tokens must be either unigram or bigram")
      }
    fieldWeights.copyFrom(traversal.getGlobals)
    val transformed: Node = traversal.getRetrieval.transformQuery(root, fieldWeights)
    val results = traversal.getRetrieval.executeQuery(transformed, fieldWeights).scoredDocuments
    if (results.size > 0) results.head.getScore
    else Double.NaN
  }

  def main(args: Array[String]) {
    val mainParameters = Arguments.parse(args)
    val queries: Map[String, Seq[String]] = Source.fromInputStream(
      getClass.getResourceAsStream("/sigir2013-dbpedia/queries.txt")).getLines.
      map { line => line.split("\t") match {
      case Array(qId, qText) => (qId, qText)
    }
    }.toMap.map { case (qId: String, qText: String) =>
      (qId, Util.tokenize(qText))
    }

    parameters.copyFrom(mainParameters)
    val retrieval: Retrieval = RetrievalFactory.create(parameters)
    val traversal: ParametrizedFSDMTraversal = new ParametrizedFSDMTraversal(retrieval)
    val feature: BaselineTopScoreFieldFeature = FieldFeature("baselinetopscore", traversal)
    queries.foreach { case (qId: String, qTokens: Seq[String]) =>
      qTokens.foreach { token: String =>
        traversal.getFields.foreach { field: String =>
          println(s"unigram\t$qId\t$token\t$field\t${feature.getPhi(Seq(token))}")
        }
      }
      qTokens.sliding(2).foreach { tokens: Seq[String] =>
        traversal.getFields.foreach { field: String =>
          println(s"unigram\t$qId\t${tokens.mkString(" ")}\t$field\t${feature.getPhi(tokens)}")
        }
      }
    }
  }
}
