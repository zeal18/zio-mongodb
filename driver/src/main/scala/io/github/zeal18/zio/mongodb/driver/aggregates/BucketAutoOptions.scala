package io.github.zeal18.zio.mongodb.driver.aggregates

import scala.jdk.CollectionConverters.*

import com.mongodb.client.model.BucketAutoOptions as JBucketAutoOptions
import io.github.zeal18.zio.mongodb.driver.model.BsonField

/** The options for a $bucketAuto aggregation pipeline stage
  *
  * @mongodb.driver.manual reference/operator/aggregation/bucketAuto/ $bucketAuto
  * @mongodb.server.release 3.4
  */
final case class BucketAutoOptions(
  output: List[BsonField] = Nil,
  granularity: Option[BucketGranularity] = None,
) {

  /** The definition of the output document in each bucket
    *
    * @param output the output document definition
    */
  def withOutput(output: List[BsonField]): BucketAutoOptions = copy(output = output)

  /** Specifies the granularity of the bucket definitions.
    *
    * @param granularity the granularity of the bucket definitions
    * @see <a href="https://en.wikipedia.org/wiki/Preferred_number">Preferred numbers</a>
    * @see BucketGranularity
    */
  def withGranularity(granularity: BucketGranularity): BucketAutoOptions =
    copy(granularity = Some(granularity))

  private[driver] def toJava: JBucketAutoOptions = {
    val options = new JBucketAutoOptions()

    options.output(output.asJava)
    granularity.foreach(g => options.granularity(g.toJava))

    options
  }
}
