package fsdm2.experiments

import java.io.File

import nzhiltsov.fsdm.MLMTraversal
import org.lemurproject.galago.core.retrieval.query.{StructuredQuery, Node}
import org.lemurproject.galago.core.retrieval.{RetrievalFactory, Retrieval}
import org.lemurproject.galago.utility.Parameters
import org.lemurproject.galago.utility.tools.Arguments

import scala.io.Source

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 4/25/15.
 */
object FieldExperiment {
  val parameters = Parameters.parseFile(new File(getClass.getResource("/traversal-config.json").toURI))
  val fields: Seq[String] = parameters.getList("fields", classOf[String])

  def fieldedsdm(token: String): String = {
    "#fieldedsdm(" + token + ")"
  }

  def getTopScore(qId: String, token: String, retrieval: Retrieval, parameters: Parameters): Double = {
    val root: Node = StructuredQuery.parse(fieldedsdm(token))
    val queryParams: Parameters = Parameters.create()
    queryParams.copyFrom(parameters)
    val transformed: Node = retrieval.transformQuery(root, queryParams)
    val results = retrieval.executeQuery(transformed, queryParams).scoredDocuments
    if (results.size() > 0) results(0).getScore
    else Double.NaN
  }

  def getTopScores(qId: String, token: String, retrieval: Retrieval, parameters: Parameters): Seq[Double] = {
    fields.map { field =>
      val fieldWeights: Parameters = Parameters.create()
      fields.foreach { weightedField =>
        fieldWeights.set(MLMTraversal.UNIGRAM_FIELD_PREFIX + weightedField, if (weightedField == field) 1.0 else 0.0)
      }
      parameters.copyFrom(fieldWeights)
      getTopScore(qId, token, retrieval, parameters)
    }
  }


  def main(args: Array[String]) {
    print("qid\tterm\t")
    println(fields.mkString("\t"))
    val mainParameters = Arguments.parse(args)
    val queries: Seq[(String, String)] = Source.fromInputStream(
      getClass.getResourceAsStream("/sigir2013-dbpedia/queries.txt")).getLines.
      map { line => line.split("\t") match {
      case Array(qId, qText) => (qId, qText)
    }
    }.to

    parameters.copyFrom(mainParameters)
    val retrieval: Retrieval = RetrievalFactory.create(parameters)
    queries.foreach { query => Util.tokenize(query._2).foreach { token: String =>
      println(s"${query._1}\t$token\t${getTopScores(query._1, token, retrieval, parameters).mkString("\t")}")
    }
    }
  }
}
