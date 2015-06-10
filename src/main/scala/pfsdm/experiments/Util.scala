package pfsdm.experiments

import org.lemurproject.galago.core.parse.{Document, TagTokenizer}
import org.lemurproject.galago.core.util.WordLists

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

  private val defaultStopwords = WordLists.getWordList("inquery")

  def isStopWord(token: String): Boolean = {
    defaultStopwords.contains(token)
  }
}
