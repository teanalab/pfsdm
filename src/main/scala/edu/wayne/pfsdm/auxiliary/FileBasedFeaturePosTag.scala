package edu.wayne.pfsdm.auxiliary

import java.io._

import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import edu.wayne.pfsdm.Util

import scala.collection.JavaConversions._
import scala.io._

/**
  * Created by fsqcds on 6/10/15.
  */
object FileBasedFeaturePosTag extends App {
  def queries: Seq[(String, String)] = Source.fromInputStream(
    getClass.getResourceAsStream("/btc/all-queries.txt")).getLines().
    map { line => line.split("\t") match {
      case Array(qId, qText) => (qId, qText)
    }
    }.toSeq

  def tokenizedQueries: Seq[(String, Seq[String])] = queries.map { case (qId: String, qText: String) =>
    (qId, Util.filterTokens(qText))
  }

  def uniBiGrams: Seq[(String, Seq[Seq[String]])] = tokenizedQueries.map { case (qId, qTokens) =>
    (qId, qTokens.map(Seq(_)) union (if (qTokens.size >= 2) qTokens.sliding(2).toSeq else Seq.empty))
  }

  def getTag(query: String, stemmedToken: String): String = {
    val sentences = MaxentTagger.tokenizeText(new StringReader(query.replace('-', ' ')))
    for (sentence <- sentences) {
      val tSentence = tagger.tagSentence(sentence)
      for (taggedWord: TaggedWord <- tSentence) {
        if (Util.filterTokens(taggedWord.word()).nonEmpty &&
          Util.filterTokens(taggedWord.word()).head == stemmedToken) {
          return taggedWord.tag()
        }
      }
    }
    null
  }

  val output = new PrintWriter(args(0))
  val posTag = args(1)

  val tagger = new MaxentTagger("data/english-bidirectional-distsim.tagger")

  for ((qId, grams) <- uniBiGrams; gram <- grams) {
    output.print(qId)
    output.print("\t")
    output.print(gram.mkString(" "))
    output.print("\t")
    // only for unigrams
    if (gram.length == 1) {
      output.print(if (getTag(queries.toMap.get(qId).get, gram(0)) == posTag) 1 else 0)
    } else {
      output.print(0)
    }
    output.print("\t")
    output.println(queries.toMap.get(qId).get)
  }
  output.close()
}
