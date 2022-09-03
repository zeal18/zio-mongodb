package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.CollationCaseFirst as JCollationCaseFirst

/** Collation support allows the specific configuration of how character cases are handled.
  *
  * @mongodb.server.release 3.4
  */
sealed trait CollationCaseFirst {
  private[driver] def toJava: JCollationCaseFirst = this match {
    case CollationCaseFirst.Off   => JCollationCaseFirst.OFF
    case CollationCaseFirst.Upper => JCollationCaseFirst.UPPER
    case CollationCaseFirst.Lower => JCollationCaseFirst.LOWER
  }
}

object CollationCaseFirst {

  /** Off
    */
  case object Off extends CollationCaseFirst

  /** Uppercase first
    */
  case object Upper extends CollationCaseFirst

  /** Lowercase first
    */
  case object Lower extends CollationCaseFirst
}
