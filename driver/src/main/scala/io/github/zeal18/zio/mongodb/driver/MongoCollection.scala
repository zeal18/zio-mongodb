package io.github.zeal18.zio.mongodb.driver

import scala.annotation.nowarn
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
import io.github.zeal18.zio.mongodb.driver.model.bulk.BulkWrite
import io.github.zeal18.zio.mongodb.driver.query.*
import io.github.zeal18.zio.mongodb.driver.result.*
import io.github.zeal18.zio.mongodb.driver.updates.Update
import org.bson.codecs.configuration.CodecRegistries.*
import org.bson.codecs.configuration.CodecRegistry
import zio.Chunk
import zio.Task
import zio.ZLayer
import zio.ZManaged

/** The MongoCollection representation.
  *
  * @param wrapped the underlying java MongoCollection
  * @tparam A The type that this collection will encode documents from and decode documents to.
  */
object MongoCollection {
  trait Service[A] {

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
    def withDocumentClass[C](implicit codec: Codec[C]): MongoCollection.Service[C]

    /** Create a new MongoCollection instance with a different read preference.
      *
      * @param readPreference the new { @link com.mongodb.ReadPreference} for the collection
      * @return a new MongoCollection instance with the different readPreference
      */
    def withReadPreference(readPreference: ReadPreference): MongoCollection.Service[A]

    /** Create a new MongoCollection instance with a different write concern.
      *
      * @param writeConcern the new { @link com.mongodb.WriteConcern} for the collection
      * @return a new MongoCollection instance with the different writeConcern
      */
    def withWriteConcern(writeConcern: WriteConcern): MongoCollection.Service[A]

    /** Create a new MongoCollection instance with a different read concern.
      *
      * @param readConcern the new [[ReadConcern]] for the collection
      * @return a new MongoCollection instance with the different ReadConcern
      */
    def withReadConcern(readConcern: ReadConcern): MongoCollection.Service[A]

    /** Creates a client session.
      *
      * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
      *
      * @note Requires MongoDB 3.6 or greater
      */
    def startSession(): ZManaged[Any, Throwable, ClientSession]

    /** Creates a client session.
      *
      * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
      *
      * @param options  the options for the client session
      * @note Requires MongoDB 3.6 or greater
      */
    def startSession(options: ClientSessionOptions): ZManaged[Any, Throwable, ClientSession]

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
    def aggregate(pipeline: Seq[Aggregation]): AggregateQuery[A]

    /** Aggregates documents according to the specified aggregation pipeline.
      *
      * @param clientSession the client session with which to associate this operation
      * @param pipeline the aggregate pipeline
      * @return a Observable containing the result of the aggregation operation
      *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
      * @note Requires MongoDB 3.6 or greater
      */
    def aggregate(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): AggregateQuery[A]

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

  @nowarn("cat=unused")
  def live[A: izumi.reflect.Tag](name: String)(implicit
    codec: Codec[A],
  ): ZLayer[MongoDatabase, Nothing, MongoCollection[A]] =
    ZLayer.fromFunction[MongoDatabase, MongoCollection.Service[A]](_.get.getCollection[A](name))

