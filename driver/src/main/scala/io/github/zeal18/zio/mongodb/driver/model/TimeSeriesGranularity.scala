package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.TimeSeriesGranularity as JTimeSeriesGranularity

/** An enumeration of time-series data granularity.
  * <p>
  * It describes the units one would use to describe the expected interval between subsequent measurements for a time-series.
  * </p>
  * @see TimeSeriesOptions
  * @see CreateCollectionOptions
  */
sealed trait TimeSeriesGranularity {
  private[driver] def toJava: JTimeSeriesGranularity = this match {
    case TimeSeriesGranularity.Seconds => JTimeSeriesGranularity.SECONDS
    case TimeSeriesGranularity.Minutes => JTimeSeriesGranularity.MINUTES
    case TimeSeriesGranularity.Hours   => JTimeSeriesGranularity.HOURS
  }
}

object TimeSeriesGranularity {

  /** Seconds-level granularity.
    *
    *  <p>If granularity of a time-series collection is unspecified, this is the default value.</p>
    */
  case object Seconds extends TimeSeriesGranularity

  /** Minutes-level granularity.
    */
  case object Minutes extends TimeSeriesGranularity

  /** Hours-level granularity.
    */
  case object Hours extends TimeSeriesGranularity
}
