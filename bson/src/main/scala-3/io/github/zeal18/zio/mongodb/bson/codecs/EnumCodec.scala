package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonWriter
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonInvalidOperationException
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError

/** A codec for simple coproducts
  *
  * A simple coproduct is a sealed trait or an enum with all object members
  * or a Scala 3 parameterized enum.
  *
  * @param fullName full name of the type for better error reporting
  * @param nameByValue function to get a name by a coproduct case
  * @param valueByName function to get a coproduct case by its name
  */
case class EnumCodec[A](
  fullName: String,
  nameByValue: A => String,
  valueByName: String => Option[A],
) extends Codec[A]:
  override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
    try
      val shortName = nameByValue(value)
      writer.writeString(shortName)
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          fullName,
          BsonError.SerializationError(e),
        )
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          fullName,
          BsonError.SerializationError(e),
        )

  override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
    try
      val name = reader.readString()
      valueByName(name).fold[A](
        throw BsonError.CoproductError(
          name,
          BsonError.GeneralError("unsupported discriminator value"),
        ),
      )(identity[A])
    catch
      case e: BsonSerializationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e))
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(fullName, BsonError.SerializationError(e))
      case e: BsonError =>
        throw BsonError.CodecError(fullName, e)
end EnumCodec
