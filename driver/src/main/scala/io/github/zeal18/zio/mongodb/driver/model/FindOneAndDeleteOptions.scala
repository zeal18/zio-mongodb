package io.github.zeal18.zio.mongodb.driver.model

import java.time.Duration

import com.mongodb.client.model.FindOneAndDeleteOptions as JFindOneAndDeleteOptions
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonValue
import org.bson.conversions.Bson

/** The options to apply to an operation that atomically finds a document and deletes it.
  *
  * @mongodb.driver.manual reference/command/findAndModify/
  */
final case class FindOneAndDeleteOptions(
  projection: Option[Projection] = None,
  sort: Option[Sort] = None,
  maxTime: Option[Duration] = None,
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
  def withProjection(projection: Projection): FindOneAndDeleteOptions =
    copy(projection = Some(projection))

  /** Sets the sort criteria to apply to the query.
    *
    * @param sort the sort criteria
    * @mongodb.driver.manual reference/method/cursor.sort/ Sort
    */
  def withSort(sort: Sort): FindOneAndDeleteOptions = copy(sort = Some(sort))

  /** Sets the maximum execution time on the server for this operation.
    *
    * @param maxTime  the max time
    */
  def withMaxTime(maxTime: Duration): FindOneAndDeleteOptions = copy(maxTime = Some(maxTime))

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): FindOneAndDeleteOptions =
    copy(collation = Some(collation))

  /** Sets the hint to apply.
    *
    * @param hint a document describing the index which should be used for this operation.
    * @mongodb.server.release 4.4
    */
  def withHint(hint: Hint): FindOneAndDeleteOptions = copy(hint = Some(hint))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): FindOneAndDeleteOptions = copy(comment = Some(comment))

  /** Add top-level variables for the operation
    *
    * <p>Allows for improved command readability by separating the variables from the query text.
    *
    * @param variables for the operation
    * @mongodb.server.release 5.0
    */
  def withVariables(variables: Bson): FindOneAndDeleteOptions = copy(variables = Some(variables))

  private[driver] def toJava: JFindOneAndDeleteOptions = {
    val options = new JFindOneAndDeleteOptions()

    projection.foreach(p => options.projection(p))
    sort.foreach(s => options.sort(s))
    maxTime.foreach(t => options.maxTime(t.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS))
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
