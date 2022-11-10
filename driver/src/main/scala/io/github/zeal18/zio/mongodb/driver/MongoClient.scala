package io.github.zeal18.zio.mongodb.driver

import java.io.Closeable

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

import com.mongodb.connection.ClusterDescription
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoClient as JMongoClient
import io.github.zeal18.zio.mongodb.bson.codecs.internal.DocumentCodecProvider
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.ClientSessionOptions
import io.github.zeal18.zio.mongodb.driver.MongoClientSettings
import io.github.zeal18.zio.mongodb.driver.MongoDriverInformation
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation
import io.github.zeal18.zio.mongodb.driver.query.*
import io.github.zeal18.zio.mongodb.driver.reactivestreams.*
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.interop.reactivestreams.*
import zio.stream.ZStream

/** A client-side representation of a MongoDB cluster.  Instances can represent either a standalone MongoDB instance, a replica set,
  * or a sharded cluster.  Instance of this class are responsible for maintaining an up-to-date state of the cluster,
  * and possibly cache resources related to this, including background threads for monitoring, and connection pools.
  */
trait MongoClient {

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

  /** Gets the database with the given name.
    *
    * @param name the name of the database
    * @return the database
    */
  def getDatabase(name: String): MongoDatabase

  /** Get a list of the database names
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/listDatabases List Databases]]
    * @return an iterable containing all the names of all the databases
    */
  def listDatabaseNames(): ZStream[Any, Throwable, String]

  /** Get a list of the database names
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/listDatabases List Databases]]
    *
    * @param clientSession the client session with which to associate this operation
    * @return an iterable containing all the names of all the databases
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabaseNames(clientSession: ClientSession): ZStream[Any, Throwable, String]

  /** Gets the list of databases
    *
    * @return the fluent list databases interface
    */
  def listDatabases(): ListDatabasesQuery[Document]

  /** Gets the list of databases
    *
    * @param clientSession the client session with which to associate this operation
    * @return the fluent list databases interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabases(clientSession: ClientSession): ListDatabasesQuery[Document]

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

  /** Gets the current cluster description.
    *
    * <p>
    * This method will not block, meaning that it may return a { @link ClusterDescription} whose { @code clusterType} is unknown
    * and whose { @link com.mongodb.connection.ServerDescription}s are all in the connecting state.  If the application requires
    * notifications after the driver has connected to a member of the cluster, it should register a { @link ClusterListener} via
    * the { @link ClusterSettings} in { @link com.mongodb.MongoClientSettings}.
    * </p>
    *
    * @return the current cluster description
    * @see ClusterSettings.Builder#addClusterListener(ClusterListener)
    * @see com.mongodb.MongoClientSettings.Builder#applyToClusterSettings(com.mongodb.Block)
    */
  def getClusterDescription: ClusterDescription
}

object MongoClient {

  /** Create a default MongoClient at localhost:27017
    *
    * @return MongoClient
    */
  val localhost: ZLayer[Any, Throwable, MongoClient] =
    MongoClientSettings.localhost >>> MongoClient.live

  /** Create a MongoClient instance from a connection string uri
    *
    * @param uri the connection string
    */
  def live(uri: String): ZLayer[Any, Throwable, MongoClient] =
    MongoClientSettings.fromUri(uri) >>> MongoClient.live

  /** Create a MongoClient instance from the MongoClientSettings
    */
  val live: ZLayer[MongoClientSettings, Throwable, MongoClient] =
    ZLayer.scoped((for {
      clientSettings <- ZIO.service[MongoClientSettings]

      driverInfo = MongoDriverInformation
        .builder()
        .driverName("zio-mongodb")
        .driverPlatform(s"Scala/${scala.util.Properties.versionString}")
        .build()

      mongoClient <- ZIO.acquireRelease(
        ZIO.succeed(Live(MongoClients.create(clientSettings, driverInfo))),
      )(client => ZIO.succeed(client.close()))
    } yield mongoClient))

  private[driver] val DEFAULT_CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(DocumentCodecProvider()),
    com.mongodb.MongoClientSettings.getDefaultCodecRegistry,
  )

  final private case class Live(wrapped: JMongoClient) extends MongoClient with Closeable {
    override def startSession(): ZIO[Scope, Throwable, ClientSession] =
      ZIO.acquireRelease(wrapped.startSession().head)(s => ZIO.succeed(s.close()))

    override def startSession(
      options: ClientSessionOptions,
    ): ZIO[Scope, Throwable, ClientSession] =
      ZIO.acquireRelease(wrapped.startSession(options).head)(s => ZIO.succeed(s.close()))

    override def getDatabase(name: String): MongoDatabase =
      MongoDatabase.Live(this, wrapped.getDatabase(name))

    override def close(): Unit = wrapped.close()

    override def listDatabaseNames(): ZStream[Any, Throwable, String] =
      wrapped.listDatabaseNames().toZIOStream()

    override def listDatabaseNames(clientSession: ClientSession): ZStream[Any, Throwable, String] =
      wrapped.listDatabaseNames(clientSession).toZIOStream()

    override def listDatabases(): ListDatabasesQuery[Document] =
      ListDatabasesQuery(wrapped.listDatabases(implicitly[ClassTag[Document]]))

    override def listDatabases(
      clientSession: ClientSession,
    ): ListDatabasesQuery[Document] =
      ListDatabasesQuery(wrapped.listDatabases(clientSession, implicitly[ClassTag[Document]]))

    override def watch(): ChangeStreamQuery[Document] =
      ChangeStreamQuery(wrapped.watch(implicitly[ClassTag[Document]]))

    override def watch(pipeline: Seq[Aggregation]): ChangeStreamQuery[Document] =
      ChangeStreamQuery(
        wrapped.watch(pipeline.asJava, implicitly[ClassTag[Document]]),
      )

    override def watch(clientSession: ClientSession): ChangeStreamQuery[Document] =
      ChangeStreamQuery(wrapped.watch(clientSession, implicitly[ClassTag[Document]]))

    override def watch(
      clientSession: ClientSession,
      pipeline: Seq[Aggregation],
    ): ChangeStreamQuery[Document] =
      ChangeStreamQuery(
        wrapped.watch(clientSession, pipeline.asJava, implicitly[ClassTag[Document]]),
      )

    override def getClusterDescription: ClusterDescription = wrapped.getClusterDescription
  }
}
