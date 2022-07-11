package io.github.zeal18.zio.mongodb.bson.codecs.decoders

import io.github.zeal18.zio.mongodb.bson.codecs.Decoder
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.codecs.DecoderContext

trait OptionDecoders {
  implicit def nestedOption[A <: Option[?]: Decoder]: Decoder[Option[A]] =
    new NestedOptionDecoder[A]
  implicit def option[A: Decoder]: Decoder[Option[A]] = new OptionDecoder[A]
}

class OptionDecoder[A: Decoder] extends Decoder[Option[A]] {
  override def decode(reader: BsonReader, decoderContext: DecoderContext): Option[A] =
    reader.getCurrentBsonType() match {
      case BsonType.NULL =>
        reader.skipValue()
        None
      case _ => Some(Decoder[A].decode(reader, decoderContext))
    }
}

class NestedOptionDecoder[A <: Option[?]: Decoder] extends Decoder[Option[A]] {
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
