package io.github.zeal18.zio.mongodb.driver.query

import com.mongodb.CursorType
import com.mongodb.ExplainVerbosity
import com.mongodb.reactivestreams.client.FindPublisher
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import io.github.zeal18.zio.mongodb.driver.model.Collation
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.reactivestreams.Publisher
import zio.Task

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/** Observable interface for Find.
  *
  * @param wrapped the underlying java FindQuery
  * @tparam TResult The type of the result.
  */
case class FindQuery[TResult](private val wrapped: FindPublisher[TResult]) extends Query[TResult] {

  override def runHead: Task[Option[TResult]] = wrapped.first().headOption

  /** Sets the query filter to apply to the query.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/db.collection.find/ Filter]]
    * @param filter the filter, which may be null.
    * @return this
    */
  def filter(filter: Filter): FindQuery[TResult] = {
    wrapped.filter(filter)
    this
  }

  /** Sets the limit to apply.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/cursor.limit/#cursor.limit Limit]]
    * @param limit the limit, which may be null
    * @return this
    */
  def limit(limit: Int): FindQuery[TResult] = {
    wrapped.limit(limit)
    this
  }

  /** Sets the number of documents to skip.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/cursor.skip/#cursor.skip Skip]]
    * @param skip the number of documents to skip
    * @return this
    */
  def skip(skip: Int): FindQuery[TResult] = {
    wrapped.skip(skip)
    this
  }

  /** Sets the maximum execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxTime(duration: Duration): FindQuery[TResult] = {
    wrapped.maxTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** The maximum amount of time for the server to wait on new documents to satisfy a tailable cursor
    * query. This only applies to a TAILABLE_AWAIT cursor. When the cursor is not a TAILABLE_AWAIT cursor,
    * this option is ignored.
    *
    * On servers &gt;= 3.2, this option will be specified on the getMore command as "maxTimeMS". The default
    * is no value: no "maxTimeMS" is sent to the server with the getMore command.
    *
    * On servers &lt; 3.2, this option is ignored, and indicates that the driver should respect the server's default value
    *
    * A zero value will be ignored.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return the maximum await execution time in the given time unit
    */
  def maxAwaitTime(duration: Duration): FindQuery[TResult] = {
    wrapped.maxAwaitTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets a document describing the fields to return for all matching documents.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/db.collection.find/ Projection]]
    * @param projection the project document, which may be null.
    * @return this
    */
  def projection(projection: Projection): FindQuery[TResult] = {
    wrapped.projection(projection)
    this
  }

  /** Sets the sort criteria to apply to the query.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/cursor.sort/ Sort]]
    * @param sort the sort criteria.
    * @return this
    */
  def sort(sorts: Sort*): FindQuery[TResult] = {
    wrapped.sort(Sort.Compound(sorts))
    this
  }

  /** The server normally times out idle cursors after an inactivity period (10 minutes)
    * to prevent excess memory use. Set this option to prevent that.
    *
    * @param noCursorTimeout true if cursor timeout is disabled
    * @return this
    */
  def noCursorTimeout(noCursorTimeout: Boolean): FindQuery[TResult] = {
    wrapped.noCursorTimeout(noCursorTimeout)
    this
  }

  /** Get partial results from a sharded cluster if one or more shards are unreachable (instead of throwing an error).
    *
    * @param partial if partial results for sharded clusters is enabled
    * @return this
    */
  def partial(partial: Boolean): FindQuery[TResult] = {
    wrapped.partial(partial)
    this
  }

  /** Sets the cursor type.
    *
    * @param cursorType the cursor type
    * @return this
    */
  def cursorType(cursorType: CursorType): FindQuery[TResult] = {
    wrapped.cursorType(cursorType)
    this
  }

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @return this
    * @note A null value represents the server default.
    * @note Requires MongoDB 3.4 or greater
    */
  def collation(collation: Collation): FindQuery[TResult] = {
    wrapped.collation(collation.toJava)
    this
  }

  /** Sets the comment to the query. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    */
  def comment(comment: String): FindQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note The comment can be any valid BSON type for server versions 4.4 and above.
    *       Server versions between 3.6 and 4.2 only support
    *       string as comment, and providing a non-string type will result in a server-side error.
    */
  def comment(comment: BsonValue): FindQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Sets the hint for which index to use. A null value means no hint is set.
    *
    * @param hint the hint
    * @return this
    */
  def hint(hint: Hint): FindQuery[TResult] = {
    hint.toBson match {
      case Left(string) => wrapped.hintString(string)
      case Right(bson)  => wrapped.hint(bson)
    }
    this
  }

  /** Add top-level variables to the operation. A null value means no variables are set.
    *
    * Allows for improved command readability by separating the variables from the query text.
    *
    * @param let the top-level variables for the find operation or null
    * @return this
    * @note Requires MongoDB 5.0 or greater
    */
  def let(let: Bson): FindQuery[TResult] = {
    wrapped.let(let)
    this
  }

  /** Sets the exclusive upper bound for a specific index. A null value means no max is set.
    *
    * @param max the max
    * @return this
    */
  def max(max: Bson): FindQuery[TResult] = {
    wrapped.max(max)
    this
  }

  /** Sets the minimum inclusive lower bound for a specific index. A null value means no max is set.
    *
    * @param min the min
    * @return this
    */
  def min(min: Bson): FindQuery[TResult] = {
    wrapped.min(min)
    this
  }

  /** Sets the returnKey. If true the find operation will return only the index keys in the resulting documents.
    *
    * @param returnKey the returnKey
    * @return this
    */
  def returnKey(returnKey: Boolean): FindQuery[TResult] = {
    wrapped.returnKey(returnKey)
    this
  }

  /** Sets the showRecordId. Set to true to add a field `\$recordId` to the returned documents.
    *
    * @param showRecordId the showRecordId
    * @return this
    */
  def showRecordId(showRecordId: Boolean): FindQuery[TResult] = {
    wrapped.showRecordId(showRecordId)
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): FindQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Enables writing to temporary files on the server. When set to true, the server
    * can write temporary data to disk while executing the find operation.
    *
    * <p>This option is sent only if the caller explicitly provides a value. The default
    * is to not send a value. For servers &lt; 3.2, this option is ignored and not sent
    * as allowDiskUse does not exist in the OP_QUERY wire protocol.</p>
    *
    * @param allowDiskUse the allowDiskUse
    * @note Requires MongoDB 4.4 or greater
    */
  def allowDiskUse(allowDiskUse: Boolean): FindQuery[TResult] = {
    wrapped.allowDiskUse(allowDiskUse)
    this
  }

  /** Explain the execution plan for this operation with the server's default verbosity level
    *
    * @tparam ExplainResult The type of the result
    * @return the execution plan
    * @note Requires MongoDB 3.2 or greater
    */
  def explain[ExplainResult]()(implicit ct: ClassTag[ExplainResult]): Task[ExplainResult] =
    wrapped.explain[ExplainResult](ct).head

  /** Explain the execution plan for this operation with the given verbosity level
    *
    * @tparam ExplainResult The type of the result
    * @param verbosity the verbosity of the explanation
    * @return the execution plan
    * @note Requires MongoDB 3.2 or greater
    */
  def explain[ExplainResult](
    verbosity: ExplainVerbosity,
  )(implicit ct: ClassTag[ExplainResult]): Task[ExplainResult] =
    wrapped.explain[ExplainResult](ct, verbosity).head

  override def run: Publisher[TResult] = wrapped
}
