package io.github.zeal18.zio.mongodb.driver.query

import com.mongodb.reactivestreams.client.ListDatabasesPublisher
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.reactivestreams.Publisher
import zio.Task
import zio.Trace

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/** Observable interface for ListDatabases.
  *
  * @param wrapped the underlying java ListDatabasesQuery
  * @tparam TResult The type of the result.
  */
case class ListDatabasesQuery[TResult](wrapped: ListDatabasesPublisher[TResult]) extends Query[TResult] {

  /** Sets the maximum execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxTime(duration: Duration): ListDatabasesQuery[TResult] = {
    wrapped.maxTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the query filter to apply to the returned database names.
    *
    * @param filter the filter, which may be null.
    * @return this
    * @note Requires MongoDB 3.4.2 or greater
    */
  def filter(filter: Bson): ListDatabasesQuery[TResult] = {
    wrapped.filter(filter)
    this
  }

  /** Sets the nameOnly flag that indicates whether the command should return just the database names or return the database names and
    * size information.
    *
    * @param nameOnly the nameOnly flag, which may be null
    * @return this
    * @note Requires MongoDB 3.4.3 or greater
    */
  def nameOnly(nameOnly: Boolean): ListDatabasesQuery[TResult] = {
    wrapped.nameOnly(nameOnly)
    this
  }

  /** Sets the authorizedDatabasesOnly flag that indicates whether the command should return just the databases which the user
    * is authorized to see.
    *
    * @param authorizedDatabasesOnly the authorizedDatabasesOnly flag, which may be null
    * @return this
    * @note Requires MongoDB 4.0.5 or greater
    */
  def authorizedDatabasesOnly(authorizedDatabasesOnly: Boolean): ListDatabasesQuery[TResult] = {
    wrapped.authorizedDatabasesOnly(authorizedDatabasesOnly)
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): ListDatabasesQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: String): ListDatabasesQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: BsonValue): ListDatabasesQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  override def runHead(implicit trace: Trace): Task[Option[TResult]] = wrapped.first().headOption

  override def run: Publisher[TResult] = wrapped
}
