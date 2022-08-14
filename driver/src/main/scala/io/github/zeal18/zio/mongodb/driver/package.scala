package io.github.zeal18.zio.mongodb

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.reactivestreams.Publisher
import zio.Has
import zio.Task
import zio.interop.reactivestreams.*
import zio.stream.ZStream

package object driver {
  implicit class PublisherOps[A](private val publisher: Publisher[A]) extends AnyVal {
    def stream: ZStream[Any, Throwable, A] = publisher.toStream()
    def getOneOpt: Task[Option[A]]         = publisher.toStream(qSize = 2).runHead
    def getOne: Task[A] =
      getOneOpt.someOrFail(new IllegalStateException("Expected one value but received nothing"))
  }

  type MongoClient        = Has[MongoClient.Service]
  type MongoDatabase      = Has[MongoDatabase.Service]
  type MongoCollection[A] = Has[MongoCollection.Service[A]]

  /** An immutable Document implementation.
    *
    * A strictly typed `Map[String, BsonValue]` like structure that traverses the elements in insertion order. Unlike native scala maps there
    * is no variance in the value type and it always has to be a `BsonValue`.
    */
  type Document = bson.Document

  /** An immutable Document implementation.
    *
    * A strictly typed `Map[String, BsonValue]` like structure that traverses the elements in insertion order. Unlike native scala maps there
    * is no variance in the value type and it always has to be a `BsonValue`.
    */
  val Document = bson.Document

  /** The Connection String
    */
  type ConnectionString = com.mongodb.ConnectionString

  /** Connection String companion object
    */
  object ConnectionString {
    def apply(connectionString: String): ConnectionString =
      new com.mongodb.ConnectionString(connectionString)
  }

  /** The result of a successful bulk write operation.
    */
  type BulkWriteResult = com.mongodb.bulk.BulkWriteResult

  /** Represents the  commit quorum specifies how many data-bearing members of a replica set, including the primary, must
    * complete the index builds successfully before the primary marks the indexes as ready.
    */
  type CreateIndexCommitQuorum = com.mongodb.CreateIndexCommitQuorum

  /** A MongoDB namespace, which includes a database name and collection name.
    */
  type MongoNamespace = com.mongodb.MongoNamespace

  /** The readConcern option allows clients to choose a level of isolation for their reads.
    *
    * @see [[ReadConcern]]
    */
  type ReadConcernLevel = com.mongodb.ReadConcernLevel

  /** Represents preferred replica set members to which a query or command can be sent.
    */
  type ReadPreference = com.mongodb.ReadPreference

  /** Represents ReadPreferences that can be combined with tags
    */
  type TaggableReadPreference = com.mongodb.TaggableReadPreference

  /** A replica set tag
    */
  type Tag = com.mongodb.Tag

  /** An immutable set of tags, used to select members of a replica set to use for read operations.
    */
  type TagSet = com.mongodb.TagSet

  /** Controls the acknowledgment of write operations with various options.
    */
  type WriteConcern = com.mongodb.WriteConcern

  /** Controls the level of isolation for reads.
    */
  type ReadConcern = com.mongodb.ReadConcern

  /** The result of a successful write operation.  If the write was unacknowledged, then `wasAcknowledged` will return false and all
    * other methods with throw `MongoUnacknowledgedWriteException`.
    *
    * @see [[WriteConcern]]
    */
  type WriteConcernResult = com.mongodb.WriteConcernResult

  /** Represents the details of a write error , e.g. a duplicate key error
    */
  type WriteError = com.mongodb.WriteError

  /** Represents credentials to authenticate to a MongoDB server,as well as the source of the credentials and the authentication mechanism to
    * use.
    */
  type MongoCredential = com.mongodb.MongoCredential

  /** Represents the location of a MongoDB server
    */
  type ServerAddress = com.mongodb.ServerAddress

  /** The MongoDriverInformation class allows driver and library authors to add extra information about their library. This information is
    * then available in the MongoD/MongoS logs.
    *
    * The following metadata can be included when creating a `MongoClient`.
    *
    *  - The driver name. Eg: `mongo-scala-driver`
    *  - The driver version. Eg: `1.2.0`
    *  - Extra platform information. Eg: `Scala 2.11`
    *
    * '''Note:''' Library authors are responsible for accepting `MongoDriverInformation` from external libraries using their library.
    * Also all the meta data is limited to 512 bytes and any excess data will be truncated.
    *
    * @note Requires MongoDB 3.4 or greater
    */
  type MongoDriverInformation = com.mongodb.MongoDriverInformation

  /** Various settings to control the behavior of a `MongoClient`.
    */
  type MongoClientSettings = com.mongodb.MongoClientSettings

  /** A Client Session
    */
  type ClientSession = com.mongodb.reactivestreams.client.ClientSession

  /** Options for creating ClientSessions
    */
  type ClientSessionOptions = com.mongodb.ClientSessionOptions

  /** Options for transactions
    */
  type TransactionOptions = com.mongodb.TransactionOptions

  /** Options for creating MongoCompressor
    */
  type MongoCompressor = com.mongodb.MongoCompressor

  // MongoException Aliases
  /** Top level Exception for all Exceptions, server-side or client-side, that come from the driver.
    */
  type MongoException = com.mongodb.MongoException

  /** Top level Exception for all Exceptions, server-side or client-side, that come from the driver.
    */
  object MongoException {

    /** An error label indicating that the exception can be treated as a transient transaction error.
      */
    val TRANSIENT_TRANSACTION_ERROR_LABEL: String =
      com.mongodb.MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL

    /** An error label indicating that the exception can be treated as an unknown transaction commit result.
      */
    val UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL: String =
      com.mongodb.MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL
  }

  /** An exception that represents all errors associated with a bulk write operation.
    */
  type MongoBulkWriteException = com.mongodb.MongoBulkWriteException

  /** An exception indicating that a failure occurred when running a `\$changeStream`.
    */
  type MongoChangeStreamException = com.mongodb.MongoChangeStreamException

  /** A base class for exceptions indicating a failure condition with the MongoClient.
    */
  type MongoClientException = com.mongodb.MongoClientException

  /** An exception indicating that a command sent to a MongoDB server returned a failure.
    */
  type MongoCommandException = com.mongodb.MongoCommandException

  /** Subclass of [[MongoException]] representing a cursor-not-found exception.
    */
  type MongoCursorNotFoundException = com.mongodb.MongoCursorNotFoundException

  /** Subclass of [[MongoClientException]] representing a server-unavailable exception.
    */
  type MongoServerUnavailableException = com.mongodb.MongoServerUnavailableException

  /** Exception indicating that the execution of the current operation timed out as a result of the maximum operation time being exceeded.
    */
  type MongoExecutionTimeoutException = com.mongodb.MongoExecutionTimeoutException

  /** An exception indicating that this version of the driver is not compatible with at least one of the servers that it is currently
    * connected to.
    */
  type MongoIncompatibleDriverException = com.mongodb.MongoIncompatibleDriverException

  /** A Mongo exception internal to the driver, not carrying any error code.
    */
  type MongoInternalException = com.mongodb.MongoInternalException

  /** A non-checked exception indicating that the driver has been interrupted by a call to `Thread.interrupt`.
    */
  type MongoInterruptedException = com.mongodb.MongoInterruptedException

  /** An exception indicating that the server is a member of a replica set but is in recovery mode, and therefore refused to execute
    * the operation. This can happen when a server is starting up and trying to join the replica set.
    */
  type MongoNodeIsRecoveringException = com.mongodb.MongoNodeIsRecoveringException

  /** An exception indicating that the server is a member of a replica set but is not the primary, and therefore refused to execute either a
    * write operation or a read operation that required a primary.  This can happen during a replica set election.
    */
  type MongoNotPrimaryException = com.mongodb.MongoNotPrimaryException

  /** An exception indicating that a query operation failed on the server.
    */
  type MongoQueryException = com.mongodb.MongoQueryException

  /** This exception is thrown when there is an error reported by the underlying client authentication mechanism.
    */
  type MongoSecurityException = com.mongodb.MongoSecurityException

  /** An exception indicating that some error has been raised by a MongoDB server in response to an operation.
    */
  type MongoServerException = com.mongodb.MongoServerException

  /** This exception is thrown when trying to read or write from a closed socket.
    */
  type MongoSocketClosedException = com.mongodb.MongoSocketClosedException

  /** Subclass of [[MongoException]] representing a network-related exception
    */
  type MongoSocketException = com.mongodb.MongoSocketException

  /** This exception is thrown when there is an exception opening a Socket.
    */
  type MongoSocketOpenException = com.mongodb.MongoSocketOpenException

  /** This exception is thrown when there is an exception reading a response from a Socket.
    */
  type MongoSocketReadException = com.mongodb.MongoSocketReadException

  /** This exception is thrown when there is a timeout reading a response from the socket.
    */
  type MongoSocketReadTimeoutException = com.mongodb.MongoSocketReadTimeoutException

  /** This exception is thrown when there is an exception writing a response to a Socket.
    */
  type MongoSocketWriteException = com.mongodb.MongoSocketWriteException

  /** An exception indicating that the driver has timed out waiting for either a server or a connection to become available.
    */
  type MongoTimeoutException = com.mongodb.MongoTimeoutException

  /** An exception indicating a failure to apply the write concern to the requested write operation
    *
    * @see [[WriteConcern]]
    */
  type MongoWriteConcernException = com.mongodb.MongoWriteConcernException

  /** An exception indicating the failure of a write operation.
    */
  type MongoWriteException = com.mongodb.MongoWriteException

  /** An exception representing an error reported due to a write failure.
    */
  type WriteConcernException = com.mongodb.WriteConcernException

  /** Subclass of [[WriteConcernException]] representing a duplicate key exception
    */
  type DuplicateKeyException = com.mongodb.DuplicateKeyException

  /** An exception that may happen usually as a result of another thread clearing a connection pool.
    * Such clearing usually itself happens as a result of an exception.
    */
  type MongoConnectionPoolClearedException = com.mongodb.MongoConnectionPoolClearedException

  /** The client-side automatic encryption settings. Client side encryption enables an application to specify what fields in a collection
    * must be encrypted, and the driver automatically encrypts commands sent to MongoDB and decrypts responses.
    *
    * Automatic encryption is an enterprise only feature that only applies to operations on a collection. Automatic encryption is not
    * supported for operations on a database or view and will result in error. To bypass automatic encryption,
    * set bypassAutoEncryption=true in `AutoEncryptionSettings`.
    *
    * Explicit encryption/decryption and automatic decryption is a community feature, enabled with the new
    * `com.mongodb.client.vault.ClientEncryption` type.
    *
    * A MongoClient configured with bypassAutoEncryption=true will still automatically decrypt.
    *
    * If automatic encryption fails on an operation, use a MongoClient configured with bypassAutoEncryption=true and use
    * ClientEncryption#encrypt to manually encrypt values.
    *
    * Enabling client side encryption reduces the maximum document and message size (using a maxBsonObjectSize of 2MiB and
    * maxMessageSizeBytes of 6MB) and may have a negative performance impact.
    *
    * Automatic encryption requires the authenticated user to have the listCollections privilege action.
    */
  type AutoEncryptionSettings = com.mongodb.AutoEncryptionSettings

  /** The client-side settings for data key creation and explicit encryption.
    *
    * Explicit encryption/decryption is a community feature, enabled with the new `com.mongodb.client.vault.ClientEncryption` type,
    * for which this is the settings.
    */
  type ClientEncryptionSettings = com.mongodb.ClientEncryptionSettings

  /** Helper to get the class from a classTag
    *
    * @param ct the classTag we want to implicitly get the class of
    * @tparam C the class type
    * @return the classOf[C]
    */
  implicit def classTagToClassOf[C](ct: ClassTag[C]): Class[C] =
    ct.runtimeClass.asInstanceOf[Class[C]] // scalafix:ok

  implicit def bsonDocumentToDocument(doc: BsonDocument): Document = new Document(doc)

  implicit def documentToUntypedDocument(doc: Document): org.bson.Document =
    bsonDocumentToUntypedDocument(doc.underlying)

  private lazy val DOCUMENT_CODEC = new DocumentCodec()
  implicit def bsonDocumentToUntypedDocument(doc: BsonDocument): org.bson.Document =
    DOCUMENT_CODEC.decode(new BsonDocumentReader(doc), DecoderContext.builder().build())
}
