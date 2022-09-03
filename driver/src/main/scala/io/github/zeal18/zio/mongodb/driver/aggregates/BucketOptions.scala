package io.github.zeal18.zio.mongodb.driver.aggregates

import scala.jdk.CollectionConverters.*

import com.mongodb.client.model.BucketOptions as JBucketOptions
import io.github.zeal18.zio.mongodb.driver.model.BsonField

/** The options for a $bucket aggregation pipeline stage
  *
  * @mongodb.driver.manual reference/operator/aggregation/bucketAuto/ $bucket
  * @mongodb.server.release 3.4
  */
final case class BucketOptions(
  defaultBucket: Option[String] = None,
  output: List[BsonField] = Nil,
) {

  /** The name of the default bucket for values outside the defined buckets
    *
    * @param name the bucket value
    */
  def withDefaultBucket(defaultBucket: String): BucketOptions =
    copy(defaultBucket = Some(defaultBucket))

  /** The definition of the output document in each bucket
    *
    * @param output the output document definition
    */
  def withOutput(output: List[BsonField]): BucketOptions = copy(output = output)

  private[driver] def toJava: JBucketOptions = {
    val options = new JBucketOptions()

    defaultBucket.foreach(options.defaultBucket)
    options.output(output.asJava)

    options
  }
}
