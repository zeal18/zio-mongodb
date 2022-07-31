package io.github.zeal18.zio.mongodb.driver.indexes

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.mongodb.client.model.DropIndexOptions as JDropIndexOptions

/** The options to apply to the command when dropping indexes.
  *
  * @see [[https://www.mongodb.com/docs/manual/reference/command/dropIndexes/ Drop Indexes]]
  */
final case class DropIndexOptions(maxTime: Duration) {
  private[driver] def toJava: JDropIndexOptions =
    new JDropIndexOptions().maxTime(maxTime.toMillis, TimeUnit.MILLISECONDS)
}
