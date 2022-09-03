package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.CollationStrength as JCollationStrength

/** Collation support allows the specific configuration of how differences between characters are handled.
  *
  * @mongodb.server.release 3.4
  */
sealed trait CollationStrength {
  private[driver] def toJava: JCollationStrength = this match {
    case CollationStrength.Primary    => JCollationStrength.PRIMARY
    case CollationStrength.Secondary  => JCollationStrength.SECONDARY
    case CollationStrength.Tertiary   => JCollationStrength.TERTIARY
    case CollationStrength.Quaternary => JCollationStrength.QUATERNARY
    case CollationStrength.Identical  => JCollationStrength.IDENTICAL
  }
}

object CollationStrength {

  /** Strongest level, denote difference between base characters
    */
  case object Primary extends CollationStrength

  /** Accents in characters are considered secondary differences
    */
  case object Secondary extends CollationStrength

  /** Upper and lower case differences in characters are distinguished at the tertiary level. The server default.
    */
  case object Tertiary extends CollationStrength

  /** When punctuation is ignored at level 1-3, an additional level can be used to distinguish words with and without punctuation.
    */
  case object Quaternary extends CollationStrength

  /** When all other levels are equal, the identical level is used as a tiebreaker.
    * The Unicode code point values of the NFD form of each string are compared at this level, just in case there is no difference at
    * levels 1-4
    */
  case object Identical extends CollationStrength
}
