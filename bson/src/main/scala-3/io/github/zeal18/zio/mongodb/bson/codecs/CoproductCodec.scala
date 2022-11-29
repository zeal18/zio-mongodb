package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonWriter
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonInvalidOperationException
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError

/** A codec for complex coproducts
  *
  * @param fullName full name of the type for better error reporting
  * @param discriminator discriminator field name
  * @param codecByValue function to get a pair of a name and a codec by a coproduct case
  * @param codecByName function to get a coproduct case codec by its name
  */
case class CoproductCodec[A](
  fullName: String,
  discriminator: String,
  codecByValue: A => (String, Codec[?]),
  codecByName: String => Option[Codec[A]],
) extends Codec[A]:
  override def encode(writer: BsonWriter, value: A, encoderCtx: EncoderContext): Unit =
    try
      val (name, codec) = codecByValue(value)
      try
        writer.writeStartDocument()
        writer.writeString(discriminator, name)
        codec.asInstanceOf[Codec[A]].encode(writer, value, encoderCtx) // scalafix:ok
        writer.writeEndDocument()
      catch
        case e: BsonSerializationException =>
          throw BsonError.CoproductError(name, BsonError.SerializationError(e)) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CoproductError(name, BsonError.SerializationError(e)) // scalafix:ok
        case e: BsonError =>
          throw BsonError.CoproductError(name, e) // scalafix:ok
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e)) // scalafix:ok
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e)) // scalafix:ok
      case e: BsonError =>
        throw BsonError.CodecError(fullName, e) // scalafix:ok

  override def decode(reader: BsonReader, decoderCtx: DecoderContext): A =
    try
      reader.readStartDocument()
      val typeTag = reader.readString(discriminator)
      val codec   = codecByName(typeTag)
      val result = codec
        .map { codec =>
          try codec.decode(reader, decoderCtx)
          catch
            case e: BsonError =>
              throw BsonError.CoproductError(typeTag, e) // scalafix:ok
        }
        .getOrElse(
          throw BsonError.CoproductError(
            typeTag,
            BsonError.GeneralError("unsupported discriminator value"),
          ), // scalafix:ok
        )
        .asInstanceOf[A] // scalafix:ok
      reader.readEndDocument()

      result
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e)) // scalafix:ok
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e)) // scalafix:ok
      case e: BsonError =>
        throw BsonError.CodecError(fullName, e) // scalafix:ok
end CoproductCodec
