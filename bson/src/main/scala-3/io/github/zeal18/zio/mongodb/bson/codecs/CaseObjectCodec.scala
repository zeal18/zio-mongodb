package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import org.bson.BsonInvalidOperationException
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

/** A codec which reads/writes a string and always returns a specified value when string matches
  *
  * @param shortName string to be written/read and checked
  * @param fullName full name of the type for better error reporting
  * @param obj constant value to be returned on decoding
  */
case class CaseObjectCodec[A](shortName: String, fullName: String, obj: A) extends Codec[A]:
  override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
    try writer.writeString(shortName)
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e))
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e))

  override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
    try
      val name = reader.readString()
      if name == shortName then obj
      else throw BsonError.GeneralError(s"Expected '$shortName', got '$name'.")
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e))
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e))
      case e: BsonError =>
        throw BsonError.CodecError(fullName, e)
end CaseObjectCodec
