package io.github.zeal18.zio.mongodb.driver.aggregates

import com.mongodb.client.model.BucketGranularity as JBucketGranularity

/** Granularity values for automatic bucketing.
  *
  * @mongodb.driver.manual reference/operator/aggregation/bucketAuto/ $bucketAuto
  * @mongodb.server.release 3.4
  * @see <a href="https://en.wikipedia.org/wiki/Preferred_number">Preferred numbers</a>
  * @since 3.4
  */
sealed abstract class BucketGranularity(val name: String) {
  private[driver] def toJava: JBucketGranularity = JBucketGranularity.valueOf(name)
}

object BucketGranularity {
  case object R5        extends BucketGranularity("R5")
  case object R10       extends BucketGranularity("R10")
  case object R20       extends BucketGranularity("R20")
  case object R40       extends BucketGranularity("R40")
  case object R80       extends BucketGranularity("R80")
  case object Series125 extends BucketGranularity("1-2-5")
  case object E6        extends BucketGranularity("E6")
  case object E12       extends BucketGranularity("E12")
  case object E24       extends BucketGranularity("E24")
  case object E48       extends BucketGranularity("E48")
  case object E96       extends BucketGranularity("E96")
  case object E192      extends BucketGranularity("E192")
  case object PowersOf2 extends BucketGranularity("POWERSOF2")

  def fromString(str: String): Either[String, BucketGranularity] =
    str match {
      case "R5"        => Right(R5)
      case "R10"       => Right(R10)
      case "R20"       => Right(R20)
      case "R40"       => Right(R40)
      case "R80"       => Right(R80)
      case "1-2-5"     => Right(Series125)
      case "E6"        => Right(E6)
      case "E12"       => Right(E12)
      case "E24"       => Right(E24)
      case "E48"       => Right(E48)
      case "E96"       => Right(E96)
      case "E192"      => Right(E192)
      case "POWERSOF2" => Right(PowersOf2)
      case _           => Left(s"No Granularity exists for the value $str")
    }
}
