package fsdm2.experiments

import org.lemurproject.galago.core.parse.{Document, TagTokenizer}

import scala.collection.JavaConversions._

/**
 * Created by fsqcds on 4/25/15.
 */
object Util {
  def tokenize(text: String): Seq[String] = {
    val tokenizer = new TagTokenizer()
    val document = new Document()
    document.text = text
    tokenizer.process(document)
    document.terms
  }
}
