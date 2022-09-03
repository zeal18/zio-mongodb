package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.InsertManyOptions as JInsertManyOptions
import org.bson.BsonValue

/** The options to apply to an operation that inserts multiple documents into a collection.
  *
  * @mongodb.driver.manual tutorial/insert-documents/ Insert Tutorial
  * @mongodb.driver.manual reference/command/insert/ Insert Command
  */
final case class InsertManyOptions(
  ordered: Boolean = true,
  bypassDocumentValidation: Option[Boolean] = None,
  comment: Option[BsonValue] = None,
) {

  /** Sets whether the server should insert the documents in the order provided.
    *
    * @param ordered true if documents should be inserted in order
    */
  def withOrdered(ordered: Boolean): InsertManyOptions = copy(ordered = ordered)

  /** Sets the bypass document level validation flag.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @mongodb.server.release 3.2
    */
  def withBypassDocumentValidation(bypassDocumentValidation: Boolean): InsertManyOptions =
    copy(bypassDocumentValidation = Some(bypassDocumentValidation))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): InsertManyOptions =
    copy(comment = Some(comment))

  private[driver] def toJava: JInsertManyOptions = {
    val options = new JInsertManyOptions()

    options.ordered(ordered)
    bypassDocumentValidation.foreach(options.bypassDocumentValidation(_))
    comment.foreach(options.comment)

    options
  }
}
