package edu.wayne.pfsdm

import java.io.StringReader

import nzhiltsov.belegaer.DbpediaLiteralAnalyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import scala.annotation.tailrec

/**
 * Created by fsqcds on 7/1/15.
 */
object Util {
  private val analyzer = new DbpediaLiteralAnalyzer(1, true)

  private def read(tokenStream: TokenStream): List[String] = read(List.empty, tokenStream)

  @tailrec
  private def read(accum: List[String], tokenStream: TokenStream): List[String] = if (!tokenStream.incrementToken) accum
  else read(accum :+ tokenStream.getAttribute(classOf[CharTermAttribute]).toString, tokenStream)

  def filterTokens(text: String): List[String] = {
    val tokenStream: TokenStream = analyzer.tokenStream("", new StringReader(text))
    tokenStream.reset
    val tokens = read(tokenStream)
    tokenStream.end
    tokenStream.close
    tokens.map(_.replace(".", ""))
  }

  def unorderedBigrams(tokens: Seq[String]) : Seq[Seq[String]] = {
    for (token1index <- tokens.indices; token2index <- tokens.indices
         if token1index != token2index && math.abs(token2index - token1index) < 8)
      yield Seq(tokens(token1index), tokens(token2index))
  }
}
