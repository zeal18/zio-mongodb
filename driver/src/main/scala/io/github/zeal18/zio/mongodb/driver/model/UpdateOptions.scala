package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.UpdateOptions as JUpdateOptions
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import org.bson.BsonValue
import org.bson.conversions.Bson

import scala.jdk.CollectionConverters.*

/** The options to apply when updating documents.
  *
  * @mongodb.driver.manual tutorial/modify-documents/ Updates
  * @mongodb.driver.manual reference/operator/update/ Update Operators
  * @mongodb.driver.manual reference/command/update/ Update Command
  */
final case class UpdateOptions(
  upsert: Boolean = false,
  bypassDocumentValidation: Option[Boolean] = None,
  collation: Option[Collation] = None,
  arrayFilters: Option[Seq[Filter]] = None,
  hint: Option[Hint] = None,
  comment: Option[BsonValue] = None,
  variables: Option[Bson] = None,
) {

  /** Set to true if a new document should be inserted if there are no matches to the query filter.
    *
    * @param upsert true if a new document should be inserted if there are no matches to the query filter
    */
  def withUpsert(upsert: Boolean): UpdateOptions = copy(upsert = upsert)

  /** Sets the bypass document level validation flag.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @mongodb.server.release 3.2
    */
  def withBypassDocumentValidation(bypassDocumentValidation: Boolean): UpdateOptions =
    copy(bypassDocumentValidation = Some(bypassDocumentValidation))

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): UpdateOptions = copy(collation = Some(collation))

  /** Sets the array filters option
    *
    * @param arrayFilters the array filters
    * @mongodb.server.release 3.6
    */
  def withArrayFilters(filters: Filter*): UpdateOptions =
    copy(arrayFilters = Some(filters))

  /** Sets the hint for which index to use.
    *
    * @param hint the hint
    */
  def withHint(hint: Hint): UpdateOptions = copy(hint = Some(hint))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): UpdateOptions = copy(comment = Some(comment))

  /** Add top-level variables for the operation
    *
    * <p>Allows for improved command readability by separating the variables from the query text.
    *
    * @param variables for the operation
    * @mongodb.server.release 5.0
    */
  def withVariables(variables: Bson): UpdateOptions = copy(variables = Some(variables))

  private[driver] def toJava: JUpdateOptions = {
    val options = new JUpdateOptions()

    options.upsert(upsert)
    bypassDocumentValidation.foreach(options.bypassDocumentValidation(_))
    collation.foreach(c => options.collation(c.toJava))
    arrayFilters.foreach(f => options.arrayFilters(f.asJava))
    hint.map(_.toBson).foreach {
      case Left(string) => options.hintString(string)
      case Right(bson)  => options.hint(bson)
    }
    comment.foreach(options.comment)
    variables.foreach(options.let)

    options
  }
}
