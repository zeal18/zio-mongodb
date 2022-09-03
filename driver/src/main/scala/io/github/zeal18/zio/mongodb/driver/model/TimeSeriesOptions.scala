package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.TimeSeriesOptions as JTimeSeriesOptions

/** Options related to the creation of time-series collections.
  *
  * @param timeField the name of the top-level field to be used for time. Inserted documents must have this field, and the field must be
  *                 of the BSON datetime type.
  *
  * @see CreateCollectionOptions
  * @mongodb.driver.manual core/timeseries-collections/ Time-series collections
  */
final case class TimeSeriesOptions(
  timeField: String,
  metaField: Option[String] = None,
  granularity: Option[TimeSeriesGranularity] = None,
) {

  /** Sets the name of the meta field.
    * <p>
    *  The name of the field which contains metadata in each time series document. The metadata in the specified field should be data
    *  that is used to label a unique series of documents. The metadata should rarely, if ever, change.  This field is used to group
    *  related data and may be of any BSON type, except for array. This name may not be the same as the {@code timeField} or "_id".
    * </p>
    * @param metaField the name of the meta field
    */
  def withMetaField(metaField: String): TimeSeriesOptions = copy(metaField = Some(metaField))

  /** Sets the granularity of the time-series data.
    * <p>
    * The default value is {@link TimeSeriesGranularity#SECONDS}.
    * </p>
    *
    * @param granularity the time-series granularity
    */
  def withGranularity(granularity: TimeSeriesGranularity): TimeSeriesOptions =
    copy(granularity = Some(granularity))

  private[driver] def toJava: JTimeSeriesOptions = {
    val options = new JTimeSeriesOptions(timeField)

    metaField.foreach(options.metaField)
    granularity.foreach(g => options.granularity(g.toJava))

    options
  }
}
