package io.github.zeal18.zio.mongodb.driver

import com.mongodb.bulk.BulkWriteResult
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoCollection as JMongoCollection
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.internal.CodecAdapter
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.MongoNamespace
import io.github.zeal18.zio.mongodb.driver.ReadConcern
import io.github.zeal18.zio.mongodb.driver.ReadPreference
import io.github.zeal18.zio.mongodb.driver.WriteConcern
import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.indexes.CreateIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.DropIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.Index
import io.github.zeal18.zio.mongodb.driver.indexes.IndexKey
import io.github.zeal18.zio.mongodb.driver.indexes.IndexOptions
import io.github.zeal18.zio.mongodb.driver.model.*
import io.github.zeal18.zio.mongodb.driver.model.bulk.BulkWrite
import io.github.zeal18.zio.mongodb.driver.query.*
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import io.github.zeal18.zio.mongodb.driver.result.*
import io.github.zeal18.zio.mongodb.driver.updates.Update
import org.bson.codecs.configuration.CodecRegistries.*
import org.bson.codecs.configuration.CodecRegistry
import zio.Chunk
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

/** The MongoCollection representation.
  *
  * @param wrapped the underlying java MongoCollection
  * @tparam A The type that this collection will encode documents from and decode documents to.
  */
trait MongoCollection[A] {

  /** Gets the namespace of this collection.
    *
    * @return the namespace
    */
  def namespace: MongoNamespace

  /** Get the default class to cast any documents returned from the database into.
    *
    * @return the default class to cast any documents into
    */
  def documentClass: Class[A]

  /** Get the codec registry for the MongoDatabase.
    *
    * @return the { @link org.bson.codecs.configuration.CodecRegistry}
    */
  def codecRegistry: CodecRegistry

  /** Get the read preference for the MongoDatabase.
    *
    * @return the { @link com.mongodb.ReadPreference}
    */
  def readPreference: ReadPreference

  /** Get the write concern for the MongoDatabase.
    *
    * @return the { @link com.mongodb.WriteConcern}
    */
  def writeConcern: WriteConcern

  /** Get the read concern for the MongoDatabase.
    *
    * @return the [[ReadConcern]]
    */
  def readConcern: ReadConcern

  /** Create a new MongoCollection instance with a different default class to cast any documents returned from the database into..
    *
    * @tparam C   The type that the new collection will encode documents from and decode documents to
    * @return a new MongoCollection instance with the different default class
    */
  def withDocumentClass[C: ClassTag](implicit codec: Codec[C]): MongoCollection[C]

  /** Create a new MongoCollection instance with a different read preference.
    *
    * @param readPreference the new { @link com.mongodb.ReadPreference} for the collection
    * @return a new MongoCollection instance with the different readPreference
    */
  def withReadPreference(readPreference: ReadPreference): MongoCollection[A]

  /** Create a new MongoCollection instance with a different write concern.
    *
    * @param writeConcern the new { @link com.mongodb.WriteConcern} for the collection
    * @return a new MongoCollection instance with the different writeConcern
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoCollection[A]

  /** Create a new MongoCollection instance with a different read concern.
    *
    * @param readConcern the new [[ReadConcern]] for the collection
    * @return a new MongoCollection instance with the different ReadConcern
    */
  def withReadConcern(readConcern: ReadConcern): MongoCollection[A]

  /** Creates a client session.
    *
    * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
    *
    * @note Requires MongoDB 3.6 or greater
    */
  def startSession(): ZIO[Scope, Throwable, ClientSession]

  /** Creates a client session.
    *
    * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
    *
    * @param options  the options for the client session
    * @note Requires MongoDB 3.6 or greater
    */
  def startSession(options: ClientSessionOptions): ZIO[Scope, Throwable, ClientSession]

  /** Gets an estimate of the count of documents in a collection using collection metadata.
    *
    * @return a publisher with a single element indicating the estimated number of documents
    */
  def estimatedDocumentCount(): Task[Long]

