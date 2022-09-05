package io.github.zeal18.zio.mongodb.driver

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

import com.mongodb.client.model.CreateViewOptions
import com.mongodb.reactivestreams.client.MongoDatabase as JMongoDatabase
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.internal.CodecAdapter
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.ClientSession
import io.github.zeal18.zio.mongodb.driver.Document
import io.github.zeal18.zio.mongodb.driver.ReadConcern
import io.github.zeal18.zio.mongodb.driver.ReadPreference
import io.github.zeal18.zio.mongodb.driver.WriteConcern
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation
import io.github.zeal18.zio.mongodb.driver.classTagToClassOf
import io.github.zeal18.zio.mongodb.driver.model.CreateCollectionOptions
import io.github.zeal18.zio.mongodb.driver.query.*
import org.bson.codecs.configuration.CodecRegistries.*
import org.bson.codecs.configuration.CodecRegistry
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.stream.ZStream

trait MongoDatabase {

  /** Gets the name of the database.
    *
    * @return the database name
    */
  def name: String

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

  /** Create a new MongoDatabase instance with a different codec registry.
    *
    * @param codecRegistry the new { @link org.bson.codecs.configuration.CodecRegistry} for the collection
    * @return a new MongoDatabase instance with the different codec registry
    */
  def withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase

  /** Create a new MongoDatabase instance with a different read preference.
    *
    * @param readPreference the new { @link com.mongodb.ReadPreference} for the collection
    * @return a new MongoDatabase instance with the different readPreference
    */
  def withReadPreference(readPreference: ReadPreference): MongoDatabase

  /** Create a new MongoDatabase instance with a different write concern.
    *
    * @param writeConcern the new { @link com.mongodb.WriteConcern} for the collection
    * @return a new MongoDatabase instance with the different writeConcern
    */
  def withWriteConcern(writeConcern: WriteConcern): MongoDatabase

  /** Create a new MongoDatabase instance with a different read concern.
    *
    * @param readConcern the new [[ReadConcern]] for the collection
    * @return a new MongoDatabase instance with the different ReadConcern
    */
  def withReadConcern(readConcern: ReadConcern): MongoDatabase

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

  /** Gets a collection, with a specific default document class.
    *
    * @param collectionName the name of the collection to return
    * @tparam TResult       the type of the class to use instead of [[Document]].
    * @return the collection
    */
  def getCollection[A: ClassTag](collectionName: String)(implicit
    codec: Codec[A],
  ): MongoCollection[A]

  /** Executes command in the context of the current database using the primary server.
    *
    * @param command  the command to be run
    * @return a Observable containing the command result
    */
  def runCommand(command: Bson): Task[Option[Document]]

  /** Executes command in the context of the current database.
    *
    * @param command        the command to be run
    * @param readPreference the [[ReadPreference]] to be used when executing the command
    * @return a Observable containing the command result
    */
  def runCommand(command: Bson, readPreference: ReadPreference): Task[Option[Document]]

