package io.github.zeal18.zio.mongodb.driver

import java.util

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoCollection as JMongoCollection
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.MongoNamespace
import io.github.zeal18.zio.mongodb.driver.ReadConcern
import io.github.zeal18.zio.mongodb.driver.ReadPreference
import io.github.zeal18.zio.mongodb.driver.WriteConcern
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.indexes.CreateIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.DropIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.Index
import io.github.zeal18.zio.mongodb.driver.indexes.IndexKey
import io.github.zeal18.zio.mongodb.driver.indexes.IndexOptions
import io.github.zeal18.zio.mongodb.driver.model.*
import io.github.zeal18.zio.mongodb.driver.query.*
import io.github.zeal18.zio.mongodb.driver.result.*
import io.github.zeal18.zio.mongodb.driver.updates.Update
import org.bson.codecs.configuration.CodecRegistries.*
import org.bson.codecs.configuration.CodecRegistry
import zio.Chunk
import zio.Task

/** The MongoCollection representation.
  *
  * @param wrapped the underlying java MongoCollection
  * @tparam TResult The type that this collection will encode documents from and decode documents to.
  */
case class MongoCollection[TResult](private val wrapped: JMongoCollection[TResult]) {

  /** Gets the namespace of this collection.
    *
    * @return the namespace
    */
  lazy val namespace: MongoNamespace = wrapped.getNamespace

  /** Get the default class to cast any documents returned from the database into.
    *
    * @return the default class to cast any documents into
    */
  lazy val documentClass: Class[TResult] = wrapped.getDocumentClass

  /** Get the codec registry for the MongoDatabase.
    *
    * @return the { @link org.bson.codecs.configuration.CodecRegistry}
    */
  lazy val codecRegistry: CodecRegistry = wrapped.getCodecRegistry

  /** Get the read preference for the MongoDatabase.
    *
    * @return the { @link com.mongodb.ReadPreference}
    */
  lazy val readPreference: ReadPreference = wrapped.getReadPreference

  /** Get the write concern for the MongoDatabase.
    *
    * @return the { @link com.mongodb.WriteConcern}
    */
  lazy val writeConcern: WriteConcern = wrapped.getWriteConcern

  /** Get the read concern for the MongoDatabase.
    *
    * @return the [[ReadConcern]]
    */
  lazy val readConcern: ReadConcern = wrapped.getReadConcern

  /** Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
    *
    * @tparam C   The type that the new collection will encode documents from and decode documents to
    * @return a new MongoCollection instance with the different default class
    */
  def withDocumentClass[C](implicit codec: Codec[C]): MongoCollection[C] =
    MongoCollection(
      wrapped
        .withDocumentClass(codec.getEncoderClass())
        .withCodecRegistry(fromRegistries(fromCodecs(codec), MongoClient.DEFAULT_CODEC_REGISTRY)),
    )

  /** Create a new MongoCollection instance with a different read preference.
    *
    * @param readPreference the new { @link com.mongodb.ReadPreference} for the collection
    * @return a new MongoCollection instance with the different readPreference
    */
  def withReadPreference(readPreference: ReadPreference): MongoCollection[TResult] =
    MongoCollection(wrapped.withReadPreference(readPreference))

  /** Create a new MongoCollection instance with a different write concern.
    *
    * @param writeConcern the new { @link com.mongodb.WriteConcern} for the collection
    * @return a new MongoCollection instance with the different writeConcern
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoCollection[TResult] =
    MongoCollection(wrapped.withWriteConcern(writeConcern))

  /** Create a new MongoCollection instance with a different read concern.
    *
    * @param readConcern the new [[ReadConcern]] for the collection
    * @return a new MongoCollection instance with the different ReadConcern
    */
  def withReadConcern(readConcern: ReadConcern): MongoCollection[TResult] =
    MongoCollection(wrapped.withReadConcern(readConcern))

  /** Gets an estimate of the count of documents in a collection using collection metadata.
    *
    * @return a publisher with a single element indicating the estimated number of documents
    */
  def estimatedDocumentCount(): Task[Long] =
    wrapped.estimatedDocumentCount().getOne.map(identity[Long](_))

  /** Gets an estimate of the count of documents in a collection using collection metadata.
    *
    * @param options the options describing the count
    * @return a publisher with a single element indicating the estimated number of documents
    */
  def estimatedDocumentCount(options: EstimatedDocumentCountOptions): Task[Long] =
    wrapped.estimatedDocumentCount(options).getOne.map(identity[Long](_))

  /** Counts the number of documents in the collection.
    *
    * '''Note:'''
    * For a fast count of the total documents in a collection see [[estimatedDocumentCount()*]]
    * When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+----------------------------------------+
    * | Operator    | Replacement                            |
    * +=============+========================================+
    * | `\$where`     |  `\$expr`                            |
    * +-------------+----------------------------------------+
    * | `\$near`      |  `\$geoWithin` with  `\$center`      |
    * +-------------+----------------------------------------+
    * | `\$nearSphere`|  `\$geoWithin` with  `\$centerSphere`|
    * +-------------+----------------------------------------+
    * }}}
    *
    * @return a publisher with a single element indicating the number of documents
    */
  def countDocuments(): Task[Long] =
    wrapped.countDocuments().getOne.map(identity[Long](_))

  /** Counts the number of documents in the collection according to the given options.
    *
    * '''Note:'''
    * For a fast count of the total documents in a collection see [[estimatedDocumentCount()*]]
    * When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+----------------------------------------+
    * | Operator    | Replacement                            |
    * +=============+========================================+
    * | `\$where`     |  `\$expr`                            |
    * +-------------+----------------------------------------+
    * | `\$near`      |  `\$geoWithin` with  `\$center`      |
    * +-------------+----------------------------------------+
    * | `\$nearSphere`|  `\$geoWithin` with  `\$centerSphere`|
    * +-------------+----------------------------------------+
    * }}}
    *
    * @param filter the query filter
    * @return a publisher with a single element indicating the number of documents
    */
  def countDocuments(filter: Filter): Task[Long] =
    wrapped.countDocuments(filter.toBson).getOne.map(identity[Long](_))

  /** Counts the number of documents in the collection according to the given options.
    *
    * '''Note:'''
    * For a fast count of the total documents in a collection see [[estimatedDocumentCount()*]]
    * When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+----------------------------------------+
    * | Operator    | Replacement                            |
    * +=============+========================================+
    * | `\$where`     |  `\$expr`                            |
    * +-------------+----------------------------------------+
    * | `\$near`      |  `\$geoWithin` with  `\$center`      |
    * +-------------+----------------------------------------+
    * | `\$nearSphere`|  `\$geoWithin` with  `\$centerSphere`|
    * +-------------+----------------------------------------+
    * }}}
    *
    * @param filter  the query filter
    * @param options the options describing the count
    * @return a publisher with a single element indicating the number of documents
    */
  def countDocuments(filter: Filter, options: CountOptions): Task[Long] =
    wrapped.countDocuments(filter.toBson, options).getOne.map(identity[Long](_))

  /** Counts the number of documents in the collection.
    *
    * '''Note:'''
    * For a fast count of the total documents in a collection see [[estimatedDocumentCount()*]]
    * When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+----------------------------------------+
    * | Operator    | Replacement                            |
    * +=============+========================================+
    * | `\$where`     |  `\$expr`                            |
    * +-------------+----------------------------------------+
    * | `\$near`      |  `\$geoWithin` with  `\$center`      |
    * +-------------+----------------------------------------+
    * | `\$nearSphere`|  `\$geoWithin` with  `\$centerSphere`|
    * +-------------+----------------------------------------+
    * }}}
    *
    * @param clientSession the client session with which to associate this operation
    * @return a publisher with a single element indicating the number of documents
    * @note Requires MongoDB 3.6 or greater
    */
  def countDocuments(clientSession: ClientSession): Task[Long] =
    wrapped.countDocuments(clientSession).getOne.map(identity[Long](_))

  /** Counts the number of documents in the collection according to the given options.
    *
    * '''Note:'''
    * For a fast count of the total documents in a collection see [[estimatedDocumentCount()*]]
    * When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+----------------------------------------+
    * | Operator    | Replacement                            |
    * +=============+========================================+
    * | `\$where`     |  `\$expr`                            |
    * +-------------+----------------------------------------+
    * | `\$near`      |  `\$geoWithin` with  `\$center`      |
    * +-------------+----------------------------------------+
    * | `\$nearSphere`|  `\$geoWithin` with  `\$centerSphere`|
    * +-------------+----------------------------------------+
    * }}}
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter        the query filter
    * @return a publisher with a single element indicating the number of documents
    * @note Requires MongoDB 3.6 or greater
    */
  def countDocuments(clientSession: ClientSession, filter: Filter): Task[Long] =
    wrapped.countDocuments(clientSession, filter.toBson).getOne.map(identity[Long](_))

  /** Counts the number of documents in the collection according to the given options.
    *
    * '''Note:'''
    * For a fast count of the total documents in a collection see [[estimatedDocumentCount()*]]
    * When migrating from `count()` to `countDocuments()` the following query operators must be replaced:
    *
    * {{{
    * +-------------+----------------------------------------+
    * | Operator    | Replacement                            |
    * +=============+========================================+
    * | `\$where`     |  `\$expr`                            |
    * +-------------+----------------------------------------+
    * | `\$near`      |  `\$geoWithin` with  `\$center`      |
    * +-------------+----------------------------------------+
    * | `\$nearSphere`|  `\$geoWithin` with  `\$centerSphere`|
    * +-------------+----------------------------------------+
    * }}}
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter        the query filter
    * @param options       the options describing the count
    * @return a publisher with a single element indicating the number of documents
    * @note Requires MongoDB 3.6 or greater
    */
  def countDocuments(
    clientSession: ClientSession,
    filter: Filter,
    options: CountOptions,
  ): Task[Long] =
    wrapped.countDocuments(clientSession, filter.toBson, options).getOne.map(identity[Long](_))

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param fieldName the field name
    * @return a Observable emitting the sequence of distinct values
    */
  def distinct(fieldName: String): DistinctQuery[TResult] =
    DistinctQuery(wrapped.distinct(fieldName, documentClass))

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param fieldName the field name
    * @param filter  the query filter
    * @return a Observable emitting the sequence of distinct values
    */
  def distinct(fieldName: String, filter: Filter): DistinctQuery[TResult] =
    DistinctQuery(wrapped.distinct(fieldName, filter.toBson, documentClass))

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param clientSession the client session with which to associate this operation
    * @param fieldName the field name
    * @return a Observable emitting the sequence of distinct values
    * @note Requires MongoDB 3.6 or greater
    */
  def distinct(clientSession: ClientSession, fieldName: String): DistinctQuery[TResult] =
    DistinctQuery(wrapped.distinct(clientSession, fieldName, documentClass))

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param clientSession the client session with which to associate this operation
    * @param fieldName the field name
    * @param filter  the query filter
    * @return a Observable emitting the sequence of distinct values
    * @note Requires MongoDB 3.6 or greater
    */
  def distinct(
    clientSession: ClientSession,
    fieldName: String,
    filter: Filter,
  ): DistinctQuery[TResult] =
    DistinctQuery(wrapped.distinct(clientSession, fieldName, filter.toBson, documentClass))

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    *
    * @return the find Observable
    */
  def find(): FindQuery[TResult] =
    FindQuery(wrapped.find[TResult](documentClass))

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    * @param filter the query filter
    * @return the find Observable
    */
  def find(filter: Filter): FindQuery[TResult] =
    FindQuery(wrapped.find(filter.toBson, documentClass))

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    *
    * @param clientSession the client session with which to associate this operation
    * @return the find Observable
    * @note Requires MongoDB 3.6 or greater
    */
  def find(clientSession: ClientSession): FindQuery[TResult] =
    FindQuery(wrapped.find(clientSession, documentClass))

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter
    * @return the find Observable
    * @note Requires MongoDB 3.6 or greater
    */
  def find(clientSession: ClientSession, filter: Filter): FindQuery[TResult] =
    FindQuery(wrapped.find(clientSession, filter.toBson, documentClass))

  /** Aggregates documents according to the specified aggregation pipeline.
    *
    * @param pipeline the aggregate pipeline
    * @return a Observable containing the result of the aggregation operation
    *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    */
  def aggregate(pipeline: Seq[Aggregation]): AggregateQuery[TResult] =
    AggregateQuery(wrapped.aggregate[TResult](pipeline.map(_.toBson).asJava, documentClass))

  /** Aggregates documents according to the specified aggregation pipeline.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregate pipeline
    * @return a Observable containing the result of the aggregation operation
    *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    * @note Requires MongoDB 3.6 or greater
    */
  def aggregate(clientSession: ClientSession, pipeline: Seq[Aggregation]): AggregateQuery[TResult] =
    AggregateQuery(
      wrapped.aggregate[TResult](clientSession, pipeline.map(_.toBson).asJava, documentClass),
    )

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param requests the writes to execute
    * @return a Observable with a single element the BulkWriteResult
    */
  def bulkWrite(requests: Seq[? <: WriteModel[? <: TResult]]): Task[BulkWriteResult] =
    wrapped
      .bulkWrite(requests.asJava.asInstanceOf[util.List[? <: WriteModel[? <: TResult]]])
      .getOne // scalafix:ok

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param requests the writes to execute
    * @param options  the options to apply to the bulk write operation
    * @return a Observable with a single element the BulkWriteResult
    */
  def bulkWrite(
    requests: Seq[? <: WriteModel[? <: TResult]],
    options: BulkWriteOptions,
  ): Task[BulkWriteResult] =
    wrapped
      .bulkWrite(
        requests.asJava.asInstanceOf[util.List[? <: WriteModel[? <: TResult]]],
        options,
      )
      .getOne // scalafix:ok

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param clientSession the client session with which to associate this operation
    * @param requests the writes to execute
    * @return a Observable with a single element the BulkWriteResult
    * @note Requires MongoDB 3.6 or greater
    */
  def bulkWrite(
    clientSession: ClientSession,
    requests: Seq[? <: WriteModel[? <: TResult]],
  ): Task[BulkWriteResult] =
    wrapped
      .bulkWrite(
        clientSession,
        requests.asJava.asInstanceOf[util.List[? <: WriteModel[? <: TResult]]],
      )
      .getOne // scalafix:ok

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param clientSession the client session with which to associate this operation
    * @param requests the writes to execute
    * @param options  the options to apply to the bulk write operation
    * @return a Observable with a single element the BulkWriteResult
    * @note Requires MongoDB 3.6 or greater
    */
  def bulkWrite(
    clientSession: ClientSession,
    requests: Seq[? <: WriteModel[? <: TResult]],
    options: BulkWriteOptions,
  ): Task[BulkWriteResult] =
    wrapped
      .bulkWrite(
        clientSession,
        requests.asJava.asInstanceOf[util.List[? <: WriteModel[? <: TResult]]],
        options,
      )
      .getOne // scalafix:ok

  /** Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param document the document to insert
    * @return a Observable with a single element the InsertOneResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertOne(document: TResult): Task[InsertOneResult] = wrapped.insertOne(document).getOne

  /** Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param document the document to insert
    * @param options  the options to apply to the operation
    * @return a Observable with a single element the InsertOneResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertOne(document: TResult, options: InsertOneOptions): Task[InsertOneResult] =
    wrapped.insertOne(document, options).getOne

  /** Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param clientSession the client session with which to associate this operation
    * @param document the document to insert
    * @return a Observable with a single element the InsertOneResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def insertOne(
    clientSession: ClientSession,
    document: TResult,
  ): Task[InsertOneResult] =
    wrapped.insertOne(clientSession, document).getOne

  /** Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param clientSession the client session with which to associate this operation
    * @param document the document to insert
    * @param options  the options to apply to the operation
    * @return a Observable with a single element the InsertOneResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def insertOne(
    clientSession: ClientSession,
    document: TResult,
    options: InsertOneOptions,
  ): Task[InsertOneResult] =
    wrapped.insertOne(clientSession, document, options).getOne

  /** Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
    * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
    *
    * @param documents the documents to insert
    * @return a Observable with a single element the InsertManyResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertMany(documents: Seq[? <: TResult]): Task[InsertManyResult] =
    wrapped.insertMany(documents.asJava).getOne

  /** Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
    * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
    *
    * @param documents the documents to insert
    * @param options   the options to apply to the operation
    * @return a Observable with a single element the InsertManyResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertMany(
    documents: Seq[? <: TResult],
    options: InsertManyOptions,
  ): Task[InsertManyResult] =
    wrapped.insertMany(documents.asJava, options).getOne

  /** Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API.
    *
    * @param clientSession the client session with which to associate this operation
    * @param documents the documents to insert
    * @return a Observable with a single element the InsertManyResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def insertMany(
    clientSession: ClientSession,
    documents: Seq[? <: TResult],
  ): Task[InsertManyResult] =
    wrapped.insertMany(clientSession, documents.asJava).getOne

  /** Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API.
    *
    * @param clientSession the client session with which to associate this operation
    * @param documents the documents to insert
    * @param options   the options to apply to the operation
    * @return a Observable with a single element the InsertManyResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoExceptionn
    * @note Requires MongoDB 3.6 or greater
    */
  def insertMany(
    clientSession: ClientSession,
    documents: Seq[? <: TResult],
    options: InsertManyOptions,
  ): Task[InsertManyResult] =
    wrapped.insertMany(clientSession, documents.asJava, options).getOne

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteOne(filter: Filter): Task[DeleteResult] = wrapped.deleteOne(filter.toBson).getOne

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param filter the query filter to apply the delete operation
    * @param options the options to apply to the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteOne(filter: Filter, options: DeleteOptions): Task[DeleteResult] =
    wrapped.deleteOne(filter.toBson, options).getOne

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def deleteOne(clientSession: ClientSession, filter: Filter): Task[DeleteResult] =
    wrapped.deleteOne(clientSession, filter.toBson).getOne

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter to apply the delete operation
    * @param options the options to apply to the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def deleteOne(
    clientSession: ClientSession,
    filter: Filter,
    options: DeleteOptions,
  ): Task[DeleteResult] =
    wrapped.deleteOne(clientSession, filter.toBson, options).getOne

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteMany(filter: Filter): Task[DeleteResult] = wrapped.deleteMany(filter.toBson).getOne

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param filter the query filter to apply the delete operation
    * @param options the options to apply to the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteMany(filter: Filter, options: DeleteOptions): Task[DeleteResult] =
    wrapped.deleteMany(filter.toBson, options).getOne

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def deleteMany(clientSession: ClientSession, filter: Filter): Task[DeleteResult] =
    wrapped.deleteMany(clientSession, filter.toBson).getOne

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter to apply the delete operation
    * @param options the options to apply to the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def deleteMany(
    clientSession: ClientSession,
    filter: Filter,
    options: DeleteOptions,
  ): Task[DeleteResult] =
    wrapped.deleteMany(clientSession, filter.toBson, options).getOne

  /** Replace a document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @return a Observable with a single element the UpdateResult
    */
  def replaceOne(filter: Filter, replacement: TResult): Task[UpdateResult] =
    wrapped.replaceOne(filter.toBson, replacement).getOne

  /** Replace a document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param clientSession the client session with which to associate this operation
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 3.6 or greater
    */
  def replaceOne(
    clientSession: ClientSession,
    filter: Filter,
    replacement: TResult,
  ): Task[UpdateResult] =
    wrapped.replaceOne(clientSession, filter.toBson, replacement).getOne

  /** Replace a document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @param options     the options to apply to the replace operation
    * @return a Observable with a single element the UpdateResult
    */
  def replaceOne(
    filter: Filter,
    replacement: TResult,
    options: ReplaceOptions,
  ): Task[UpdateResult] =
    wrapped.replaceOne(filter.toBson, replacement, options).getOne

  /** Replace a document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param clientSession the client session with which to associate this operation
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @param options     the options to apply to the replace operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 3.6 or greater
    */
  def replaceOne(
    clientSession: ClientSession,
    filter: Filter,
    replacement: TResult,
    options: ReplaceOptions,
  ): Task[UpdateResult] =
    wrapped.replaceOne(clientSession, filter.toBson, replacement, options).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @return a Observable with a single element the UpdateResult
    */
  def updateOne(filter: Filter, update: Update): Task[UpdateResult] =
    wrapped.updateOne(filter.toBson, update.toBson).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    */
  def updateOne(
    filter: Filter,
    update: Update,
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateOne(filter.toBson, update.toBson, options).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 3.6 or greater
    */
  def updateOne(
    clientSession: ClientSession,
    filter: Filter,
    update: Update,
  ): Task[UpdateResult] =
    wrapped.updateOne(clientSession, filter.toBson, update.toBson).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 3.6 or greater
    */
  def updateOne(
    clientSession: ClientSession,
    filter: Filter,
    update: Update,
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateOne(clientSession, filter.toBson, update.toBson, options).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateOne(filter: Filter, update: Seq[Update]): Task[UpdateResult] =
    wrapped.updateOne(filter.toBson, update.map(_.toBson).asJava).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateOne(
    filter: Filter,
    update: Seq[Update],
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateOne(filter.toBson, update.map(_.toBson).asJava, options).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateOne(
    clientSession: ClientSession,
    filter: Filter,
    update: Seq[Update],
  ): Task[UpdateResult] =
    wrapped.updateOne(clientSession, filter.toBson, update.map(_.toBson).asJava).getOne

  /** Update a single document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateOne(
    clientSession: ClientSession,
    filter: Filter,
    update: Seq[Update],
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateOne(clientSession, filter.toBson, update.map(_.toBson).asJava, options).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @return a Observable with a single element the UpdateResult
    */
  def updateMany(filter: Filter, update: Update): Task[UpdateResult] =
    wrapped.updateMany(filter.toBson, update.toBson).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    */
  def updateMany(
    filter: Filter,
    update: Update,
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateMany(filter.toBson, update.toBson, options).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 3.6 or greater
    */
  def updateMany(
    clientSession: ClientSession,
    filter: Filter,
    update: Update,
  ): Task[UpdateResult] =
    wrapped.updateMany(clientSession, filter.toBson, update.toBson).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 3.6 or greater
    */
  def updateMany(
    clientSession: ClientSession,
    filter: Filter,
    update: Update,
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateMany(clientSession, filter.toBson, update.toBson, options).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateMany(filter: Filter, update: Seq[Update]): Task[UpdateResult] =
    wrapped.updateMany(filter.toBson, update.map(_.toBson).asJava).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateMany(
    filter: Filter,
    update: Seq[Update],
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateMany(filter.toBson, update.map(_.toBson).asJava, options).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateMany(
    clientSession: ClientSession,
    filter: Filter,
    update: Seq[Update],
  ): Task[UpdateResult] =
    wrapped.updateMany(clientSession, filter.toBson, update.map(_.toBson).asJava).getOne

  /** Update all documents in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/ Updates]]
    * [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @param options the options to apply to the update operation
    * @return a Observable with a single element the UpdateResult
    * @note Requires MongoDB 4.2 or greater
    */
  def updateMany(
    clientSession: ClientSession,
    filter: Filter,
    update: Seq[Update],
    options: UpdateOptions,
  ): Task[UpdateResult] =
    wrapped.updateMany(clientSession, filter.toBson, update.map(_.toBson).asJava, options).getOne

  /** Atomically find a document and remove it.
    *
    * @param filter  the query filter to find the document with
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    */
  def findOneAndDelete(filter: Filter): Task[Option[TResult]] =
    wrapped.findOneAndDelete(filter.toBson).getOneOpt

  /** Atomically find a document and remove it.
    *
    * @param filter  the query filter to find the document with
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    */
  def findOneAndDelete(filter: Filter, options: FindOneAndDeleteOptions): Task[Option[TResult]] =
    wrapped.findOneAndDelete(filter.toBson, options).getOneOpt

  /** Atomically find a document and remove it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  the query filter to find the document with
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndDelete(clientSession: ClientSession, filter: Filter): Task[Option[TResult]] =
    wrapped.findOneAndDelete(clientSession, filter.toBson).getOneOpt

  /** Atomically find a document and remove it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  the query filter to find the document with
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndDelete(
    clientSession: ClientSession,
    filter: Filter,
    options: FindOneAndDeleteOptions,
  ): Task[Option[TResult]] =
    wrapped.findOneAndDelete(clientSession, filter.toBson, options).getOneOpt

  /** Atomically find a document and replace it.
    *
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @return a Observable with a single element the document that was replaced.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    */
  def findOneAndReplace(filter: Filter, replacement: TResult): Task[Option[TResult]] =
    wrapped.findOneAndReplace(filter.toBson, replacement).getOneOpt

  /** Atomically find a document and replace it.
    *
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @param options     the options to apply to the operation
    * @return a Observable with a single element the document that was replaced.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    */
  def findOneAndReplace(
    filter: Filter,
    replacement: TResult,
    options: FindOneAndReplaceOptions,
  ): Task[Option[TResult]] =
    wrapped.findOneAndReplace(filter.toBson, replacement, options).getOneOpt

  /** Atomically find a document and replace it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @return a Observable with a single element the document that was replaced.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndReplace(
    clientSession: ClientSession,
    filter: Filter,
    replacement: TResult,
  ): Task[Option[TResult]] =
    wrapped.findOneAndReplace(clientSession, filter.toBson, replacement).getOneOpt

  /** Atomically find a document and replace it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @param options     the options to apply to the operation
    * @return a Observable with a single element the document that was replaced.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndReplace(
    clientSession: ClientSession,
    filter: Filter,
    replacement: TResult,
    options: FindOneAndReplaceOptions,
  ): Task[Option[TResult]] =
    wrapped.findOneAndReplace(clientSession, filter.toBson, replacement, options).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    */
  def findOneAndUpdate(filter: Filter, update: Update): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(filter.toBson, update.toBson).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    */
  def findOneAndUpdate(
    filter: Filter,
    update: Update,
    options: FindOneAndUpdateOptions,
  ): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(filter.toBson, update.toBson, options).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndUpdate(
    clientSession: ClientSession,
    filter: Filter,
    update: Update,
  ): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(clientSession, filter.toBson, update.toBson).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a document describing the update, which may not be null. The update to apply must include only update operators. This
    *                can be of any type for which a `Codec` is registered
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndUpdate(
    clientSession: ClientSession,
    filter: Filter,
    update: Update,
    options: FindOneAndUpdateOptions,
  ): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(clientSession, filter.toBson, update.toBson, options).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(filter: Filter, update: Seq[Update]): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(filter.toBson, update.map(_.toBson).asJava).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(
    filter: Filter,
    update: Seq[Update],
    options: FindOneAndUpdateOptions,
  ): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(filter.toBson, update.map(_.toBson).asJava, options).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(
    clientSession: ClientSession,
    filter: Filter,
    update: Seq[Update],
  ): Task[Option[TResult]] =
    wrapped.findOneAndUpdate(clientSession, filter.toBson, update.map(_.toBson).asJava).getOneOpt

  /** Atomically find a document and update it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  a document describing the query filter, which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param update  a pipeline describing the update.
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was updated.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    * @note Requires MongoDB 4.2 or greater
    */
  def findOneAndUpdate(
    clientSession: ClientSession,
    filter: Filter,
    update: Seq[Update],
    options: FindOneAndUpdateOptions,
  ): Task[Option[TResult]] =
    wrapped
      .findOneAndUpdate(clientSession, filter.toBson, update.map(_.toBson).asJava, options)
      .getOneOpt

  /** Drops this collection from the Database.
    *
    * @return an empty Observable that indicates when the operation has completed
    *         [[https://www.mongodb.com/docs/manual/reference/command/drop/ Drop Collection]]
    */
  def drop(): Task[Unit] = wrapped.drop().getOne.unit

  /** Drops this collection from the Database.
    *
    * @param clientSession the client session with which to associate this operation
    * @return an empty Observable that indicates when the operation has completed
    *         [[https://www.mongodb.com/docs/manual/reference/command/drop/ Drop Collection]]
    * @note Requires MongoDB 3.6 or greater
    */
  def drop(clientSession: ClientSession): Task[Unit] = wrapped.drop(clientSession).getOne.unit

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param key an object describing the index key(s)
    * @return created index name
    */
  def createIndex(key: IndexKey): Task[String] = wrapped.createIndex(key.toBson).getOne

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param key     an object describing the index key(s)
    * @param options the options for the index
    * @return created index name
    */
  def createIndex(key: IndexKey, options: IndexOptions): Task[String] =
    wrapped.createIndex(key.toBson, options.toJava).getOne

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param clientSession the client session with which to associate this operation
    * @param key     an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @return created index name
    * @note Requires MongoDB 3.6 or greater
    */
  def createIndex(clientSession: ClientSession, key: IndexKey): Task[String] =
    wrapped.createIndex(clientSession, key.toBson).getOne

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param clientSession the client session with which to associate this operation
    * @param key     an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @param options the options for the index
    * @return created index name
    * @note Requires MongoDB 3.6 or greater
    */
  def createIndex(
    clientSession: ClientSession,
    key: IndexKey,
    options: IndexOptions,
  ): Task[String] =
    wrapped.createIndex(clientSession, key.toBson, options.toJava).getOne

  /** Create multiple indexes.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param indexes the list of indexes to create
    * @return names of the indexes
    */
  def createIndexes(indexes: Index*): Task[Chunk[String]] = {
    val models = indexes.map(_.toJava).asJava

    wrapped.createIndexes(models).stream.runCollect
  }

  /** Create multiple indexes.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param indexes the list of indexes to create
    * @param createIndexOptions options to use when creating indexes
    * @return names of the indexes
    */
  def createIndexes(
    indexes: Seq[Index],
    createIndexOptions: CreateIndexOptions,
  ): Task[Chunk[String]] = {
    val models = indexes.map(_.toJava).asJava

    wrapped.createIndexes(models, createIndexOptions.toJava).stream.runCollect
  }

  /** Create multiple indexes.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param clientSession the client session with which to associate this operation
    * @param indexes the list of indexes to create
    * @return names of the indexes
    * @note Requires MongoDB 3.6 or greater
    */
  def createIndexes(
    clientSession: ClientSession,
    indexes: Index*,
  ): Task[Chunk[String]] = {
    val models = indexes.map(_.toJava).asJava

    wrapped.createIndexes(clientSession, models).stream.runCollect
  }

  /** Create multiple indexes.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param clientSession the client session with which to associate this operation
    * @param indexes the list of indexes to create
    * @param createIndexOptions options to use when creating indexes
    * @return names of the indexes
    * @note Requires MongoDB 3.6 or greater
    */
  def createIndexes(
    clientSession: ClientSession,
    indexes: Seq[Index],
    createIndexOptions: CreateIndexOptions,
  ): Task[Chunk[String]] = {
    val models = indexes.map(_.toJava).asJava

    wrapped.createIndexes(clientSession, models, createIndexOptions.toJava).stream.runCollect
  }

  /** Get all the indexes in this collection.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listIndexes/ listIndexes]]
    * @return the fluent list indexes interface
    */
  def listIndexes()(implicit
    ct: ClassTag[Document],
  ): ListIndexesQuery[Document] =
    ListIndexesQuery(wrapped.listIndexes(ct))

  /** Get all the indexes in this collection.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listIndexes/ listIndexes]]
    * @param clientSession the client session with which to associate this operation
    * @return the fluent list indexes interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listIndexes(clientSession: ClientSession)(implicit
    ct: ClassTag[Document],
  ): ListIndexesQuery[Document] =
    ListIndexesQuery(wrapped.listIndexes(clientSession, ct))

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param indexName the name of the index to remove
    * @return an empty Task
    */
  def dropIndex(indexName: String): Task[Unit] = wrapped.dropIndex(indexName).getOneOpt.unit

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param indexName the name of the index to remove
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    */
  def dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): Task[Unit] =
    wrapped.dropIndex(indexName, dropIndexOptions.toJava).getOneOpt.unit

  /** Drops the index given the keys used to create it.
    *
    * @param keys the keys of the index to remove
    * @return an empty Task
    */
  def dropIndex(keys: IndexKey): Task[Unit] = wrapped.dropIndex(keys.toBson).getOneOpt.unit

  /** Drops the index given the keys used to create it.
    *
    * @param keys the keys of the index to remove
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    */
  def dropIndex(keys: IndexKey, dropIndexOptions: DropIndexOptions): Task[Unit] =
    wrapped.dropIndex(keys.toBson, dropIndexOptions.toJava).getOneOpt.unit

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession the client session with which to associate this operation
    * @param indexName the name of the index to remove
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, indexName: String): Task[Unit] =
    wrapped.dropIndex(clientSession, indexName).getOneOpt.unit

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession the client session with which to associate this operation
    * @param indexName the name of the index to remove
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndex(
    clientSession: ClientSession,
    indexName: String,
    dropIndexOptions: DropIndexOptions,
  ): Task[Unit] =
    wrapped.dropIndex(clientSession, indexName, dropIndexOptions.toJava).getOneOpt.unit

  /** Drops the index given the keys used to create it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param keys the keys of the index to remove
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, keys: IndexKey): Task[Unit] =
    wrapped.dropIndex(clientSession, keys.toBson).getOneOpt.unit

  /** Drops the index given the keys used to create it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param keys the keys of the index to remove
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndex(
    clientSession: ClientSession,
    keys: IndexKey,
    dropIndexOptions: DropIndexOptions,
  ): Task[Unit] =
    wrapped.dropIndex(clientSession, keys.toBson, dropIndexOptions.toJava).getOneOpt.unit

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @return an empty Task
    */
  def dropIndexes(): Task[Unit] = wrapped.dropIndexes().getOneOpt.unit

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    */
  def dropIndexes(dropIndexOptions: DropIndexOptions): Task[Unit] =
    wrapped.dropIndexes(dropIndexOptions.toJava).getOneOpt.unit

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession the client session with which to associate this operation
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndexes(clientSession: ClientSession): Task[Unit] =
    wrapped.dropIndexes(clientSession).getOneOpt.unit

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession the client session with which to associate this operation
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndexes(
    clientSession: ClientSession,
    dropIndexOptions: DropIndexOptions,
  ): Task[Unit] =
    wrapped.dropIndexes(clientSession, dropIndexOptions.toJava).getOneOpt.unit

  /** Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/renameCollection Rename collection]]
    * @param newCollectionNamespace the name the collection will be renamed to
    * @return an empty Observable that indicates when the operation has completed
    */
  def renameCollection(newCollectionNamespace: MongoNamespace): Task[Unit] =
    wrapped.renameCollection(newCollectionNamespace).getOneOpt.unit

  /** Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/renameCollection Rename collection]]
    * @param newCollectionNamespace the name the collection will be renamed to
    * @param options                the options for renaming a collection
    * @return an empty Observable that indicates when the operation has completed
    */
  def renameCollection(
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions,
  ): Task[Unit] =
    wrapped.renameCollection(newCollectionNamespace, options).getOneOpt.unit

  /** Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/renameCollection Rename collection]]
    * @param clientSession the client session with which to associate this operation
    * @param newCollectionNamespace the name the collection will be renamed to
    * @return an empty Observable that indicates when the operation has completed
    * @note Requires MongoDB 3.6 or greater
    */
  def renameCollection(
    clientSession: ClientSession,
    newCollectionNamespace: MongoNamespace,
  ): Task[Unit] =
    wrapped.renameCollection(clientSession, newCollectionNamespace).getOneOpt.unit

  /** Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/renameCollection Rename collection]]
    * @param clientSession the client session with which to associate this operation
    * @param newCollectionNamespace the name the collection will be renamed to
    * @param options                the options for renaming a collection
    * @return an empty Observable that indicates when the operation has completed
    * @note Requires MongoDB 3.6 or greater
    */
  def renameCollection(
    clientSession: ClientSession,
    newCollectionNamespace: MongoNamespace,
    options: RenameCollectionOptions,
  ): Task[Unit] =
    wrapped.renameCollection(clientSession, newCollectionNamespace, options).getOneOpt.unit

  /** Creates a change stream for this collection.
    *
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(): ChangeStreamQuery[TResult] =
    ChangeStreamQuery(wrapped.watch(documentClass))

  /** Creates a change stream for this collection.
    *
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(pipeline: Seq[Aggregation]): ChangeStreamQuery[TResult] =
    ChangeStreamQuery(wrapped.watch(pipeline.map(_.toBson).asJava, documentClass))

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(clientSession: ClientSession): ChangeStreamQuery[TResult] =
    ChangeStreamQuery(wrapped.watch(clientSession, documentClass))

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(clientSession: ClientSession, pipeline: Seq[Aggregation]): ChangeStreamQuery[TResult] =
    ChangeStreamQuery(wrapped.watch(clientSession, pipeline.map(_.toBson).asJava, documentClass))
}
