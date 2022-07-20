package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext

final case class Variable[A](name: String, value: A)(implicit codec: Codec[A]) {
  def encode(writer: BsonDocumentWriter): Unit =
    codec.encode(writer, value, EncoderContext.builder().build())
}