  /** Executes command in the context of the current database using the primary server.
    *
    * @param clientSession the client session with which to associate this operation
    * @param command  the command to be run
    * @return a Observable containing the command result
    * @note Requires MongoDB 3.6 or greater
    */
  def runCommand(clientSession: ClientSession, command: Bson): Task[Option[Document]]

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
  ): Task[Option[Document]]

  /** Drops this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database]]
    * @return a Observable identifying when the database has been dropped
    */
  def drop(): Task[Unit]

  /** Drops this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/dropDatabase/#dbcmd.dropDatabase Drop database]]
    * @param clientSession the client session with which to associate this operation
    * @return a Observable identifying when the database has been dropped
    * @note Requires MongoDB 3.6 or greater
    */
  def drop(clientSession: ClientSession): Task[Unit]

  /** Gets the names of all the collections in this database.
    *
    * @return a Observable with all the names of all the collections in this database
    */
  def listCollectionNames(): ZStream[Any, Throwable, String]

  /** Finds all the collections in this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listCollections listCollections]]
    * @return the fluent list collections interface
    */
  def listCollections(): ListCollectionsQuery[Document]

  /** Gets the names of all the collections in this database.
    *
    * @param clientSession the client session with which to associate this operation
    * @return a Observable with all the names of all the collections in this database
    * @note Requires MongoDB 3.6 or greater
    */
  def listCollectionNames(clientSession: ClientSession): ZStream[Any, Throwable, String]

  /** Finds all the collections in this database.
    *
    * [[https://www.mongodb.com/docs/manual/reference/command/listCollections listCollections]]
    * @param clientSession the client session with which to associate this operation
    * @return the fluent list collections interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listCollections(clientSession: ClientSession): ListCollectionsQuery[Document]

  /** Create a new collection with the given name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param collectionName the name for the new collection to create
    * @return a Observable identifying when the collection has been created
    */
  def createCollection(collectionName: String): Task[Unit]

  /** Create a new collection with the selected options
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param collectionName the name for the new collection to create
    * @param options        various options for creating the collection
    * @return a Observable identifying when the collection has been created
    */
  def createCollection(collectionName: String, options: CreateCollectionOptions): Task[Unit]

  /** Create a new collection with the given name.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param clientSession the client session with which to associate this operation
    * @param collectionName the name for the new collection to create
    * @return a Observable identifying when the collection has been created
    * @note Requires MongoDB 3.6 or greater
    */
  def createCollection(clientSession: ClientSession, collectionName: String): Task[Unit]

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
  ): Task[Unit]

  /** Creates a view with the given name, backing collection/view name, and aggregation pipeline that defines the view.
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/create Create Command]]
    * @param viewName the name of the view to create
    * @param viewOn   the backing collection/view for the view
    * @param pipeline the pipeline that defines the view
    * @note Requires MongoDB 3.4 or greater
    */
  def createView(viewName: String, viewOn: String, pipeline: Seq[Aggregation]): Task[Unit]

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
    pipeline: Seq[Aggregation],
    createViewOptions: CreateViewOptions,
  ): Task[Unit]

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
    pipeline: Seq[Aggregation],
  ): Task[Unit]

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
    pipeline: Seq[Aggregation],
    createViewOptions: CreateViewOptions,
  ): Task[Unit]

  /** Creates a change stream for this collection.
    *
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(): ChangeStreamQuery[Document]

  /** Creates a change stream for this collection.
    *
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(pipeline: Seq[Aggregation]): ChangeStreamQuery[Document]

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(clientSession: ClientSession): ChangeStreamQuery[Document]

  /** Creates a change stream for this collection.
    *
    * @param clientSession the client session with which to associate this operation
    * @param pipeline the aggregation pipeline to apply to the change stream
    * @return the change stream observable
    * @note Requires MongoDB 4.0 or greater
    */
  def watch(clientSession: ClientSession, pipeline: Seq[Aggregation]): ChangeStreamQuery[Document]

  /** Aggregates documents according to the specified aggregation pipeline.
    *
    * @param pipeline the aggregate pipeline
    * @return a Observable containing the result of the aggregation operation
    *         [[https://www.mongodb.com/docs/manual/aggregation/ Aggregation]]
    * @note Requires MongoDB 3.6 or greater
    */
  def aggregate(pipeline: Seq[Aggregation]): AggregateQuery[Document]

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
  ): AggregateQuery[Document]
}

object MongoDatabase {

  /** Gets the database with the given name.
    *
    * @param name the name of the database
    * @return the database
    */
  def live(name: String): ZLayer[MongoClient, Nothing, MongoDatabase] =
    ZLayer.fromFunction[MongoClient => MongoDatabase](_.getDatabase(name))

