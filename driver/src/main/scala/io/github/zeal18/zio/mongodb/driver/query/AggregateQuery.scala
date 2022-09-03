package io.github.zeal18.zio.mongodb.driver.query

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

import com.mongodb.ExplainVerbosity
import com.mongodb.reactivestreams.client.AggregatePublisher
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import io.github.zeal18.zio.mongodb.driver.model.Collation
import zio.Task
import zio.stream.ZStream

/** Observable for aggregate
  *
  * @param wrapped the underlying java AggregateQuery
  * @tparam TResult The type of the result.
  */
case class AggregateQuery[TResult](private val wrapped: AggregatePublisher[TResult])
    extends Query[TResult] {

  /** Enables writing to temporary files. A null value indicates that it's unspecified.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/aggregate/ Aggregation]]
    *
    * @param allowDiskUse true if writing to temporary files is enabled
    * @return this
    */
  def allowDiskUse(allowDiskUse: Boolean): AggregateQuery[TResult] = {
    wrapped.allowDiskUse(allowDiskUse)
    this
  }

  /** Sets the maximum execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    */
  def maxTime(duration: Duration): AggregateQuery[TResult] = {
    wrapped.maxTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the maximum await execution time on the server for this operation.
    *
    * [[https://www.mongodb.com/docs/manual/reference/operator/meta/maxTimeMS/ Max Time]]
    * @param duration the duration
    * @return this
    * @note Requires MongoDB 3.6 or greater
    */
  def maxAwaitTime(duration: Duration): AggregateQuery[TResult] = {
    wrapped.maxAwaitTime(duration.toMillis, TimeUnit.MILLISECONDS)
    this
  }

  /** Sets the bypass document level validation flag.
    *
    * '''Note:''': This only applies when an `\$out` stage is specified.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/aggregate/ Aggregation]]
    * @note Requires MongoDB 3.2 or greater
    * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
    * @return this
    */
  def bypassDocumentValidation(bypassDocumentValidation: Boolean): AggregateQuery[TResult] = {
    wrapped.bypassDocumentValidation(bypassDocumentValidation)
    this
  }

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @return this
    * @note A null value represents the server default.
    * @note Requires MongoDB 3.4 or greater
    */
  def collation(collation: Collation): AggregateQuery[TResult] = {
    wrapped.collation(collation.toJava)
    this
  }

  /** Sets the comment for this operation. A null value means no comment is set.
    *
    * @param comment the comment
    * @return this
    * @note Requires MongoDB 3.6 or greater
    */
  def comment(comment: String): AggregateQuery[TResult] = {
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
  def comment(comment: BsonValue): AggregateQuery[TResult] = {
    wrapped.comment(comment)
    this
  }

  /** Add top-level variables to the aggregation.
    *
    * For MongoDB 5.0+, the aggregate command accepts a "let" option. This option is a document consisting of zero or more
    * fields representing variables that are accessible to the aggregation pipeline.  The key is the name of the variable and the value is
    * a constant in the aggregate expression language. Each parameter name is then usable to access the value of the corresponding
    * expression with the "$$" syntax within aggregate expression contexts which may require the use of '\$expr' or a pipeline.
    *
    * @param variables the variables
    * @return this
    * @note Requires MongoDB 5.0 or greater
    */
  def let(variables: Bson): AggregateQuery[TResult] = {
    wrapped.let(variables)
    this
  }

  /** Sets the hint for which index to use. A null value means no hint is set.
    *
    * @param hint the hint
    * @return this
    * @note Requires MongoDB 3.6 or greater
    */
  def hint(hint: Hint): AggregateQuery[TResult] = {
    hint.toBson match {
      case Left(string) => wrapped.hintString(string)
      case Right(bson)  => wrapped.hint(bson)
    }
    this
  }

  /** Sets the number of documents to return per batch.
    *
    * @param batchSize the batch size
    * @return this
    */
  def batchSize(batchSize: Int): AggregateQuery[TResult] = {
    wrapped.batchSize(batchSize)
    this
  }

  /** Aggregates documents according to the specified aggregation pipeline, which must end with a `\$out` stage.
    *
    * [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    * @return an empty Observable that indicates when the operation has completed
    */
  def toCollection(): Task[Unit] = wrapped.toCollection().stream.runDrain

  /** Helper to return a single observable limited to the first result.
    *
    * @return a single observable which will the first result.
    */
  def first(): Task[Option[TResult]] = wrapped.first().getOneOpt

  /** Explain the execution plan for this operation with the server's default verbosity level
    *
    * @tparam ExplainResult The type of the result
    * @return the execution plan
    * @note Requires MongoDB 3.6 or greater
    */
  def explain[ExplainResult]()(implicit ct: ClassTag[ExplainResult]): Task[ExplainResult] =
    wrapped.explain[ExplainResult](ct).getOne

  /** Explain the execution plan for this operation with the given verbosity level
    *
    * @tparam ExplainResult The type of the result
    * @param verbosity the verbosity of the explanation
    * @return the execution plan
    * @note Requires MongoDB 3.6 or greater
    */
  def explain[ExplainResult](
    verbosity: ExplainVerbosity,
  )(implicit ct: ClassTag[ExplainResult]): Task[ExplainResult] =
    wrapped.explain[ExplainResult](ct, verbosity).getOne

  override def execute: ZStream[Any, Throwable, TResult] = wrapped.stream
}
