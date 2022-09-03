package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.InsertOneOptions as JInsertOneOptions
import org.bson.BsonValue

/** The options to apply to an operation that inserts a single document into a collection.
  *
  * @mongodb.server.release 3.2
  * @mongodb.driver.manual tutorial/insert-documents/ Insert Tutorial
  * @mongodb.driver.manual reference/command/insert/ Insert Command
  */
final case class InsertOneOptions(
  bypassDocumentValidation: Option[Boolean] = None,
  comment: Option[BsonValue] = None,
) {

  /** Sets the bypass document level validation flag.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    */
  def withBypassDocumentValidation(bypassDocumentValidation: Boolean): InsertOneOptions =
    copy(bypassDocumentValidation = Some(bypassDocumentValidation))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): InsertOneOptions =
    copy(comment = Some(comment))

  private[driver] def toJava: JInsertOneOptions = {
    val jInsertOneOptions = new JInsertOneOptions()

    bypassDocumentValidation.foreach(jInsertOneOptions.bypassDocumentValidation(_))
    comment.foreach(jInsertOneOptions.comment)

    jInsertOneOptions
  }
}
