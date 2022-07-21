package io.github.zeal18.zio.mongodb.driver

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

import com.mongodb.client.model.CreateCollectionOptions
import com.mongodb.client.model.CreateViewOptions
import com.mongodb.reactivestreams.client.MongoDatabase as JMongoDatabase
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.ClientSession
import io.github.zeal18.zio.mongodb.driver.Document
import io.github.zeal18.zio.mongodb.driver.ReadConcern
import io.github.zeal18.zio.mongodb.driver.ReadPreference
import io.github.zeal18.zio.mongodb.driver.WriteConcern
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.classTagToClassOf
import io.github.zeal18.zio.mongodb.driver.query.*
import org.bson.codecs.configuration.CodecRegistries.*
import org.bson.codecs.configuration.CodecRegistry
import zio.Task
import zio.stream.ZStream

/** The MongoDatabase representation.
  *
  * @param wrapped the underlying java MongoDatabase
  */
case class MongoDatabase(private[driver] val wrapped: JMongoDatabase) {

  /** Gets the name of the database.
    *
    * @return the database name
    */
  lazy val name: String = wrapped.getName

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

  /** Create a new MongoDatabase instance with a different codec registry.
    *
    * @param codecRegistry the new { @link org.bson.codecs.configuration.CodecRegistry} for the collection
    * @return a new MongoDatabase instance with the different codec registry
    */
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase =
    MongoDatabase(wrapped.withCodecRegistry(codecRegistry))

  /** Create a new MongoDatabase instance with a different read preference.
    *
    * @param readPreference the new { @link com.mongodb.ReadPreference} for the collection
    * @return a new MongoDatabase instance with the different readPreference
    */
  def withReadPreference(readPreference: ReadPreference): MongoDatabase =
    MongoDatabase(wrapped.withReadPreference(readPreference))

  /** Create a new MongoDatabase instance with a different write concern.
    *
    * @param writeConcern the new { @link com.mongodb.WriteConcern} for the collection
    * @return a new MongoDatabase instance with the different writeConcern
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoDatabase =
    MongoDatabase(wrapped.withWriteConcern(writeConcern))

  /** Create a new MongoDatabase instance with a different read concern.
    *
    * @param readConcern the new [[ReadConcern]] for the collection
    * @return a new MongoDatabase instance with the different ReadConcern
    */
  def withReadConcern(readConcern: ReadConcern): MongoDatabase =
    MongoDatabase(wrapped.withReadConcern(readConcern))

  /** Gets a collection, with a specific default document class.
    *
    * @param collectionName the name of the collection to return
    * @tparam TResult       the type of the class to use instead of [[Document]].
    * @return the collection
    */
  def getCollection[A](
    collectionName: String,
  )(implicit codec: Codec[A]): MongoCollection[A] =
    MongoCollection(
      wrapped
        .getCollection(collectionName, codec.getEncoderClass())
        .withCodecRegistry(fromRegistries(fromCodecs(codec), MongoClient.DEFAULT_CODEC_REGISTRY)),
    )

  /** Executes command in the context of the current database using the primary server.
    *
    * @param command  the command to be run
    * @return a Observable containing the command result
    */
  def runCommand(command: Bson)(implicit ct: ClassTag[Document]): Task[Option[Document]] =
    wrapped.runCommand[Document](command, ct).getOneOpt

  /** Executes command in the context of the current database.
    *
    * @param command        the command to be run
    * @param readPreference the [[ReadPreference]] to be used when executing the command
    * @return a Observable containing the command result
    */
  def runCommand(command: Bson, readPreference: ReadPreference)(implicit
    ct: ClassTag[Document],
  ): Task[Option[Document]] =
    wrapped.runCommand[Document](command, readPreference, ct).getOneOpt

  /** Executes command in the context of the current database using the primary server.
    *
    * @param clientSession the client session with which to associate this operation
    * @param command  the command to be run
    * @return a Observable containing the command result
    * @note Requires MongoDB 3.6 or greater
    */
  def runCommand(clientSession: ClientSession, command: Bson)(implicit
    ct: ClassTag[Document],
  ): Task[Option[Document]] =
    wrapped.runCommand[Document](clientSession, command, ct).getOneOpt

  /** Executes command in the context of the current database.
    *
    * @param command        the command to be run
    * @param readPreference the [[ReadPreference]] to be used when executing the command
    * @tparam TResult       the type of the class to use instead of [[Document]].
    * @return a Observable containing the command result
    * @note Requires MongoDB 3.6 or greater
    */
  def runCommand(
    clientSession: ClientSession,
    command: Bson,
    readPreference: ReadPreference,
  )(implicit
    ct: ClassTag[Document],
  ): Task[Option[Document]] =
    wrapped.runCommand(clientSession, command, readPreference, ct).getOneOpt

  /** Drops this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database]]
    * @return a Observable identifying when the database has been dropped
    */
  def drop(): Task[Unit] = wrapped.drop().getOne.unit

  /** Drops this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database]]
    * @param clientSession the client session with which to associate this operation
    * @return a Observable identifying when the database has been dropped
    * @note Requires MongoDB 3.6 or greater
    */
  def drop(clientSession: ClientSession): Task[Unit] = wrapped.drop(clientSession).getOne.unit

  /** Gets the names of all the collections in this database.
    *
    * @return a Observable with all the names of all the collections in this database
    */
  def listCollectionNames(): ZStream[Any, Throwable, String] = wrapped.listCollectionNames().stream

  /** Finds all the collections in this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listCollections listCollections]]
    * @return the fluent list collections interface
    */
  def listCollections()(implicit
    ct: ClassTag[Document],
  ): ListCollectionsQuery[Document] =
    ListCollectionsQuery(wrapped.listCollections(ct))

  /** Gets the names of all the collections in this database.
    *
    * @param clientSession the client session with which to associate this operation
    * @return a Observable with all the names of all the collections in this database
    * @note Requires MongoDB 3.6 or greater
    */
  def listCollectionNames(clientSession: ClientSession): ZStream[Any, Throwable, String] =
    wrapped.listCollectionNames(clientSession).stream

  /** Finds all the collections in this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listCollections listCollections]]
    * @param clientSession the client session with which to associate this operation
    * @return the fluent list collections interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listCollections(clientSession: ClientSession)(implicit
    ct: ClassTag[Document],
  ): ListCollectionsQuery[Document] =
    ListCollectionsQuery(wrapped.listCollections(clientSession, ct))

  /** Create a new collection with the given name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param collectionName the name for the new collection to create
    * @return a Observable identifying when the collection has been created
    */
  def createCollection(collectionName: String): Task[Unit] =
    wrapped.createCollection(collectionName).getOneOpt.unit

  /** Create a new collection with the selected options
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param collectionName the name for the new collection to create
    * @param options        various options for creating the collection
    * @return a Observable identifying when the collection has been created
    */
  def createCollection(
    collectionName: String,
    options: CreateCollectionOptions,
  ): Task[Unit] =
    wrapped.createCollection(collectionName, options).getOneOpt.unit

  /** Create a new collection with the given name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param clientSession the client session with which to associate this operation
    * @param collectionName the name for the new collection to create
    * @return a Observable identifying when the collection has been created
    * @note Requires MongoDB 3.6 or greater
    */
  def createCollection(
    clientSession: ClientSession,
    collectionName: String,
  ): Task[Unit] =
    wrapped.createCollection(clientSession, collectionName).getOneOpt.unit

  /** Create a new collection with the selected options
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param clientSession the client session with which to associate this operation
    * @param collectionName the name for the new collection to create
    * @param options        various options for creating the collection
    * @return a Observable identifying when the collection has been created
    * @note Requires MongoDB 3.6 or greater
    */
  def createCollection(
    clientSession: ClientSession,
    collectionName: String,
    options: CreateCollectionOptions,
  ): Task[Unit] =
    wrapped.createCollection(clientSession, collectionName, options).getOneOpt.unit

  /** Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param viewName the name of the view to create
    * @param viewOn   the backing collection/view for the view
    * @param pipeline the pipeline that defines the view
    * @note Requires MongoDB 3.4 or greater
    */
  def createView(viewName: String, viewOn: String, pipeline: Seq[Bson]): Task[Unit] =
    wrapped.createView(viewName, viewOn, pipeline.asJava).getOneOpt.unit

  /** Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param viewName          the name of the view to create
    * @param viewOn            the backing collection/view for the view
    * @param pipeline          the pipeline that defines the view
    * @param createViewOptions various options for creating the view
    * @note Requires MongoDB 3.4 or greater
    */
  def createView(
    viewName: String,
    viewOn: String,
    pipeline: Seq[Bson],
    createViewOptions: CreateViewOptions,
  ): Task[Unit] =
    wrapped.createView(viewName, viewOn, pipeline.asJava, createViewOptions).getOneOpt.unit

  /** Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param clientSession the client session with which to associate this operation
    * @param viewName the name of the view to create
    * @param viewOn   the backing collection/view for the view
    * @param pipeline the pipeline that defines the view
    * @note Requires MongoDB 3.6 or greater
    */
  def createView(
    clientSession: ClientSession,
    viewName: String,
    viewOn: String,
    pipeline: Seq[Bson],
  ): Task[Unit] =
    wrapped.createView(clientSession, viewName, viewOn, pipeline.asJava).getOneOpt.unit

  /** Creates a view with the given name, backing collection/view name, aggregation pipeline, and options that defines the view.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param clientSession the client session with which to associate this operation
    * @param viewName          the name of the view to create
    * @param viewOn            the backing collection/view for the view
    * @param pipeline          the pipeline that defines the view
    * @param createViewOptions various options for creating the view
    * @note Requires MongoDB 3.6 or greater
    */
  def createView(
    clientSession: ClientSession,
    viewName: String,
    viewOn: String,
    pipeline: Seq[Bson],
    createViewOptions: CreateViewOptions,
  ): Task[Unit] =
    wrapped
      .createView(clientSession, viewName, viewOn, pipeline.asJava, createViewOptions)
      .getOneOpt
      .unit

  /** Creates a change stream for this collection.
    *
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch()(implicit ct: ClassTag[Document]): ChangeStreamQuery[Document] =
    ChangeStreamQuery(wrapped.watch(ct))

  /** Creates a change stream for this collection.
    *
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(
    pipeline: Seq[Bson],
  )(implicit ct: ClassTag[Document]): ChangeStreamQuery[Document] =
    ChangeStreamQuery(wrapped.watch(pipeline.asJava, ct))

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(
    clientSession: ClientSession,
  )(implicit ct: ClassTag[Document]): ChangeStreamQuery[Document] =
    ChangeStreamQuery(wrapped.watch(clientSession, ct))

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(
    clientSession: ClientSession,
    pipeline: Seq[Bson],
  )(implicit ct: ClassTag[Document]): ChangeStreamQuery[Document] =
    ChangeStreamQuery(wrapped.watch(clientSession, pipeline.asJava, ct))

  /** Aggregates documents according to the specified aggregation pipeline.
    *
    * @param pipeline the aggregate pipeline
    * @return a Observable containing the result of the aggregation operation
    *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    * @note Requires MongoDB 3.6 or greater
    */
  def aggregate(
    pipeline: Seq[Bson],
  )(implicit ct: ClassTag[Document]): AggregateQuery[Document] =
    AggregateQuery(wrapped.aggregate[Document](pipeline.asJava, ct))

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
    pipeline: Seq[Bson],
  )(implicit ct: ClassTag[Document]): AggregateQuery[Document] =
    AggregateQuery(wrapped.aggregate[Document](clientSession, pipeline.asJava, ct))
}
