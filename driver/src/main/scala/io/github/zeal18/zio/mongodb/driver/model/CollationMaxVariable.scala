package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.CollationMaxVariable as JCollationMaxVariable

/** Collation support allows the specific configuration of whether or not spaces and punctuation are considered base characters.
  *
  * <p>{@code CollationMaxVariable} controls which characters are affected by {@link CollationAlternate#Shifted}.</p>
  *
  * @see CollationAlternate#Shifted
  * @mongodb.server.release 3.4
  */
sealed trait CollationMaxVariable {
  private[driver] def toJava: JCollationMaxVariable = this match {
    case CollationMaxVariable.Punct => JCollationMaxVariable.PUNCT
    case CollationMaxVariable.Space => JCollationMaxVariable.SPACE
  }
}

object CollationMaxVariable {

  /** Punct
    *
    * <p>Both punctuation and spaces are affected.</p>
    */
  case object Punct extends CollationMaxVariable

  /** Shifted
    *
    * <p>Only spaces are affected.</p>
    */
  case object Space extends CollationMaxVariable
}
