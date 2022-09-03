package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.BulkWriteOptions as JBulkWriteOptions
import org.bson.BsonValue
import org.bson.conversions.Bson

/** The options to apply to a bulk write.
  */
final case class BulkWriteOptions(
  ordered: Boolean = true,
  bypassDocumentValidation: Option[Boolean] = None,
  comment: Option[BsonValue] = None,
  variables: Option[Bson] = None,
) {

  /** If true, then when a write fails, return without performing the remaining
    * writes. If false, then when a write fails, continue with the remaining writes, if any.
    * Defaults to true.
    *
    * @param ordered true if the writes should be ordered
    */
  def withOrdered(ordered: Boolean): BulkWriteOptions = copy(ordered = ordered)

  /** Sets the bypass document level validation flag.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @mongodb.server.release 3.2
    */
  def withBypassDocumentValidation(bypassDocumentValidation: Boolean): BulkWriteOptions =
    copy(bypassDocumentValidation = Some(bypassDocumentValidation))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): BulkWriteOptions = copy(comment = Some(comment))

  /** Add top-level variables for the operation
    *
    * <p>Allows for improved command readability by separating the variables from the query text.
    * The value of let will be passed to all update and delete, but not insert, commands.
    *
    * @param variables for the operation or null
    * @mongodb.server.release 5.0
    */
  def withVariables(variables: Bson): BulkWriteOptions = copy(variables = Some(variables))

  private[driver] def toJava: JBulkWriteOptions = {
    val options = new JBulkWriteOptions()

    options.ordered(ordered)
    bypassDocumentValidation.foreach(options.bypassDocumentValidation(_))
    comment.foreach(options.comment)
    variables.foreach(options.let)

    options
  }
}
