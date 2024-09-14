package io.github.zeal18.zio.mongodb.driver.query

import com.mongodb.reactivestreams.client.ChangeStreamPublisher
import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonTimestamp
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.driver.model.Collation
import io.github.zeal18.zio.mongodb.driver.model.changestream.ChangeStreamDocument
import io.github.zeal18.zio.mongodb.driver.model.changestream.FullDocument
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.reactivestreams.Publisher
import zio.Task

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/** Observable for change streams.
  *
  * '''Note:''' The `ChangeStreamDocument` class will not be applicable for all change stream outputs.
  * If using custom pipelines that radically change the result, the [[ChangeStreamQuery#withDocumentClass]] method should be used
  * to provide an alternative document format.
  *
  * @param wrapped the underlying java ChangeStreamIterable
  * @tparam TResult The type of the result.
  * @note Requires MongoDB 3.6 or greater
  */
case class ChangeStreamQuery[TResult](private val wrapped: ChangeStreamPublisher[TResult])
    extends Query[ChangeStreamDocument[TResult]] {

  /** Sets the fullDocument value.
    *
    * @param fullDocument the fullDocument
    * @return this
    */
  def fullDocument(fullDocument: FullDocument): ChangeStreamQuery[TResult] = {
    wrapped.fullDocument(fullDocument)
    this
  }

  /** Sets the logical starting point for the new change stream.
    *
    * @param resumeToken the resume token
    * @return this
    */
  def resumeAfter(resumeToken: BsonDocument): ChangeStreamQuery[TResult] = {
    wrapped.resumeAfter(resumeToken)
    this
  }

  /** The change stream will only provide changes that occurred at or after the specified timestamp.
    *
    * Any command run against the server will return an operation time that can be used here.
    * The default value is an operation time obtained from the server before the change stream was created.
    *
    * @param startAtOperationTime the start at operation time
    * @return this
    * @note Requires MongoDB 4.0 or greater
    */
  def startAtOperationTime(startAtOperationTime: BsonTimestamp): ChangeStreamQuery[TResult] = {
    wrapped.startAtOperationTime(startAtOperationTime)
    this
  }

  /** Sets the logical starting point for the new change stream.
    *
    * This will allow users to watch collections that have been dropped and recreated or newly renamed collections without missing
    * any notifications.
    *
    * @param startAfter the resume token
    * @return this
    * @note Requires MongoDB 4.2 or greater
    * @note The server will report an error if both `startAfter` and `resumeAfter` are specified.
    * @see [[https://www.mongodb.com/docs/manual/changeStreams/#change-stream-start-after Change stream start after]]
    */
  def startAfter(startAfter: BsonDocument): ChangeStreamQuery[TResult] = {
    wrapped.startAfter(startAfter)
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): ChangeStreamQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Sets the maximum await execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxAwaitTime(duration: Duration): ChangeStreamQuery[TResult] = {
    wrapped.maxAwaitTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the collation options
    *
    * A null value represents the server default.
    *
    * @param collation the collation options to use
    * @return this
    */
  def collation(collation: Collation): ChangeStreamQuery[TResult] = {
    wrapped.collation(collation.toJava)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 3.6 or greater
    */
  def comment(comment: String): ChangeStreamQuery[TResult] = {
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
  def comment(comment: BsonValue): ChangeStreamQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  // /** Returns an `Observable` containing the results of the change stream based on the document class provided.
  //   *
  //   * @param clazz the class to use for the raw result.
  //   * @tparam T the result type
  //   * @return an Observable
  //   */
  // def withDocumentClass[T](clazz: Class[T]): Observable[T] =
  //   wrapped.withDocumentClass(clazz).toObservable()

  override def runHead: Task[Option[ChangeStreamDocument[TResult]]] = wrapped.first().headOption

  override def run: Publisher[ChangeStreamDocument[TResult]] = wrapped
}
