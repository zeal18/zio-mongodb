package io.github.zeal18.zio.mongodb.driver.session

import com.mongodb.reactivestreams.client.ClientSession as JavaClientSession
import io.github.zeal18.zio.mongodb.driver.TransactionOptions
import io.github.zeal18.zio.mongodb.driver.reactivestreams.PublisherOps
import zio.Task
import zio.ZIO

sealed trait ClientSession {
  private[driver] def underlying: JavaClientSession

  /** Returns true if there is an active transaction on this session, and false otherwise
    *
    * @return true if there is an active transaction on this session
    * @mongodb.server.release 4.0
    */
  def hasActiveTransaction(): Boolean

  /** Gets the transaction options.  Only call this method of the session has an active transaction
    *
    * @return the transaction options
    */
  def getTransactionOptions(): TransactionOptions

  /** Start a transaction in the context of this session with default transaction options. A transaction can not be started if there is
    * already an active transaction on this session.
    *
    * @mongodb.server.release 4.0
    */
  def startTransaction(): Task[Unit]

  /** Start a transaction in the context of this session with the given transaction options. A transaction can not be started if there is
    * already an active transaction on this session.
    *
    * @param transactionOptions the options to apply to the transaction
    *
    * @mongodb.server.release 4.0
    */
  def startTransaction(options: TransactionOptions): Task[Unit]

  /** Commit a transaction in the context of this session.  A transaction can only be commmited if one has first been started.
    *
    * @return an empty publisher that indicates when the operation has completed
    * @mongodb.server.release 4.0
    */
  def commitTransaction(): Task[Unit]

  /** Abort a transaction in the context of this session.  A transaction can only be aborted if one has first been started.
    *
    * @return an empty publisher that indicates when the operation has completed
    * @mongodb.server.release 4.0
    */
  def abortTransaction(): Task[Unit]

}

object ClientSession {
  private[driver] def apply(underlying: JavaClientSession): ClientSession =
    ClientSessionLive(underlying)

  final private[driver] case class ClientSessionLive(underlying: JavaClientSession)
      extends ClientSession {
    override def hasActiveTransaction(): Boolean = underlying.hasActiveTransaction()

    override def getTransactionOptions(): TransactionOptions = underlying.getTransactionOptions()

    override def startTransaction(): Task[Unit] = ZIO.attempt(underlying.startTransaction())

    override def startTransaction(options: TransactionOptions): Task[Unit] =
      ZIO.attempt(underlying.startTransaction(options))

    override def commitTransaction(): Task[Unit] = underlying.commitTransaction().unit

    override def abortTransaction(): Task[Unit] = underlying.abortTransaction().unit

  }
}
