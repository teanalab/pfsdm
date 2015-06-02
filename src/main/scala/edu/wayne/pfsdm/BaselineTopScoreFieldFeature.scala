package edu.wayne.pfsdm

import fsdm2.experiments.FieldExperiment._
import fsdm2.experiments.Util
import nzhiltsov.fsdm.{FieldedSequentialDependenceTraversal, MLMTraversal}
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}
import org.lemurproject.galago.core.retrieval.{Retrieval, RetrievalFactory}
import org.lemurproject.galago.utility.Parameters
import org.lemurproject.galago.utility.tools.Arguments

import _root_.scala.io.Source
import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 5/1/15.
 */
class BaselineTopScoreFieldFeature(val traversal: ParametrizedFSDMTraversal) extends FieldFeature {
  def mlm(tokens: Seq[String]): String = {
    s"#mlm(${tokens.mkString(" ")})"
  }

  def fieldedsdm(tokens: Seq[String]): String = {
    s"#fieldedsdm(${tokens.mkString(" ")})"
  }

  override def getPhi(tokens: Seq[String], fieldName: String): Double = {
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
}
