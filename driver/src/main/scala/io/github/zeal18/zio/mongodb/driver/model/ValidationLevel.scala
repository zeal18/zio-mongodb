package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.ValidationLevel as JValidationLevel

/** Determines how strictly MongoDB applies the validation rules to existing documents during an insert or update.
  *
  * @mongodb.server.release 3.2
  * @mongodb.driver.manual reference/method/db.createCollection/ Create Collection
  */
sealed trait ValidationLevel {
  private[driver] def toJava: JValidationLevel = this match {
    case ValidationLevel.Off      => JValidationLevel.OFF
    case ValidationLevel.Moderate => JValidationLevel.MODERATE
    case ValidationLevel.Strict   => JValidationLevel.STRICT
  }
}

object ValidationLevel {

  /** No validation for inserts or updates.
    */
  case object Off extends ValidationLevel

  /** Apply validation rules to all inserts and all updates.
    */
  case object Strict extends ValidationLevel

  /** Applies validation rules to inserts and to updates on existing valid documents.
    *
    * <p>Does not apply rules to updates on existing invalid documents.</p>
    */
  case object Moderate extends ValidationLevel
}
