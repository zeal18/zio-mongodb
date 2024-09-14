package io.github.zeal18.zio.mongodb.driver.query

import com.mongodb.reactivestreams.client.ListIndexesPublisher
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.reactivestreams.Publisher
import zio.Task

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/** Observable interface for ListIndexes.
  *
  * @param wrapped the underlying java ListIndexesQuery
  * @tparam TResult The type of the result.
  */
case class ListIndexesQuery[TResult](wrapped: ListIndexesPublisher[TResult]) extends Query[TResult] {

  /** Sets the maximum execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxTime(duration: Duration): ListIndexesQuery[TResult] = {
    wrapped.maxTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): ListIndexesQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: String): ListIndexesQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: BsonValue): ListIndexesQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  override def runHead: Task[Option[TResult]] = wrapped.first().headOption

  override def run: Publisher[TResult] = wrapped
}
