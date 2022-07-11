package io.github.zeal18.zio.mongodb.bson.codecs.encoders

import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import org.bson.BsonWriter
import org.bson.codecs.EncoderContext

trait OptionEncoders {
  implicit def nestedOption[A <: Option[?]: Encoder]: Encoder[Option[A]] =
    new NestedOptionEncoder[A]
  implicit def option[A: Encoder]: Encoder[Option[A]] = new OptionEncoder[A]
}

class OptionEncoder[A: Encoder] extends Encoder[Option[A]] {
  override def encode(writer: BsonWriter, value: Option[A], encoderContext: EncoderContext): Unit =
    value.fold(writer.writeNull())(element => Encoder[A].encode(writer, element, encoderContext))
}

class NestedOptionEncoder[A <: Option[?]: Encoder] extends Encoder[Option[A]] {
  override def encode(
    writer: BsonWriter,
    value: Option[A],
    encoderContext: EncoderContext,
  ): Unit =
    value.fold(writer.writeNull()) { element =>
      writer.writeStartDocument()
      writer.writeName("_option")
      Encoder[A].encode(writer, element, encoderContext)
      writer.writeEndDocument()
    }
}