  /** Gets an estimate of the count of documents in a collection using collection metadata.
    *
    * @param options the options describing the count
    * @return a publisher with a single element indicating the estimated number of documents
    */
  def estimatedDocumentCount(options: EstimatedDocumentCountOptions): Task[Long]

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
  def countDocuments(): Task[Long]

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
  def countDocuments(filter: Filter): Task[Long]

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
  def countDocuments(filter: Filter, options: CountOptions): Task[Long]

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
  def countDocuments(clientSession: ClientSession): Task[Long]

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
  def countDocuments(clientSession: ClientSession, filter: Filter): Task[Long]

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
  ): Task[Long]

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param fieldName the field name
    * @return a Observable emitting the sequence of distinct values
    */
  def distinct(fieldName: String): DistinctQuery[A]

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param fieldName the field name
    * @param filter  the query filter
    * @return a Observable emitting the sequence of distinct values
    */
  def distinct(fieldName: String, filter: Filter): DistinctQuery[A]

  /** Gets the distinct values of the specified field name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/distinct/ Distinct]]
    * @param clientSession the client session with which to associate this operation
    * @param fieldName the field name
    * @return a Observable emitting the sequence of distinct values
    * @note Requires MongoDB 3.6 or greater
    */
  def distinct(clientSession: ClientSession, fieldName: String): DistinctQuery[A]

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
  ): DistinctQuery[A]

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    *
    * @return the find Observable
    */
  def find(): FindQuery[A]

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    * @param filter the query filter
    * @return the find Observable
    */
  def find(filter: Filter): FindQuery[A]

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    *
    * @param clientSession the client session with which to associate this operation
    * @return the find Observable
    * @note Requires MongoDB 3.6 or greater
    */
  def find(clientSession: ClientSession): FindQuery[A]

  /** Finds all documents in the collection.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/query-documents/ Find]]
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter
    * @return the find Observable
    * @note Requires MongoDB 3.6 or greater
    */
  def find(clientSession: ClientSession, filter: Filter): FindQuery[A]

  /** Aggregates documents according to the specified aggregation pipeline.
    *
    * @param pipeline the aggregate pipeline
    * @return a Observable containing the result of the aggregation operation
    *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    */
  def aggregate(pipeline: Aggregation*): AggregateQuery[A]

  /** Aggregates documents according to the specified aggregation pipeline.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregate pipeline
    * @return a Observable containing the result of the aggregation operation
    *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    * @note Requires MongoDB 3.6 or greater
    */
  def aggregate(clientSession: ClientSession, pipeline: Aggregation*): AggregateQuery[A]

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param requests the writes to execute
    * @return a Observable with a single element the BulkWriteResult
    */
  def bulkWrite(requests: Seq[BulkWrite[A]]): Task[BulkWriteResult]

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param requests the writes to execute
    * @param options  the options to apply to the bulk write operation
    * @return a Observable with a single element the BulkWriteResult
    */
  def bulkWrite(
    requests: Seq[BulkWrite[A]],
    options: BulkWriteOptions,
  ): Task[BulkWriteResult]

  /** Executes a mix of inserts, updates, replaces, and deletes.
    *
    * @param clientSession the client session with which to associate this operation
    * @param requests the writes to execute
    * @return a Observable with a single element the BulkWriteResult
    * @note Requires MongoDB 3.6 or greater
    */
  def bulkWrite(
    clientSession: ClientSession,
    requests: Seq[BulkWrite[A]],
  ): Task[BulkWriteResult]

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
    requests: Seq[BulkWrite[A]],
    options: BulkWriteOptions,
  ): Task[BulkWriteResult]

  /** Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param document the document to insert
    * @return a Observable with a single element the InsertOneResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertOne(document: A): Task[InsertOneResult]

  /** Inserts the provided document. If the document is missing an identifier, the driver should generate one.
    *
    * @param document the document to insert
    * @param options  the options to apply to the operation
    * @return a Observable with a single element the InsertOneResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertOne(document: A, options: InsertOneOptions): Task[InsertOneResult]

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
    document: A,
  ): Task[InsertOneResult]

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
    document: A,
    options: InsertOneOptions,
  ): Task[InsertOneResult]

  /** Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
    * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
    *
    * @param documents the documents to insert
    * @return a Observable with a single element the InsertManyResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertMany(documents: Seq[? <: A]): Task[InsertManyResult]

  /** Inserts a batch of documents. The preferred way to perform bulk inserts is to use the BulkWrite API. However, when talking with a
    * server &lt; 2.6, using this method will be faster due to constraints in the bulk API related to error handling.
    *
    * @param documents the documents to insert
    * @param options   the options to apply to the operation
    * @return a Observable with a single element the InsertManyResult or with either a
    *         com.mongodb.DuplicateKeyException or com.mongodb.MongoException
    */
  def insertMany(
    documents: Seq[? <: A],
    options: InsertManyOptions,
  ): Task[InsertManyResult]

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
    documents: Seq[? <: A],
  ): Task[InsertManyResult]

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
    documents: Seq[? <: A],
    options: InsertManyOptions,
  ): Task[InsertManyResult]

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteOne(filter: Filter): Task[DeleteResult]

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param filter the query filter to apply the delete operation
    * @param options the options to apply to the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteOne(filter: Filter, options: DeleteOptions): Task[DeleteResult]

  /** Removes at most one document from the collection that matches the given filter.  If no documents match, the collection is not
    * modified.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def deleteOne(clientSession: ClientSession, filter: Filter): Task[DeleteResult]

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
  ): Task[DeleteResult]

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteMany(filter: Filter): Task[DeleteResult]

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param filter the query filter to apply the delete operation
    * @param options the options to apply to the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    */
  def deleteMany(filter: Filter, options: DeleteOptions): Task[DeleteResult]

  /** Removes all documents from the collection that match the given query filter.  If no documents match, the collection is not modified.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter the query filter to apply the delete operation
    * @return a Observable with a single element the DeleteResult or with an com.mongodb.MongoException
    * @note Requires MongoDB 3.6 or greater
    */
  def deleteMany(clientSession: ClientSession, filter: Filter): Task[DeleteResult]

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
  ): Task[DeleteResult]

  /** Replace a document in the collection according to the specified arguments.
    *
    * [[https://www.mongodb.com/docs/manual/tutorial/modify-documents/#replace-the-document Replace]]
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @return a Observable with a single element the UpdateResult
    */
  def replaceOne(filter: Filter, replacement: A): Task[UpdateResult]

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
    replacement: A,
  ): Task[UpdateResult]

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
    replacement: A,
    options: ReplaceOptions,
  ): Task[UpdateResult]

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
    replacement: A,
    options: ReplaceOptions,
  ): Task[UpdateResult]

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
  def updateOne(filter: Filter, update: Update): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  def updateOne(filter: Filter, update: Seq[Update]): Task[UpdateResult]

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
  def updateOne(filter: Filter, update: Seq[Update], options: UpdateOptions): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  def updateMany(filter: Filter, update: Update): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  def updateMany(filter: Filter, update: Seq[Update]): Task[UpdateResult]

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
  def updateMany(filter: Filter, update: Seq[Update], options: UpdateOptions): Task[UpdateResult]

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
  ): Task[UpdateResult]

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
  ): Task[UpdateResult]

  /** Atomically find a document and remove it.
    *
    * @param filter  the query filter to find the document with
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    */
  def findOneAndDelete(filter: Filter): Task[Option[A]]

  /** Atomically find a document and remove it.
    *
    * @param filter  the query filter to find the document with
    * @param options the options to apply to the operation
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    */
  def findOneAndDelete(filter: Filter, options: FindOneAndDeleteOptions): Task[Option[A]]

  /** Atomically find a document and remove it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param filter  the query filter to find the document with
    * @return a Observable with a single element the document that was removed.  If no documents matched the query filter, then null will be
    *         returned
    * @note Requires MongoDB 3.6 or greater
    */
  def findOneAndDelete(clientSession: ClientSession, filter: Filter): Task[Option[A]]

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
  ): Task[Option[A]]

  /** Atomically find a document and replace it.
    *
    * @param filter      the query filter to apply the replace operation
    * @param replacement the replacement document
    * @return a Observable with a single element the document that was replaced.  Depending on the value of the `returnOriginal`
    *         property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the
    *         query filter, then null will be returned
    */
  def findOneAndReplace(filter: Filter, replacement: A): Task[Option[A]]

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
    replacement: A,
    options: FindOneAndReplaceOptions,
  ): Task[Option[A]]

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
    replacement: A,
  ): Task[Option[A]]

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
    replacement: A,
    options: FindOneAndReplaceOptions,
  ): Task[Option[A]]

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
  def findOneAndUpdate(filter: Filter, update: Update): Task[Option[A]]

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
  ): Task[Option[A]]

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
  ): Task[Option[A]]

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
  ): Task[Option[A]]

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
  def findOneAndUpdate(filter: Filter, update: Seq[Update]): Task[Option[A]]

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
  ): Task[Option[A]]

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
  ): Task[Option[A]]

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
  ): Task[Option[A]]

  /** Drops this collection from the Database.
    *
    * @return an empty Observable that indicates when the operation has completed
    *         [[https://www.mongodb.com/docs/manual/reference/command/drop/ Drop Collection]]
    */
  def drop(): Task[Unit]

  /** Drops this collection from the Database.
    *
    * @param clientSession the client session with which to associate this operation
    * @return an empty Observable that indicates when the operation has completed
    *         [[https://www.mongodb.com/docs/manual/reference/command/drop/ Drop Collection]]
    * @note Requires MongoDB 3.6 or greater
    */
  def drop(clientSession: ClientSession): Task[Unit]

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param key an object describing the index key(s)
    * @return created index name
    */
  def createIndex(key: IndexKey): Task[String]

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param key     an object describing the index key(s)
    * @param options the options for the index
    * @return created index name
    */
  def createIndex(key: IndexKey, options: IndexOptions): Task[String]

  /** [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param clientSession the client session with which to associate this operation
    * @param key     an object describing the index key(s), which may not be null. This can be of any type for which a `Codec` is
    *                registered
    * @return created index name
    * @note Requires MongoDB 3.6 or greater
    */
  def createIndex(clientSession: ClientSession, key: IndexKey): Task[String]

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
  ): Task[String]

  /** Create multiple indexes.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param indexes the list of indexes to create
    * @return names of the indexes
    */
  def createIndexes(indexes: Index*): Task[Chunk[String]]

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
  ): Task[Chunk[String]]

  /** Create multiple indexes.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/createIndexes Create IndexKey]]
    * @param clientSession the client session with which to associate this operation
    * @param indexes the list of indexes to create
    * @return names of the indexes
    * @note Requires MongoDB 3.6 or greater
    */
  def createIndexes(clientSession: ClientSession, indexes: Index*): Task[Chunk[String]]

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
  ): Task[Chunk[String]]

  /** Get all the indexes in this collection.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listIndexes/ listIndexes]]
    * @return the fluent list indexes interface
    */
  def listIndexes(): ListIndexesQuery[Document]

  /** Get all the indexes in this collection.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listIndexes/ listIndexes]]
    * @param clientSession the client session with which to associate this operation
    * @return the fluent list indexes interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listIndexes(clientSession: ClientSession): ListIndexesQuery[Document]

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param indexName the name of the index to remove
    * @return an empty Task
    */
  def dropIndex(indexName: String): Task[Unit]

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param indexName the name of the index to remove
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    */
  def dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): Task[Unit]

  /** Drops the index given the keys used to create it.
    *
    * @param keys the keys of the index to remove
    * @return an empty Task
    */
  def dropIndex(keys: IndexKey): Task[Unit]

  /** Drops the index given the keys used to create it.
    *
    * @param keys the keys of the index to remove
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    */
  def dropIndex(keys: IndexKey, dropIndexOptions: DropIndexOptions): Task[Unit]

  /** Drops the given index.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession the client session with which to associate this operation
    * @param indexName the name of the index to remove
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, indexName: String): Task[Unit]

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
  ): Task[Unit]

  /** Drops the index given the keys used to create it.
    *
    * @param clientSession the client session with which to associate this operation
    * @param keys the keys of the index to remove
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndex(clientSession: ClientSession, keys: IndexKey): Task[Unit]

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
  ): Task[Unit]

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @return an empty Task
    */
  def dropIndexes(): Task[Unit]

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param dropIndexOptions options to use when dropping indexes
    * @return an empty Task
    */
  def dropIndexes(dropIndexOptions: DropIndexOptions): Task[Unit]

  /** Drop all the indexes on this collection, except for the default on _id.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
    * @param clientSession the client session with which to associate this operation
    * @return an empty Task
    * @note Requires MongoDB 3.6 or greater
    */
  def dropIndexes(clientSession: ClientSession): Task[Unit]

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
  ): Task[Unit]

  /** Rename the collection with oldCollectionName to the newCollectionName.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/renameCollection Rename collection]]
    * @param newCollectionNamespace the name the collection will be renamed to
    * @return an empty Observable that indicates when the operation has completed
    */
  def renameCollection(newCollectionNamespace: MongoNamespace): Task[Unit]

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
  ): Task[Unit]

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
  ): Task[Unit]

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
  ): Task[Unit]

  /** Creates a change stream for this collection.
    *
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(): ChangeStreamQuery[A]

  /** Creates a change stream for this collection.
    *
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(pipeline: Seq[Aggregation]): ChangeStreamQuery[A]

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(clientSession: ClientSession): ChangeStreamQuery[A]

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 3.6 or greater
    */
  def watch(clientSession: ClientSession, pipeline: Seq[Aggregation]): ChangeStreamQuery[A]
}