  final private[driver] case class Live[A](
    database: MongoDatabase.Service,
    wrapped: JMongoCollection[A],
  ) extends Service[A] {
    override lazy val namespace: MongoNamespace = wrapped.getNamespace

    override lazy val documentClass: Class[A] = wrapped.getDocumentClass

    override lazy val codecRegistry: CodecRegistry = wrapped.getCodecRegistry

    override lazy val readPreference: ReadPreference = wrapped.getReadPreference

    override lazy val writeConcern: WriteConcern = wrapped.getWriteConcern

    override lazy val readConcern: ReadConcern = wrapped.getReadConcern

    override def withDocumentClass[C](implicit codec: Codec[C]): MongoCollection.Service[C] =
      Live[C](
        database,
        wrapped
          .withDocumentClass(codec.getEncoderClass())
          .withCodecRegistry(fromRegistries(fromCodecs(codec), MongoClient.DEFAULT_CODEC_REGISTRY)),
      )

    override def withReadPreference(readPreference: ReadPreference): MongoCollection.Service[A] =
      Live[A](database, wrapped.withReadPreference(readPreference))

    override def withWriteConcern(writeConcern: WriteConcern): MongoCollection.Service[A] =
      Live[A](database, wrapped.withWriteConcern(writeConcern))

    override def startSession(): ZManaged[Any, Throwable, ClientSession] = database.startSession()

    override def startSession(
      options: ClientSessionOptions,
    ): ZManaged[Any, Throwable, ClientSession] =
      database.startSession(options)

    override def withReadConcern(readConcern: ReadConcern): MongoCollection.Service[A] =
      Live[A](database, wrapped.withReadConcern(readConcern))

    override def estimatedDocumentCount(): Task[Long] =
      wrapped.estimatedDocumentCount().getOne.map(identity[Long](_))

    override def estimatedDocumentCount(options: EstimatedDocumentCountOptions): Task[Long] =
      wrapped.estimatedDocumentCount(options.toJava).getOne.map(identity[Long](_))

    override def countDocuments(): Task[Long] =
      wrapped.countDocuments().getOne.map(identity[Long](_))

    override def countDocuments(filter: Filter): Task[Long] =
      wrapped.countDocuments(filter.toBson).getOne.map(identity[Long](_))

    override def countDocuments(filter: Filter, options: CountOptions): Task[Long] =
      wrapped.countDocuments(filter.toBson, options.toJava).getOne.map(identity[Long](_))

    override def countDocuments(clientSession: ClientSession): Task[Long] =
      wrapped.countDocuments(clientSession).getOne.map(identity[Long](_))

    override def countDocuments(clientSession: ClientSession, filter: Filter): Task[Long] =
      wrapped.countDocuments(clientSession, filter.toBson).getOne.map(identity[Long](_))

    override def countDocuments(
      clientSession: ClientSession,
      filter: Filter,
      options: CountOptions,
    ): Task[Long] =
      wrapped
        .countDocuments(clientSession, filter.toBson, options.toJava)
        .getOne
        .map(identity[Long](_))

    override def distinct(fieldName: String): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(fieldName, documentClass))

    override def distinct(fieldName: String, filter: Filter): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(fieldName, filter.toBson, documentClass))

