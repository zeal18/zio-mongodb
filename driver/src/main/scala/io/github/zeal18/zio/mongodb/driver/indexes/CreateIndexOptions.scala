package io.github.zeal18.zio.mongodb.driver.indexes

import com.mongodb.client.model.CreateIndexOptions as JCreateIndexOptions

import java.time.Duration
import java.util.concurrent.TimeUnit

/** The options to apply to the command when creating indexes.
  *
  * @see [[https://www.mongodb.com/docs/manual/reference/command/createIndexes/#command-fields Create Index]]
  */
final case class CreateIndexOptions(
  maxTime: Duration,
  quorum: Option[CreateIndexCommitQuorum] = None,
) {
  private[driver] def toJava: JCreateIndexOptions = {
    val options = new JCreateIndexOptions().maxTime(maxTime.toMillis, TimeUnit.MILLISECONDS)
    quorum.foreach(q => options.commitQuorum(q.toJava))

    options
  }
}
