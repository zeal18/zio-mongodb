package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.CountOptions as JCountOptions
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import org.bson.BsonValue

import java.time.Duration

/** The options for a count operation.
  *
  * @mongodb.driver.manual reference/command/count/ Count
  */
final case class CountOptions(
  hint: Option[Hint] = None,
  limit: Option[Int] = None,
  skip: Option[Int] = None,
  maxTime: Option[Duration] = None,
  collation: Option[Collation] = None,
  comment: Option[BsonValue] = None,
) {

  /** Sets the hint to apply.
    *
    * @param hint a document describing the index which should be used for this operation.
    */
  def withHint(hint: Hint): CountOptions = copy(hint = Some(hint))

  /** Sets the limit to apply.
    *
    * @param limit the limit
    * @mongodb.driver.manual reference/method/cursor.limit/#cursor.limit Limit
    */
  def withLimit(limit: Int): CountOptions = copy(limit = Some(limit))

  /** Sets the number of documents to skip.
    *
    * @param skip the number of documents to skip
    * @mongodb.driver.manual reference/method/cursor.skip/#cursor.skip Skip
    */
  def withSkip(skip: Int): CountOptions = copy(skip = Some(skip))

  /** Sets the maximum execution time on the server for this operation.
    *
    * @param maxTime  the max time
    */
  def withMaxTime(maxTime: Duration): CountOptions = copy(maxTime = Some(maxTime))

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): CountOptions = copy(collation = Some(collation))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): CountOptions = copy(comment = Some(comment))

  private[driver] def toJava: JCountOptions = {
    val options = new JCountOptions()
    hint.map(_.toBson).foreach {
      case Left(string) => options.hintString(string)
      case Right(bson)  => options.hint(bson)
    }
    limit.foreach(options.limit)
    skip.foreach(options.skip)
    maxTime.foreach(t => options.maxTime(t.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS))
    collation.foreach(c => options.collation(c.toJava))
    comment.foreach(options.comment)

    options
  }
}
