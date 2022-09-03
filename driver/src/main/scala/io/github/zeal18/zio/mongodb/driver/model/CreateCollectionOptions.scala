package io.github.zeal18.zio.mongodb.driver.model

import java.util.concurrent.TimeUnit

import com.mongodb.client.model.IndexOptionDefaults
import com.mongodb.client.model.CreateCollectionOptions as JCreateCollectionOptions
import io.github.zeal18.zio.mongodb.driver.model.ValidationOptions
import org.bson.conversions.Bson

/** Options for creating a collection
  *
  * @mongodb.driver.manual reference/method/db.createCollection/ Create Collection
  * @mongodb.driver.manual core/timeseries-collections/ Time-series collections
  */
final case class CreateCollectionOptions(
  maxDocuments: Option[Long] = None,
  capped: Option[Boolean] = None,
  sizeInBytes: Option[Long] = None,
  storageEngineOptions: Option[Bson] = None,
  indexOptionDefaults: IndexOptionDefaults = new IndexOptionDefaults(),
  validationOptions: ValidationOptions = ValidationOptions(),
  collation: Option[Collation] = None,
  expireAfterSeconds: Option[Long] = None,
  timeSeriesOptions: Option[TimeSeriesOptions] = None,
) {

  /** Sets the maximum number of documents allowed in a capped collection.
    *
    * @param maxDocuments the maximum number of documents allowed in capped collection
    */
  def withMaxDocuments(maxDocuments: Long): CreateCollectionOptions =
    copy(maxDocuments = Some(maxDocuments))

  /** sets whether the collection is capped.
    *
    * @param capped whether the collection is capped
    */
  def withCapped(capped: Boolean): CreateCollectionOptions = copy(capped = Some(capped))

  /** Gets the maximum size of in bytes of a capped collection.
    *
    * @param sizeInBytes the maximum size of a capped collection.
    */
  def withSizeInBytes(sizeInBytes: Long): CreateCollectionOptions =
    copy(sizeInBytes = Some(sizeInBytes))

  /** Sets the storage engine options document defaults for the collection
    *
    * @param storageEngineOptions the storage engine options
    * @mongodb.server.release 3.0
    */
  def withStorageEngineOptions(storageEngineOptions: Bson): CreateCollectionOptions =
    copy(storageEngineOptions = Some(storageEngineOptions))

  /** Sets the index option defaults for the collection.
    *
    * @param indexOptionDefaults the index option defaults
    * @mongodb.server.release 3.2
    */
  def withIndexOptionDefaults(indexOptionDefaults: IndexOptionDefaults): CreateCollectionOptions =
    copy(indexOptionDefaults = indexOptionDefaults)

  /** Sets the validation options for documents being inserted or updated in a collection
    *
    * @param validationOptions the validation options
    * @mongodb.server.release 3.2
    */
  def withValidationOptions(validationOptions: ValidationOptions): CreateCollectionOptions =
    copy(validationOptions = validationOptions)

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): CreateCollectionOptions =
    copy(collation = Some(collation))

  /** Sets the expire-after option.
    *
    * A duration indicating after how long old time-series data should be deleted.
    * Currently applies only to time-series collections, so if this value is set then so must the time-series options
    *
    * @param expireAfterSeconds the expire-after duration in seconds.
    * @see #timeSeriesOptions(TimeSeriesOptions)
    * @mongodb.driver.manual core/timeseries-collections/ Time-series collections
    */
  def withExpireAfterSeconds(expireAfterSeconds: Long): CreateCollectionOptions =
    copy(expireAfterSeconds = Some(expireAfterSeconds))

  /** Sets the time-series collection options.
    *
    * @param timeSeriesOptions the time-series options
    * @mongodb.driver.manual core/timeseries-collections/ Time-series collections
    */
  def withTimeSeriesOptions(timeSeriesOptions: TimeSeriesOptions): CreateCollectionOptions =
    copy(timeSeriesOptions = Some(timeSeriesOptions))

  private[driver] def toJava: JCreateCollectionOptions = {
    val options = new JCreateCollectionOptions()

    maxDocuments.foreach(options.maxDocuments)
    capped.foreach(options.capped)
    sizeInBytes.foreach(options.sizeInBytes)
    storageEngineOptions.foreach(options.storageEngineOptions)
    options.indexOptionDefaults(indexOptionDefaults)
    options.validationOptions(validationOptions.toJava)
    collation.foreach(c => options.collation(c.toJava))
    expireAfterSeconds.foreach(options.expireAfter(_, TimeUnit.SECONDS))
    timeSeriesOptions.foreach(o => options.timeSeriesOptions(o.toJava))

    options
  }
}
