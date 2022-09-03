package io.github.zeal18.zio.mongodb.driver.model

import java.time.Duration

import com.mongodb.client.model.FindOneAndReplaceOptions as JFindOneAndReplaceOptions
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonValue
import org.bson.conversions.Bson

/** The options to apply to an operation that atomically finds a document and replaces it.
  *
  * @mongodb.driver.manual reference/command/findAndModify/
  */
final case class FindOneAndReplaceOptions(
  projection: Option[Projection] = None,
  sort: Option[Sort] = None,
  upsert: Boolean = false,
  returnDocument: ReturnDocument = ReturnDocument.Before,
  maxTime: Option[Duration] = None,
  bypassDocumentValidation: Option[Boolean] = None,
  collation: Option[Collation] = None,
  hint: Option[Hint] = None,
  comment: Option[BsonValue] = None,
  variables: Option[Bson] = None,
) {

  /** Sets a document describing the fields to return for all matching documents.
    *
    * @param projection the project document
    * @mongodb.driver.manual tutorial/project-fields-from-query-results Projection
    */
  def withProjection(projection: Projection): FindOneAndReplaceOptions =
    copy(projection = Some(projection))

  /** Sets the sort criteria to apply to the query.
    *
    * @param sort the sort criteria
    * @mongodb.driver.manual reference/method/cursor.sort/ Sort
    */
  def withSort(sort: Sort): FindOneAndReplaceOptions = copy(sort = Some(sort))

  /** Set to true if a new document should be inserted if there are no matches to the query filter.
    *
    * @param upsert true if a new document should be inserted if there are no matches to the query filter
    */
  def withUpsert(upsert: Boolean): FindOneAndReplaceOptions = copy(upsert = upsert)

  /** Set whether to return the document before it was replaced or after
    *
    * @param returnDocument set whether to return the document before it was replaced or after
    */
  def withReturnDocument(returnDocument: ReturnDocument): FindOneAndReplaceOptions =
    copy(returnDocument = returnDocument)

  /** Sets the maximum execution time on the server for this operation.
    *
    * @param maxTime  the max time
    */
  def withMaxTime(maxTime: Duration): FindOneAndReplaceOptions = copy(maxTime = Some(maxTime))

  /** Sets the bypass document level validation flag.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @mongodb.server.release 3.2
    */
  def withBypassDocumentValidation(bypassDocumentValidation: Boolean): FindOneAndReplaceOptions =
    copy(bypassDocumentValidation = Some(bypassDocumentValidation))

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): FindOneAndReplaceOptions =
    copy(collation = Some(collation))

  /** Sets the hint for which index to use.
    *
    * @param hint the hint
    */
  def withHint(hint: Hint): FindOneAndReplaceOptions = copy(hint = Some(hint))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): FindOneAndReplaceOptions = copy(comment = Some(comment))

  /** Add top-level variables for the operation
    *
    * <p>Allows for improved command readability by separating the variables from the query text.
    *
    * @param variables for the operation or null
    * @mongodb.server.release 5.0
    */
  def withVariables(variables: Bson): FindOneAndReplaceOptions = copy(variables = Some(variables))

  private[driver] def toJava: JFindOneAndReplaceOptions = {
    val options = new JFindOneAndReplaceOptions()

    projection.foreach(p => options.projection(p.toBson))
    sort.foreach(s => options.sort(s.toBson))
    options.upsert(upsert)
    options.returnDocument(returnDocument.toJava)
    maxTime.foreach(t => options.maxTime(t.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS))
    bypassDocumentValidation.foreach(options.bypassDocumentValidation(_))
    collation.foreach(c => options.collation(c.toJava))
    hint.map(_.toBson).foreach {
      case Left(string) => options.hintString(string)
      case Right(bson)  => options.hint(bson)
    }
    comment.foreach(options.comment)
    variables.foreach(options.let)

    options
  }
}
