package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.CollationAlternate as JCollactionAlternate

/** Collation support allows the specific configuration of whether or not spaces and punctuation are considered base characters.
  *
  * @mongodb.server.release 3.4
  */
sealed trait CollationAlternate {
  private[driver] def toJava: JCollactionAlternate =
    this match {
      case CollationAlternate.Shifted      => JCollactionAlternate.SHIFTED
      case CollationAlternate.NonIgnorable => JCollactionAlternate.NON_IGNORABLE
    }
}

object CollationAlternate {

  /** Shifted
    *
    * <p>Spaces and punctuation are not considered base characters, and are only distinguished when the collation strength is &gt; 3</p>
    * @see CollationMaxVariable
    */
  case object Shifted extends CollationAlternate

  /** Non-ignorable
    *
    * <p>Spaces and punctuation are considered base characters</p>
    */
  case object NonIgnorable extends CollationAlternate
}