object MongoCollection {

  def live[A: izumi.reflect.Tag: ClassTag](name: String)(implicit
    codec: Codec[A],
  ): ZLayer[MongoDatabase, Nothing, MongoCollection[A]] =
    ZLayer.fromFunction[MongoDatabase => MongoCollection[A]](_.getCollection[A](name))

  final private[driver] case class Live[A](
    database: MongoDatabase,
    wrapped: JMongoCollection[A],
  ) extends MongoCollection[A] {
    override lazy val namespace: MongoNamespace = wrapped.getNamespace

    override lazy val documentClass: Class[A] = wrapped.getDocumentClass

    override lazy val codecRegistry: CodecRegistry = wrapped.getCodecRegistry

    override lazy val readPreference: ReadPreference = wrapped.getReadPreference

    override lazy val writeConcern: WriteConcern = wrapped.getWriteConcern

    override lazy val readConcern: ReadConcern = wrapped.getReadConcern

    override def withDocumentClass[C: ClassTag](implicit
      codec: Codec[C],
    ): MongoCollection[C] = {
      val adaptedCodec = CodecAdapter(codec)
      Live[C](
        database,
        wrapped
          .withDocumentClass(adaptedCodec.getEncoderClass())
          .withCodecRegistry(
            fromRegistries(fromCodecs(adaptedCodec), MongoClient.DEFAULT_CODEC_REGISTRY),
          ),
      )
    }

    override def withReadPreference(readPreference: ReadPreference): MongoCollection[A] =
      Live[A](database, wrapped.withReadPreference(readPreference))

    override def withWriteConcern(writeConcern: WriteConcern): MongoCollection[A] =
      Live[A](database, wrapped.withWriteConcern(writeConcern))

    override def startSession(): ZIO[Scope, Throwable, ClientSession] = database.startSession()

    override def startSession(
      options: ClientSessionOptions,
    ): ZIO[Scope, Throwable, ClientSession] =
      database.startSession(options)

    override def withReadConcern(readConcern: ReadConcern): MongoCollection[A] =
      Live[A](database, wrapped.withReadConcern(readConcern))

    override def estimatedDocumentCount(): Task[Long] =
      wrapped.estimatedDocumentCount().head.map(identity[Long](_))

    override def estimatedDocumentCount(options: EstimatedDocumentCountOptions): Task[Long] =
      wrapped.estimatedDocumentCount(options.toJava).head.map(identity[Long](_))

    override def countDocuments(): Task[Long] =
      wrapped.countDocuments().head.map(identity[Long](_))

    override def countDocuments(filter: Filter): Task[Long] =
      wrapped.countDocuments(filter).head.map(identity[Long](_))

    override def countDocuments(filter: Filter, options: CountOptions): Task[Long] =
      wrapped.countDocuments(filter, options.toJava).head.map(identity[Long](_))

    override def countDocuments(clientSession: ClientSession): Task[Long] =
      wrapped.countDocuments(clientSession).head.map(identity[Long](_))

    override def countDocuments(clientSession: ClientSession, filter: Filter): Task[Long] =
      wrapped.countDocuments(clientSession, filter).head.map(identity[Long](_))

    override def countDocuments(
      clientSession: ClientSession,
      filter: Filter,
      options: CountOptions,
    ): Task[Long] =
      wrapped.countDocuments(clientSession, filter, options.toJava).head.map(identity[Long](_))

    override def distinct(fieldName: String): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(fieldName, documentClass))