    override def distinct(clientSession: ClientSession, fieldName: String): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(clientSession, fieldName, documentClass))

    override def distinct(
      clientSession: ClientSession,
      fieldName: String,
      filter: Filter,
    ): DistinctQuery[A] =
      DistinctQuery(wrapped.distinct(clientSession, fieldName, filter.toBson, documentClass))

    override def find(): FindQuery[A] =
      FindQuery(wrapped.find[A](documentClass))

    override def find(filter: Filter): FindQuery[A] =
      FindQuery(wrapped.find(filter.toBson, documentClass))

    override def find(clientSession: ClientSession): FindQuery[A] =
      FindQuery(wrapped.find(clientSession, documentClass))

    override def find(clientSession: ClientSession, filter: Filter): FindQuery[A] =
      FindQuery(wrapped.find(clientSession, filter.toBson, documentClass))

    override def aggregate(pipeline: Seq[Aggregation]): AggregateQuery[A] =
      AggregateQuery(wrapped.aggregate[A](pipeline.map(_.toBson).asJava, documentClass))

    override def aggregate(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): AggregateQuery[A] =
      AggregateQuery(
        wrapped.aggregate[A](clientSession, pipeline.map(_.toBson).asJava, documentClass),
      )

    override def bulkWrite(requests: Seq[BulkWrite[A]]): Task[BulkWriteResult] =
      wrapped.bulkWrite(requests.map(_.toJava).asJava).getOne

    override def bulkWrite(
      requests: Seq[BulkWrite[A]],
      options: BulkWriteOptions,
    ): Task[BulkWriteResult] =
      wrapped.bulkWrite(requests.map(_.toJava).asJava, options.toJava).getOne

    override def bulkWrite(
      clientSession: ClientSession,
      requests: Seq[BulkWrite[A]],
    ): Task[BulkWriteResult] =
      wrapped.bulkWrite(clientSession, requests.map(_.toJava).asJava).getOne

    override def bulkWrite(
      clientSession: ClientSession,
      requests: Seq[BulkWrite[A]],
      options: BulkWriteOptions,
    ): Task[BulkWriteResult] =
      wrapped.bulkWrite(clientSession, requests.map(_.toJava).asJava, options.toJava).getOne

    override def insertOne(document: A): Task[InsertOneResult] = wrapped.insertOne(document).getOne

    override def insertOne(document: A, options: InsertOneOptions): Task[InsertOneResult] =
      wrapped.insertOne(document, options.toJava).getOne

    override def insertOne(
      clientSession: ClientSession,
      document: A,
    ): Task[InsertOneResult] =
      wrapped.insertOne(clientSession, document).getOne

    override def insertOne(
      clientSession: ClientSession,
      document: A,
      options: InsertOneOptions,
    ): Task[InsertOneResult] =
      wrapped.insertOne(clientSession, document, options.toJava).getOne

    override def insertMany(documents: Seq[? <: A]): Task[InsertManyResult] =
      wrapped.insertMany(documents.asJava).getOne

    override def insertMany(
      documents: Seq[? <: A],
      options: InsertManyOptions,
    ): Task[InsertManyResult] =
      wrapped.insertMany(documents.asJava, options.toJava).getOne

    override def insertMany(
      clientSession: ClientSession,
      documents: Seq[? <: A],
    ): Task[InsertManyResult] =
      wrapped.insertMany(clientSession, documents.asJava).getOne

    override def insertMany(
      clientSession: ClientSession,
      documents: Seq[? <: A],
      options: InsertManyOptions,
    ): Task[InsertManyResult] =
      wrapped.insertMany(clientSession, documents.asJava, options.toJava).getOne

    override def deleteOne(filter: Filter): Task[DeleteResult] =
      wrapped.deleteOne(filter.toBson).getOne

    override def deleteOne(filter: Filter, options: DeleteOptions): Task[DeleteResult] =
      wrapped.deleteOne(filter.toBson, options.toJava).getOne

    override def deleteOne(clientSession: ClientSession, filter: Filter): Task[DeleteResult] =
      wrapped.deleteOne(clientSession, filter.toBson).getOne

    override def deleteOne(
      clientSession: ClientSession,
      filter: Filter,
      options: DeleteOptions,
    ): Task[DeleteResult] =
      wrapped.deleteOne(clientSession, filter.toBson, options.toJava).getOne

    override def deleteMany(filter: Filter): Task[DeleteResult] =
      wrapped.deleteMany(filter.toBson).getOne

    override def deleteMany(filter: Filter, options: DeleteOptions): Task[DeleteResult] =
      wrapped.deleteMany(filter.toBson, options.toJava).getOne

    override def deleteMany(clientSession: ClientSession, filter: Filter): Task[DeleteResult] =
      wrapped.deleteMany(clientSession, filter.toBson).getOne

    override def deleteMany(
      clientSession: ClientSession,
      filter: Filter,
      options: DeleteOptions,
    ): Task[DeleteResult] =
      wrapped.deleteMany(clientSession, filter.toBson, options.toJava).getOne

    override def replaceOne(filter: Filter, replacement: A): Task[UpdateResult] =
      wrapped.replaceOne(filter.toBson, replacement).getOne

    override def replaceOne(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
    ): Task[UpdateResult] =
      wrapped.replaceOne(clientSession, filter.toBson, replacement).getOne

    override def replaceOne(
      filter: Filter,
      replacement: A,
      options: ReplaceOptions,
    ): Task[UpdateResult] =
      wrapped.replaceOne(filter.toBson, replacement, options.toJava).getOne

    override def replaceOne(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
      options: ReplaceOptions,
    ): Task[UpdateResult] =
      wrapped.replaceOne(clientSession, filter.toBson, replacement, options.toJava).getOne

    override def updateOne(filter: Filter, update: Update): Task[UpdateResult] =
      wrapped.updateOne(filter.toBson, update.toBson).getOne

    override def updateOne(
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(filter.toBson, update.toBson, options.toJava).getOne

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter.toBson, update.toBson).getOne

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter.toBson, update.toBson, options.toJava).getOne

    override def updateOne(filter: Filter, update: Seq[Update]): Task[UpdateResult] =
      wrapped.updateOne(filter.toBson, update.map(_.toBson).asJava).getOne

    override def updateOne(
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateOne(filter.toBson, update.map(_.toBson).asJava, options.toJava).getOne

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
    ): Task[UpdateResult] =
      wrapped.updateOne(clientSession, filter.toBson, update.map(_.toBson).asJava).getOne

    override def updateOne(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped
        .updateOne(clientSession, filter.toBson, update.map(_.toBson).asJava, options.toJava)
        .getOne

    override def updateMany(filter: Filter, update: Update): Task[UpdateResult] =
      wrapped.updateMany(filter.toBson, update.toBson).getOne

    override def updateMany(
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(filter.toBson, update.toBson, options.toJava).getOne

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter.toBson, update.toBson).getOne

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter.toBson, update.toBson, options.toJava).getOne

    override def updateMany(filter: Filter, update: Seq[Update]): Task[UpdateResult] =
      wrapped.updateMany(filter.toBson, update.map(_.toBson).asJava).getOne

    override def updateMany(
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped.updateMany(filter.toBson, update.map(_.toBson).asJava, options.toJava).getOne

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
    ): Task[UpdateResult] =
      wrapped.updateMany(clientSession, filter.toBson, update.map(_.toBson).asJava).getOne

    override def updateMany(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
      options: UpdateOptions,
    ): Task[UpdateResult] =
      wrapped
        .updateMany(clientSession, filter.toBson, update.map(_.toBson).asJava, options.toJava)
        .getOne

    override def findOneAndDelete(filter: Filter): Task[Option[A]] =
      wrapped.findOneAndDelete(filter.toBson).getOneOpt

    override def findOneAndDelete(
      filter: Filter,
      options: FindOneAndDeleteOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndDelete(filter.toBson, options.toJava).getOneOpt

    override def findOneAndDelete(clientSession: ClientSession, filter: Filter): Task[Option[A]] =
      wrapped.findOneAndDelete(clientSession, filter.toBson).getOneOpt

    override def findOneAndDelete(
      clientSession: ClientSession,
      filter: Filter,
      options: FindOneAndDeleteOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndDelete(clientSession, filter.toBson, options.toJava).getOneOpt

    override def findOneAndReplace(filter: Filter, replacement: A): Task[Option[A]] =
      wrapped.findOneAndReplace(filter.toBson, replacement).getOneOpt

    override def findOneAndReplace(
      filter: Filter,
      replacement: A,
      options: FindOneAndReplaceOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndReplace(filter.toBson, replacement, options.toJava).getOneOpt

    override def findOneAndReplace(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
    ): Task[Option[A]] =
      wrapped.findOneAndReplace(clientSession, filter.toBson, replacement).getOneOpt

    override def findOneAndReplace(
      clientSession: ClientSession,
      filter: Filter,
      replacement: A,
      options: FindOneAndReplaceOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndReplace(clientSession, filter.toBson, replacement, options.toJava).getOneOpt

    override def findOneAndUpdate(filter: Filter, update: Update): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter.toBson, update.toBson).getOneOpt

    override def findOneAndUpdate(
      filter: Filter,
      update: Update,
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter.toBson, update.toBson, options.toJava).getOneOpt

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(clientSession, filter.toBson, update.toBson).getOneOpt

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Update,
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped
        .findOneAndUpdate(clientSession, filter.toBson, update.toBson, options.toJava)
        .getOneOpt

    override def findOneAndUpdate(filter: Filter, update: Seq[Update]): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter.toBson, update.map(_.toBson).asJava).getOneOpt

    override def findOneAndUpdate(
      filter: Filter,
      update: Seq[Update],
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(filter.toBson, update.map(_.toBson).asJava, options.toJava).getOneOpt

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
    ): Task[Option[A]] =
      wrapped.findOneAndUpdate(clientSession, filter.toBson, update.map(_.toBson).asJava).getOneOpt

    override def findOneAndUpdate(
      clientSession: ClientSession,
      filter: Filter,
      update: Seq[Update],
      options: FindOneAndUpdateOptions,
    ): Task[Option[A]] =
      wrapped
        .findOneAndUpdate(clientSession, filter.toBson, update.map(_.toBson).asJava, options.toJava)
        .getOneOpt

    override def drop(): Task[Unit] = wrapped.drop().getOne.unit

    override def drop(clientSession: ClientSession): Task[Unit] =
      wrapped.drop(clientSession).getOne.unit

    override def createIndex(key: IndexKey): Task[String] = wrapped.createIndex(key.toBson).getOne

    override def createIndex(key: IndexKey, options: IndexOptions): Task[String] =
      wrapped.createIndex(key.toBson, options.toJava).getOne

    override def createIndex(clientSession: ClientSession, key: IndexKey): Task[String] =
      wrapped.createIndex(clientSession, key.toBson).getOne

    override def createIndex(
      clientSession: ClientSession,
      key: IndexKey,
      options: IndexOptions,
    ): Task[String] =
      wrapped.createIndex(clientSession, key.toBson, options.toJava).getOne

    override def createIndexes(indexes: Index*): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(models).stream.runCollect
    }

    override def createIndexes(
      indexes: Seq[Index],
      createIndexOptions: CreateIndexOptions,
    ): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(models, createIndexOptions.toJava).stream.runCollect
    }

    override def createIndexes(
      clientSession: ClientSession,
      indexes: Index*,
    ): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(clientSession, models).stream.runCollect
    }

    override def createIndexes(
      clientSession: ClientSession,
      indexes: Seq[Index],
      createIndexOptions: CreateIndexOptions,
    ): Task[Chunk[String]] = {
      val models = indexes.map(_.toJava).asJava

      wrapped.createIndexes(clientSession, models, createIndexOptions.toJava).stream.runCollect
    }

    override def listIndexes(): ListIndexesQuery[Document] =
      ListIndexesQuery(wrapped.listIndexes(implicitly[ClassTag[Document]]))

    override def listIndexes(clientSession: ClientSession): ListIndexesQuery[Document] =
      ListIndexesQuery(wrapped.listIndexes(clientSession, implicitly[ClassTag[Document]]))

    override def dropIndex(indexName: String): Task[Unit] =
      wrapped.dropIndex(indexName).getOneOpt.unit

    override def dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): Task[Unit] =
      wrapped.dropIndex(indexName, dropIndexOptions.toJava).getOneOpt.unit

    override def dropIndex(keys: IndexKey): Task[Unit] =
      wrapped.dropIndex(keys.toBson).getOneOpt.unit

    override def dropIndex(keys: IndexKey, dropIndexOptions: DropIndexOptions): Task[Unit] =
      wrapped.dropIndex(keys.toBson, dropIndexOptions.toJava).getOneOpt.unit

    override def dropIndex(clientSession: ClientSession, indexName: String): Task[Unit] =
      wrapped.dropIndex(clientSession, indexName).getOneOpt.unit

    override def dropIndex(
      clientSession: ClientSession,
      indexName: String,
      dropIndexOptions: DropIndexOptions,
    ): Task[Unit] =
      wrapped.dropIndex(clientSession, indexName, dropIndexOptions.toJava).getOneOpt.unit

    override def dropIndex(clientSession: ClientSession, keys: IndexKey): Task[Unit] =
      wrapped.dropIndex(clientSession, keys.toBson).getOneOpt.unit

    override def dropIndex(
      clientSession: ClientSession,
      keys: IndexKey,
      dropIndexOptions: DropIndexOptions,
    ): Task[Unit] =
      wrapped.dropIndex(clientSession, keys.toBson, dropIndexOptions.toJava).getOneOpt.unit

    override def dropIndexes(): Task[Unit] = wrapped.dropIndexes().getOneOpt.unit

    override def dropIndexes(dropIndexOptions: DropIndexOptions): Task[Unit] =
      wrapped.dropIndexes(dropIndexOptions.toJava).getOneOpt.unit

    override def dropIndexes(clientSession: ClientSession): Task[Unit] =
      wrapped.dropIndexes(clientSession).getOneOpt.unit

    override def dropIndexes(
      clientSession: ClientSession,
      dropIndexOptions: DropIndexOptions,
    ): Task[Unit] =
      wrapped.dropIndexes(clientSession, dropIndexOptions.toJava).getOneOpt.unit

    override def renameCollection(newCollectionNamespace: MongoNamespace): Task[Unit] =
      wrapped.renameCollection(newCollectionNamespace).getOneOpt.unit

    override def renameCollection(
      newCollectionNamespace: MongoNamespace,
      options: RenameCollectionOptions,
    ): Task[Unit] =
      wrapped.renameCollection(newCollectionNamespace, options.toJava).getOneOpt.unit

    override def renameCollection(
      clientSession: ClientSession,
      newCollectionNamespace: MongoNamespace,
    ): Task[Unit] =
      wrapped.renameCollection(clientSession, newCollectionNamespace).getOneOpt.unit

    override def renameCollection(
      clientSession: ClientSession,
      newCollectionNamespace: MongoNamespace,
      options: RenameCollectionOptions,
    ): Task[Unit] =
      wrapped.renameCollection(clientSession, newCollectionNamespace, options.toJava).getOneOpt.unit

    override def watch(): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(documentClass))

    override def watch(pipeline: Seq[Aggregation]): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(pipeline.map(_.toBson).asJava, documentClass))

    override def watch(clientSession: ClientSession): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(clientSession, documentClass))

    override def watch(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): ChangeStreamQuery[A] =
      ChangeStreamQuery(wrapped.watch(clientSession, pipeline.map(_.toBson).asJava, documentClass))
  }
}
