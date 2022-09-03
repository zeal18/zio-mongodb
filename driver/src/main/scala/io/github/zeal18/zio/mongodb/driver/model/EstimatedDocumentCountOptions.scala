package io.github.zeal18.zio.mongodb.driver.model

import java.time.Duration

import com.mongodb.client.model.EstimatedDocumentCountOptions as JEstimatedDocumentCountOptions

/** The options to apply to an estimated count operation.
  */
final case class EstimatedDocumentCountOptions(maxTime: Duration) {
  private[driver] def toJava: JEstimatedDocumentCountOptions =
    new JEstimatedDocumentCountOptions()
      .maxTime(maxTime.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)

}
