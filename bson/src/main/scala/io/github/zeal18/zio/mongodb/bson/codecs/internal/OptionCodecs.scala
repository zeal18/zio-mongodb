package io.github.zeal18.zio.mongodb.bson.codecs.internal

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.Decoder
import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

private[codecs] trait OptionCodecs {
  implicit def nestedOption[A <: Option[?]: Codec]: Codec[Option[A]] =
    new NestedOptionCodec[A]
  implicit def option[A: Codec]: Codec[Option[A]] = new OptionCodec[A]
}

private class OptionCodec[A: Codec] extends Codec[Option[A]] {
  override def encode(writer: BsonWriter, value: Option[A], encoderContext: EncoderContext): Unit =
    value.fold(writer.writeNull())(element => Codec[A].encode(writer, element, encoderContext))

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Option[A] =
    reader.getCurrentBsonType() match {
      case BsonType.NULL =>
        reader.skipValue()
        None
      case _ => Some(Decoder[A].decode(reader, decoderContext))
    }
}

private class NestedOptionCodec[A <: Option[?]: Codec] extends Codec[Option[A]] {
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

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Option[A] =
    reader.getCurrentBsonType() match {
      case BsonType.NULL =>
        reader.skipValue()
        None
      case BsonType.DOCUMENT =>
        reader.readStartDocument()
        val name = reader.readName()
        if (name != "_option") {
          throw new BsonSerializationException(
            s"Expected DOCUMENT to contain a field named '_option', but got '$name'",
          ) // scalafix:ok
        }
        val value = Decoder[A].decode(reader, decoderContext)
        reader.readEndDocument()
        Some(value)
      case t =>
        throw new BsonSerializationException(
          s"Expected a document or null but found $t",
        ) // scalafix:ok
    }
}