  final private[driver] case class Live(client: MongoClient, wrapped: JMongoDatabase)
      extends MongoDatabase {
    override lazy val name: String = wrapped.getName

    override lazy val codecRegistry: CodecRegistry = wrapped.getCodecRegistry

    override lazy val readPreference: ReadPreference = wrapped.getReadPreference

    override lazy val writeConcern: WriteConcern = wrapped.getWriteConcern

    override lazy val readConcern: ReadConcern = wrapped.getReadConcern

    override def withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase =
      Live(client, wrapped.withCodecRegistry(codecRegistry))

    override def withReadPreference(readPreference: ReadPreference): MongoDatabase =
      Live(client, wrapped.withReadPreference(readPreference))

    override def withWriteConcern(writeConcern: WriteConcern): MongoDatabase =
      Live(client, wrapped.withWriteConcern(writeConcern))

    override def withReadConcern(readConcern: ReadConcern): MongoDatabase =
      Live(client, wrapped.withReadConcern(readConcern))

    override def startSession(): ZIO[Scope, Throwable, ClientSession] = client.startSession()

    override def startSession(
      options: ClientSessionOptions,
    ): ZIO[Scope, Throwable, ClientSession] =
      client.startSession(options)

    override def getCollection[A: ClassTag](
      collectionName: String,
    )(implicit codec: Codec[A]): MongoCollection[A] = {
      val adaptedCodec = CodecAdapter(codec)

      MongoCollection.Live[A](
        this,
        wrapped
          .getCollection(collectionName, adaptedCodec.getEncoderClass())
          .withCodecRegistry(
            fromRegistries(fromCodecs(adaptedCodec), MongoClient.DEFAULT_CODEC_REGISTRY),
          ),
      )
    }

    override def runCommand(command: Bson): Task[Option[Document]] =
      wrapped.runCommand[Document](command, implicitly[ClassTag[Document]]).getOneOpt

    override def runCommand(command: Bson, readPreference: ReadPreference): Task[Option[Document]] =
      wrapped
        .runCommand[Document](command, readPreference, implicitly[ClassTag[Document]])
        .getOneOpt

    override def runCommand(clientSession: ClientSession, command: Bson): Task[Option[Document]] =
      wrapped.runCommand[Document](clientSession, command, implicitly[ClassTag[Document]]).getOneOpt

    override def runCommand(
      clientSession: ClientSession,
      command: Bson,
      readPreference: ReadPreference,
    ): Task[Option[Document]] =
      wrapped
        .runCommand(clientSession, command, readPreference, implicitly[ClassTag[Document]])
        .getOneOpt

    override def drop(): Task[Unit] = wrapped.drop().getOne.unit

    override def drop(clientSession: ClientSession): Task[Unit] =
      wrapped.drop(clientSession).getOne.unit

    override def listCollectionNames(): ZStream[Any, Throwable, String] =
      wrapped.listCollectionNames().stream

    override def listCollections(): ListCollectionsQuery[Document] =
      ListCollectionsQuery(wrapped.listCollections(implicitly[ClassTag[Document]]))

    override def listCollectionNames(
      clientSession: ClientSession,
    ): ZStream[Any, Throwable, String] =
      wrapped.listCollectionNames(clientSession).stream

    override def listCollections(clientSession: ClientSession): ListCollectionsQuery[Document] =
      ListCollectionsQuery(wrapped.listCollections(clientSession, implicitly[ClassTag[Document]]))

    override def createCollection(collectionName: String): Task[Unit] =
      wrapped.createCollection(collectionName).getOneOpt.unit

    override def createCollection(
      collectionName: String,
      options: CreateCollectionOptions,
    ): Task[Unit] =
      wrapped.createCollection(collectionName, options.toJava).getOneOpt.unit

    override def createCollection(
      clientSession: ClientSession,
      collectionName: String,
    ): Task[Unit] =
      wrapped.createCollection(clientSession, collectionName).getOneOpt.unit

    override def createCollection(
      clientSession: ClientSession,
      collectionName: String,
      options: CreateCollectionOptions,
    ): Task[Unit] =
      wrapped.createCollection(clientSession, collectionName, options.toJava).getOneOpt.unit

    override def createView(
      viewName: String,
      viewOn: String,
      pipeline: Seq[Aggregation],
    ): Task[Unit] =
      wrapped.createView(viewName, viewOn, pipeline.map(_.toBson).asJava).getOneOpt.unit

    override def createView(
      viewName: String,
      viewOn: String,
      pipeline: Seq[Aggregation],
      createViewOptions: CreateViewOptions,
    ): Task[Unit] =
      wrapped
        .createView(viewName, viewOn, pipeline.map(_.toBson).asJava, createViewOptions)
        .getOneOpt
        .unit

    override def createView(
      clientSession: ClientSession,
      viewName: String,
      viewOn: String,
      pipeline: Seq[Aggregation],
    ): Task[Unit] =
      wrapped
        .createView(clientSession, viewName, viewOn, pipeline.map(_.toBson).asJava)
        .getOneOpt
        .unit

    override def createView(
      clientSession: ClientSession,
      viewName: String,
      viewOn: String,
      pipeline: Seq[Aggregation],
      createViewOptions: CreateViewOptions,
    ): Task[Unit] =
      wrapped
        .createView(
          clientSession,
          viewName,
          viewOn,
          pipeline.map(_.toBson).asJava,
          createViewOptions,
        )
        .getOneOpt
        .unit

    override def watch(): ChangeStreamQuery[Document] =
      ChangeStreamQuery(wrapped.watch(implicitly[ClassTag[Document]]))

    override def watch(
      pipeline: Seq[Aggregation],
    ): ChangeStreamQuery[Document] =
      ChangeStreamQuery(
        wrapped.watch(pipeline.map(_.toBson).asJava, implicitly[ClassTag[Document]]),
      )

    override def watch(
      clientSession: ClientSession,
    ): ChangeStreamQuery[Document] =
      ChangeStreamQuery(wrapped.watch(clientSession, implicitly[ClassTag[Document]]))

    override def watch(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): ChangeStreamQuery[Document] =
      ChangeStreamQuery(
        wrapped.watch(clientSession, pipeline.map(_.toBson).asJava, implicitly[ClassTag[Document]]),
      )

    override def aggregate(pipeline: Seq[Aggregation]): AggregateQuery[Document] =
      AggregateQuery(
        wrapped.aggregate[Document](pipeline.map(_.toBson).asJava, implicitly[ClassTag[Document]]),
      )

    override def aggregate(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): AggregateQuery[Document] =
      AggregateQuery(
        wrapped.aggregate[Document](
          clientSession,
          pipeline.map(_.toBson).asJava,
          implicitly[ClassTag[Document]],
        ),
      )
  }
}
