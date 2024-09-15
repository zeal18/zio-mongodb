package io.github.zeal18.zio.mongodb.driver.query

import com.mongodb.reactivestreams.client.DistinctPublisher
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.model.Collation
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.reactivestreams.Publisher
import zio.Task
import zio.Trace

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/** Observable for distinct
  *
  * @param wrapped the underlying java DistinctQuery
  * @tparam TResult The type of the result.
  */
case class DistinctQuery[TResult](private val wrapped: DistinctPublisher[TResult]) extends Query[TResult] {

  /** Sets the query filter to apply to the query.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/db.collection.find/ Filter]]
    * @param filter the filter, which may be null.
    * @return this
    */
  def filter(filter: Filter): DistinctQuery[TResult] = {
    wrapped.filter(filter)
    this
  }

  /** Sets the maximum execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxTime(duration: Duration): DistinctQuery[TResult] = {
    wrapped.maxTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @return this
    * @note A null value represents the server default.
    * @note Requires MongoDB 3.4 or greater
    */
  def collation(collation: Collation): DistinctQuery[TResult] = {
    wrapped.collation(collation.toJava)
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): DistinctQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: String): DistinctQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: BsonValue): DistinctQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  override def runHead(implicit trace: Trace): Task[Option[TResult]] = wrapped.first().headOption

  override def run: Publisher[TResult] = wrapped
}
