package io.github.zeal18.zio.mongodb.driver.query

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import com.mongodb.reactivestreams.client.ListCollectionsPublisher
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.reactivestreams.Publisher
import zio.Task

/** Observable interface for ListCollections
  *
  * @param wrapped the underlying java ListCollectionsQuery
  * @tparam TResult The type of the result.
  */
case class ListCollectionsQuery[TResult](wrapped: ListCollectionsPublisher[TResult])
    extends Query[TResult] {

  /** Sets the query filter to apply to the query.
    *
    * [[https://www.mongodb.com/docs/manual/reference/method/db.collection.find/ Filter]]
    * @param filter the filter, which may be null.
    * @return this
    */
  def filter(filter: Bson): ListCollectionsQuery[TResult] = {
    wrapped.filter(filter)
    this
  }

  /** Sets the maximum execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxTime(duration: Duration): ListCollectionsQuery[TResult] = {
    wrapped.maxTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): ListCollectionsQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: String): ListCollectionsQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 4.4 or greater
    */
  def comment(comment: BsonValue): ListCollectionsQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  override def runHead: Task[Option[TResult]] = wrapped.first().headOption

  override def run: Publisher[TResult] = wrapped
}