    override def distinct(fieldName: String, filter: Filter): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(fieldName, filter, documentClass))

    override def distinct(clientSession: ClientSession, fieldName: String): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(clientSession, fieldName, documentClass))

    override def distinct(
      clientSession: ClientSession,
      fieldName: String,
      filter: Filter,
    ): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(clientSession, fieldName, filter, documentClass))

    override def find(): FindQuery[A] =
      FindQuery(wrapped.find[A](documentClass))

    override def find(filter: Filter): FindQuery[A] =
      FindQuery(wrapped.find(filter, documentClass))

    override def find(clientSession: ClientSession): FindQuery[A] =
      FindQuery(wrapped.find(clientSession, documentClass))

    override def find(clientSession: ClientSession, filter: Filter): FindQuery[A] =
      FindQuery(wrapped.find(clientSession, filter, documentClass))

    override def aggregate(pipeline: Aggregation*): AggregateQuery[A] =
      AggregateQuery(wrapped.aggregate[A](pipeline.asJava, documentClass))

    override def aggregate(
      clientSession: ClientSession,
      pipeline: Aggregation*,
    ): AggregateQuery[A] =
      AggregateQuery(
        wrapped.aggregate[A](clientSession, pipeline.asJava, documentClass),
      )

    override def bulkWrite(requests: Seq[BulkWrite[A]]): Task[BulkWriteResult] =
      wrapped.bulkWrite(requests.map(_.toJava).asJava).head

    override def bulkWrite(
      requests: Seq[BulkWrite[A]],
      options: BulkWriteOptions,
    ): Task[BulkWriteResult] =
      wrapped.bulkWrite(requests.map(_.toJava).asJava, options.toJava).head

    override def bulkWrite(
      clientSession: ClientSession,
      requests: Seq[BulkWrite[A]],
    ): Task[BulkWriteResult] =
      wrapped.bulkWrite(clientSession, requests.map(_.toJava).asJava).head

    override def bulkWrite(
      clientSession: ClientSession,
      requests: Seq[BulkWrite[A]],
      options: BulkWriteOptions,
    ): Task[BulkWriteResult] =
      wrapped.bulkWrite(clientSession, requests.map(_.toJava).asJava, options.toJava).head

    override def insertOne(document: A): Task[InsertOneResult] = wrapped.insertOne(document).head

    override def insertOne(document: A, options: InsertOneOptions): Task[InsertOneResult] =
      wrapped.insertOne(document, options.toJava).head

    override def insertOne(
      clientSession: ClientSession,
      document: A,
    ): Task[InsertOneResult] =
      wrapped.insertOne(clientSession, document).head

    override def insertOne(
      clientSession: ClientSession,
      document: A,
      options: InsertOneOptions,
    ): Task[InsertOneResult] =
      wrapped.insertOne(clientSession, document, options.toJava).head

    override def insertMany(documents: Seq[? <: A]): Task[InsertManyResult] =
      wrapped.insertMany(documents.asJava).head

    override def insertMany(
      documents: Seq[? <: A],
      options: InsertManyOptions,
    ): Task[InsertManyResult] =
      wrapped.insertMany(documents.asJava, options.toJava).head

    override def insertMany(
      clientSession: ClientSession,
      documents: Seq[? <: A],
    ): Task[InsertManyResult] =
      wrapped.insertMany(clientSession, documents.asJava).head

    override def insertMany(
      clientSession: ClientSession,
      documents: Seq[? <: A],
      options: InsertManyOptions,
    ): Task[InsertManyResult] =
      wrapped.insertMany(clientSession, documents.asJava, options.toJava).head

    override def deleteOne(filter: Filter): Task[DeleteResult] =
      wrapped.deleteOne(filter).head

    override def deleteOne(filter: Filter, options: DeleteOptions): Task[DeleteResult] =
      wrapped.deleteOne(filter, options.toJava).head

    override def deleteOne(clientSession: ClientSession, filter: Filter): Task[DeleteResult] =
      wrapped.deleteOne(clientSession, filter).head

    override def deleteOne(
      clientSession: ClientSession,
      filter: Filter,
      options: DeleteOptions,
    ): Task[DeleteResult] =
      wrapped.deleteOne(clientSession, filter, options.toJava).head

    override def deleteMany(filter: Filter): Task[DeleteResult] =
      wrapped.deleteMany(filter).head

    override def deleteMany(filter: Filter, options: DeleteOptions): Task[DeleteResult] =
      wrapped.deleteMany(filter, options.toJava).head

    override def deleteMany(clientSession: ClientSession, filter: Filter): Task[DeleteResult] =
      wrapped.deleteMany(clientSession, filter).head

    override def deleteMany(
      clientSession: ClientSession,
      filter: Filter,
      options: DeleteOptions,
    ): Task[DeleteResult] =
      wrapped.deleteMany(clientSession, filter, options.toJava).head

    override def replaceOne(filter: Filter, replacement: A): Task[UpdateResult] =
      wrapped.replaceOne(filter, replacement).head

    override def replaceOne(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
    ): Task[UpdateResult] =
      wrapped.replaceOne(clientSession, filter, replacement).head

    override def replaceOne(
      filter: Filter,
      replacement: A,
      options: ReplaceOptions,
    ): Task[UpdateResult] =
      wrapped.replaceOne(filter, replacement, options.toJava).head

    override def replaceOne(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
      options: ReplaceOptions,
    ): Task[UpdateResult] =
      wrapped.replaceOne(clientSession, filter, replacement, options.toJava).head

    override def updateOne(filter: Filter, update: Update): Task[UpdateResult] =
      wrapped.updateOne(filter, update).head

    override def updateOne(
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(filter, update, options.toJava).head

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter, update).head

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter, update, options.toJava).head

    override def updateOne(filter: Filter, update: Seq[Update]): Task[UpdateResult] =
      wrapped.updateOne(filter, update.asJava).head

    override def updateOne(
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(filter, update.asJava, options.toJava).head

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter, update.asJava).head

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter, update.asJava, options.toJava).head

    override def updateMany(filter: Filter, update: Update): Task[UpdateResult] =
      wrapped.updateMany(filter, update).head

    override def updateMany(
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(filter, update, options.toJava).head

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter, update).head

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter, update, options.toJava).head

    override def updateMany(filter: Filter, update: Seq[Update]): Task[UpdateResult] =
      wrapped.updateMany(filter, update.asJava).head

    override def updateMany(
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(filter, update.asJava, options.toJava).head

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter, update.asJava).head

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter, update.asJava, options.toJava).head

    override def findOneAndDelete(filter: Filter): Task[Option[A]] =
      wrapped.findOneAndDelete(filter).headOption

    override def findOneAndDelete(
      filter: Filter,
      options: FindOneAndDeleteOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndDelete(filter, options.toJava).headOption

    override def findOneAndDelete(clientSession: ClientSession, filter: Filter): Task[Option[A]] =
      wrapped.findOneAndDelete(clientSession, filter).headOption

    override def findOneAndDelete(
      clientSession: ClientSession,
      filter: Filter,
      options: FindOneAndDeleteOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndDelete(clientSession, filter, options.toJava).headOption

    override def findOneAndReplace(filter: Filter, replacement: A): Task[Option[A]] =
      wrapped.findOneAndReplace(filter, replacement).headOption

    override def findOneAndReplace(
      filter: Filter,
      replacement: A,
      options: FindOneAndReplaceOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndReplace(filter, replacement, options.toJava).headOption

    override def findOneAndReplace(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
    ): Task[Option[A]] =
      wrapped.findOneAndReplace(clientSession, filter, replacement).headOption

    override def findOneAndReplace(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
      options: FindOneAndReplaceOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndReplace(clientSession, filter, replacement, options.toJava).headOption

    override def findOneAndUpdate(filter: Filter, update: Update): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter, update).headOption

    override def findOneAndUpdate(
      filter: Filter,
      update: Update,
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter, update, options.toJava).headOption

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(clientSession, filter, update).headOption

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(clientSession, filter, update, options.toJava).headOption

    override def findOneAndUpdate(filter: Filter, update: Seq[Update]): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter, update.asJava).headOption

    override def findOneAndUpdate(
      filter: Filter,
      update: Seq[Update],
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter, update.asJava, options.toJava).headOption

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(clientSession, filter, update.asJava).headOption

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(clientSession, filter, update.asJava, options.toJava).headOption

    override def drop(): Task[Unit] = wrapped.drop().headOption.unit

    override def drop(clientSession: ClientSession): Task[Unit] =
      wrapped.drop(clientSession).headOption.unit

    override def createIndex(key: IndexKey): Task[String] = wrapped.createIndex(key).head

    override def createIndex(key: IndexKey, options: IndexOptions): Task[String] =
      wrapped.createIndex(key, options.toJava).head

    override def createIndex(clientSession: ClientSession, key: IndexKey): Task[String] =
      wrapped.createIndex(clientSession, key).head

    override def createIndex(
      clientSession: ClientSession,
      key: IndexKey,
      options: IndexOptions,
    ): Task[String] =
      wrapped.createIndex(clientSession, key, options.toJava).head

    override def createIndexes(indexes: Index*): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(models).toChunk
    }

    override def createIndexes(
      indexes: Seq[Index],
      createIndexOptions: CreateIndexOptions,
    ): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(models, createIndexOptions.toJava).toChunk
    }

    override def createIndexes(
      clientSession: ClientSession,
      indexes: Index*,
    ): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(clientSession, models).toChunk
    }

    override def createIndexes(
      clientSession: ClientSession,
      indexes: Seq[Index],
      createIndexOptions: CreateIndexOptions,
    ): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(clientSession, models, createIndexOptions.toJava).toChunk
    }

    override def listIndexes(): ListIndexesQuery[Document] =
      ListIndexesQuery(wrapped.listIndexes(implicitly[ClassTag[Document]]))

    override def listIndexes(clientSession: ClientSession): ListIndexesQuery[Document] =
      ListIndexesQuery(wrapped.listIndexes(clientSession, implicitly[ClassTag[Document]]))

    override def dropIndex(indexName: String): Task[Unit] =
      wrapped.dropIndex(indexName).headOption.unit

    override def dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): Task[Unit] =
      wrapped.dropIndex(indexName, dropIndexOptions.toJava).headOption.unit

    override def dropIndex(keys: IndexKey): Task[Unit] =
      wrapped.dropIndex(keys).headOption.unit

    override def dropIndex(keys: IndexKey, dropIndexOptions: DropIndexOptions): Task[Unit] =
      wrapped.dropIndex(keys, dropIndexOptions.toJava).headOption.unit

    override def dropIndex(clientSession: ClientSession, indexName: String): Task[Unit] =
      wrapped.dropIndex(clientSession, indexName).headOption.unit

    override def dropIndex(
      clientSession: ClientSession,
      indexName: String,
      dropIndexOptions: DropIndexOptions,
    ): Task[Unit] =
      wrapped.dropIndex(clientSession, indexName, dropIndexOptions.toJava).headOption.unit

    override def dropIndex(clientSession: ClientSession, keys: IndexKey): Task[Unit] =
      wrapped.dropIndex(clientSession, keys).headOption.unit

    override def dropIndex(
      clientSession: ClientSession,
      keys: IndexKey,
      dropIndexOptions: DropIndexOptions,
    ): Task[Unit] =
      wrapped.dropIndex(clientSession, keys, dropIndexOptions.toJava).headOption.unit

    override def dropIndexes(): Task[Unit] = wrapped.dropIndexes().headOption.unit

    override def dropIndexes(dropIndexOptions: DropIndexOptions): Task[Unit] =
      wrapped.dropIndexes(dropIndexOptions.toJava).headOption.unit

    override def dropIndexes(clientSession: ClientSession): Task[Unit] =
      wrapped.dropIndexes(clientSession).headOption.unit

    override def dropIndexes(
      clientSession: ClientSession,
      dropIndexOptions: DropIndexOptions,
    ): Task[Unit] =
      wrapped.dropIndexes(clientSession, dropIndexOptions.toJava).headOption.unit

    override def renameCollection(newCollectionNamespace: MongoNamespace): Task[Unit] =
      wrapped.renameCollection(newCollectionNamespace).headOption.unit

    override def renameCollection(
      newCollectionNamespace: MongoNamespace,
      options: RenameCollectionOptions,
    ): Task[Unit] =
      wrapped.renameCollection(newCollectionNamespace, options.toJava).headOption.unit

    override def renameCollection(
      clientSession: ClientSession,
      newCollectionNamespace: MongoNamespace,
    ): Task[Unit] =
      wrapped.renameCollection(clientSession, newCollectionNamespace).headOption.unit

    override def renameCollection(
      clientSession: ClientSession,
      newCollectionNamespace: MongoNamespace,
      options: RenameCollectionOptions,
    ): Task[Unit] =
      wrapped.renameCollection(clientSession, newCollectionNamespace, options.toJava).headOption.unit

    override def watch(): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(documentClass))

    override def watch(pipeline: Seq[Aggregation]): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(pipeline.asJava, documentClass))

    override def watch(clientSession: ClientSession): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(clientSession, documentClass))

    override def watch(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(clientSession, pipeline.asJava, documentClass))
  }
}
