package edu.wayne.pfsdm.feature

import nzhiltsov.fsdm.{FieldedSequentialDependenceTraversal, MLMTraversal}
import org.lemurproject.galago.core.retrieval.Retrieval
import org.lemurproject.galago.core.retrieval.query.{Node, StructuredQuery}
import org.lemurproject.galago.utility.Parameters

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 5/1/15.
 */
class BaselineTopScoreFieldFeature(val retrieval: Retrieval) extends MemoizedFieldFeature {
  private def mlm(tokens: Seq[String]): String = {
    s"#mlm(${tokens.mkString(" ")})"
  }

  private def fieldedsdm(tokens: Seq[String]): String = {
    s"#fieldedsdm(${tokens.mkString(" ")})"
  }


  override def getNewPhi(tokens: Seq[String], fieldName: String): Double = {
    val fields = retrieval.getGlobalParameters.getAsList("fields", classOf[String])
    val fieldWeights: Parameters = Parameters.create
    val root: Node =
      if (tokens.size == 1) {
        fields.foreach { weightedField =>
          fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField, if (weightedField == fieldName) 1.0 else 0.0)
        }
        StructuredQuery.parse(mlm(Seq(tokens.head)))
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
    val transformed: Node = retrieval.transformQuery(root, fieldWeights)
    val results = retrieval.executeQuery(transformed, fieldWeights).scoredDocuments
    if (results.size > 0) results.head.getScore else 0
  }
}
