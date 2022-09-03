package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.ValidationAction as JValidationAction

/** Determines whether to error on invalid documents or just warn about the violations but allow invalid documents.
  *
  * @mongodb.server.release 3.2
  * @mongodb.driver.manual reference/method/db.createCollection/ Create Collection
  */
sealed trait ValidationAction {
  private[driver] def toJava: JValidationAction = this match {
    case ValidationAction.Warn  => JValidationAction.WARN
    case ValidationAction.Error => JValidationAction.ERROR
  }
}

object ValidationAction {

  /** Documents do not have to pass validation. If the document fails validation, the write operation logs the validation failure to
    * the mongod logs.
    */
  case object Warn extends ValidationAction

  /** Documents must pass validation before the write occurs. Otherwise, the write operation fails.
    */
  case object Error extends ValidationAction
}
