package io.github.zeal18.zio.mongodb.driver

import java.io.Closeable

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

import com.mongodb.connection.ClusterDescription
import com.mongodb.reactivestreams.client.ClientSession
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoClient as JMongoClient
import io.github.zeal18.zio.mongodb.bson.codecs.DocumentCodecProvider
import io.github.zeal18.zio.mongodb.bson.codecs.IterableCodecProvider
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.ClientSessionOptions
import io.github.zeal18.zio.mongodb.driver.ConnectionString
import io.github.zeal18.zio.mongodb.driver.MongoClientSettings
import io.github.zeal18.zio.mongodb.driver.MongoDriverInformation
import io.github.zeal18.zio.mongodb.driver.*
import io.github.zeal18.zio.mongodb.driver.query.*
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.configuration.CodecRegistry
import zio.UIO
import zio.ZManaged
import zio.stream.ZStream

/** Companion object for creating new [[MongoClient]] instances
  */
object MongoClient {

  /** Create a default MongoClient at localhost:27017
    *
    * @return MongoClient
    */
  def apply(): MongoClient = apply("mongodb://localhost:27017")

  /** Create a MongoClient instance from a connection string uri
    *
    * @param uri the connection string
    * @return MongoClient
    */
  def apply(uri: String): MongoClient = MongoClient(uri, None)

  /** Create a MongoClient instance from a connection string uri
    *
    * @param uri the connection string
    * @param mongoDriverInformation any driver information to associate with the MongoClient
    * @return MongoClient
    * @note the `mongoDriverInformation` is intended for driver and library authors to associate extra driver metadata with the connections.
    */
  def apply(uri: String, mongoDriverInformation: Option[MongoDriverInformation]): MongoClient =
    apply(
      MongoClientSettings
        .builder()
        .applyConnectionString(new ConnectionString(uri))
        .codecRegistry(DEFAULT_CODEC_REGISTRY)
        .build(),
      mongoDriverInformation,
    )

  /** Create a MongoClient instance from the MongoClientSettings
    *
    * @param clientSettings MongoClientSettings to use for the MongoClient
    * @return MongoClient
    */
  def apply(clientSettings: MongoClientSettings): MongoClient = MongoClient(clientSettings, None)

  /** Create a MongoClient instance from the MongoClientSettings
    *
    * @param clientSettings MongoClientSettings to use for the MongoClient
    * @param mongoDriverInformation any driver information to associate with the MongoClient
    * @return MongoClient
    * @note the `mongoDriverInformation` is intended for driver and library authors to associate extra driver metadata with the connections.
    */
  def apply(
    clientSettings: MongoClientSettings,
    mongoDriverInformation: Option[MongoDriverInformation],
  ): MongoClient = {
    val builder = mongoDriverInformation match {
      case Some(info) => MongoDriverInformation.builder(info)
      case None       => MongoDriverInformation.builder()
    }
    builder.driverName("scala").driverPlatform(s"Scala/${scala.util.Properties.versionString}")
    MongoClient(MongoClients.create(clientSettings, builder.build()))
  }

  val DEFAULT_CODEC_REGISTRY: CodecRegistry = fromRegistries(
    fromProviders(DocumentCodecProvider(), IterableCodecProvider()),
    com.mongodb.MongoClientSettings.getDefaultCodecRegistry,
  )
}

/** A client-side representation of a MongoDB cluster.  Instances can represent either a standalone MongoDB instance, a replica set,
  * or a sharded cluster.  Instance of this class are responsible for maintaining an up-to-date state of the cluster,
  * and possibly cache resources related to this, including background threads for monitoring, and connection pools.
  *
  * Instance of this class server as factories for [[MongoDatabase]] instances.
  *
  * @param wrapped the underlying java MongoClient
  */
case class MongoClient(private val wrapped: JMongoClient) extends Closeable {

  /** Creates a client session.
    *
    * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
    *
    * @note Requires MongoDB 3.6 or greater
    */
  def startSession(): ZManaged[Any, Throwable, ClientSession] =
    wrapped.startSession().getOne.toManaged(s => UIO(s.close()))

  /** Creates a client session.
    *
    * '''Note:''' A ClientSession instance can not be used concurrently in multiple asynchronous operations.
    *
    * @param options  the options for the client session
    * @note Requires MongoDB 3.6 or greater
    */
  def startSession(options: ClientSessionOptions): ZManaged[Any, Throwable, ClientSession] =
    wrapped.startSession(options).getOne.toManaged(s => UIO(s.close()))

  /** Gets the database with the given name.
    *
    * @param name the name of the database
    * @return the database
    */
  def getDatabase(name: String): MongoDatabase = MongoDatabase(wrapped.getDatabase(name))

  /** Close the client, which will close all underlying cached resources, including, for example,
    * sockets and background monitoring threads.
    */
  def close(): Unit = wrapped.close()

  /** Get a list of the database names
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/listDatabases List Databases]]
    * @return an iterable containing all the names of all the databases
    */
  def listDatabaseNames(): ZStream[Any, Throwable, String] = wrapped.listDatabaseNames().stream

  /** Get a list of the database names
    *
    * [[https://www.mongodb.com/docs/manual/reference/commands/listDatabases List Databases]]
    *
    * @param clientSession the client session with which to associate this operation
    * @return an iterable containing all the names of all the databases
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabaseNames(clientSession: ClientSession): ZStream[Any, Throwable, String] =
    wrapped.listDatabaseNames(clientSession).stream

  /** Gets the list of databases
    *
    * @return the fluent list databases interface
    */
  def listDatabases()(implicit
    ct: ClassTag[Document],
  ): ListDatabasesQuery[Document] =
    ListDatabasesQuery(wrapped.listDatabases(ct))

  /** Gets the list of databases
    *
    * @param clientSession the client session with which to associate this operation
    * @return the fluent list databases interface
    * @note Requires MongoDB 3.6 or greater
    */
  def listDatabases(
    clientSession: ClientSession,
  )(implicit ct: ClassTag[Document]): ListDatabasesQuery[Document] =
    ListDatabasesQuery(wrapped.listDatabases(clientSession, ct))

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
  def getClusterDescription: ClusterDescription =
    wrapped.getClusterDescription
}
