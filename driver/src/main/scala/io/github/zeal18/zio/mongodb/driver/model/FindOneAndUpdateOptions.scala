package io.github.zeal18.zio.mongodb.driver.model

import java.time.Duration

import scala.jdk.CollectionConverters.*

import com.mongodb.client.model.FindOneAndUpdateOptions as JFindOneAndUpdateOptions
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonValue
import org.bson.conversions.Bson

/** The options to apply to an operation that atomically finds a document and updates it.
  *
  * @mongodb.driver.manual reference/command/findAndModify/
  */
final case class FindOneAndUpdateOptions(
  projection: Option[Projection] = None,
  sort: Option[Sort] = None,
  upsert: Boolean = false,
  returnDocument: ReturnDocument = ReturnDocument.Before,
  maxTime: Option[Duration] = None,
  bypassDocumentValidation: Option[Boolean] = None,
  collation: Option[Collation] = None,
  arrayFilters: Option[Seq[Filter]] = None,
  hint: Option[Hint] = None,
  comment: Option[BsonValue] = None,
  variables: Option[Bson] = None,
) {

  /** Sets a document describing the fields to return for all matching documents.
    *
    * @param projection the project document
    * @mongodb.driver.manual tutorial/project-fields-from-query-results Projection
    */
  def withProjection(projection: Projection): FindOneAndUpdateOptions =
    copy(projection = Some(projection))

  /** Sets the sort criteria to apply to the query.
    *
    * @param sort the sort criteria
    * @mongodb.driver.manual reference/method/cursor.sort/ Sort
    */
  def withSort(sort: Sort): FindOneAndUpdateOptions = copy(sort = Some(sort))

  /** Set to true if a new document should be inserted if there are no matches to the query filter.
    *
    * @param upsert true if a new document should be inserted if there are no matches to the query filter
    */
  def withUpsert(upsert: Boolean): FindOneAndUpdateOptions = copy(upsert = upsert)

  /** Set whether to return the document before it was updated / inserted or after
    *
    * @param returnDocument set whether to return the document before it was updated / inserted or after
    */
  def withReturnDocument(returnDocument: ReturnDocument): FindOneAndUpdateOptions =
    copy(returnDocument = returnDocument)

  /** Sets the maximum execution time on the server for this operation.
    *
    * @param maxTime  the max time
    */
  def withMaxTime(maxTime: Duration): FindOneAndUpdateOptions = copy(maxTime = Some(maxTime))

  /** Sets the bypass document level validation flag.
    *
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @mongodb.server.release 3.2
    */
  def withBypassDocumentValidation(bypassDocumentValidation: Boolean): FindOneAndUpdateOptions =
    copy(bypassDocumentValidation = Some(bypassDocumentValidation))

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): FindOneAndUpdateOptions =
    copy(collation = Some(collation))

  /** Sets the array filters option
    *
    * @param arrayFilters the array filters
    * @mongodb.server.release 3.6
    */
  def withArrayFilters(arrayFilters: Seq[Filter]): FindOneAndUpdateOptions =
    copy(arrayFilters = Some(arrayFilters))

  /** Sets the hint for which index to use.
    *
    * @param hint the hint
    */
  def withHint(hint: Hint): FindOneAndUpdateOptions = copy(hint = Some(hint))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): FindOneAndUpdateOptions = copy(comment = Some(comment))

  /** Add top-level variables for the operation
    *
    * <p>Allows for improved command readability by separating the variables from the query text.
    *
    * @param variables for the operation
    * @mongodb.server.release 5.0
    */
  def withVariables(variables: Bson): FindOneAndUpdateOptions = copy(variables = Some(variables))

  private[driver] def toJava: JFindOneAndUpdateOptions = {
    val options = new JFindOneAndUpdateOptions()

    projection.foreach(p => options.projection(p))
    sort.foreach(s => options.sort(s))
    options.upsert(upsert)
    options.returnDocument(returnDocument.toJava)
    maxTime.foreach(t => options.maxTime(t.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS))
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
