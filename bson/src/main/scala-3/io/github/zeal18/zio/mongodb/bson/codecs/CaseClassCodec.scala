package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonWriter
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonInvalidOperationException
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError

/** "Normal" case class codec which just wraps the flatten version creating a document for it.
  */
case class CaseClassCodec[A](fullName: String, flat: FlatCaseClassCodec[A]) extends Codec[A]:
  override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit =
    try
      writer.writeStartDocument()
      flat.encode(writer, x, encoderCtx)
      writer.writeEndDocument()
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          fullName,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          fullName,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonError =>
        throw BsonError.CodecError(fullName, e) // scalafix:ok

  override def decode(reader: BsonReader, decoderCtx: DecoderContext): A =
    try
      reader.readStartDocument()
      val result = flat.decode(reader, decoderCtx)
      reader.readEndDocument()
      result
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          fullName,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          fullName,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonError =>
        throw BsonError.CodecError(fullName, e) // scalafix:ok
end CaseClassCodec
