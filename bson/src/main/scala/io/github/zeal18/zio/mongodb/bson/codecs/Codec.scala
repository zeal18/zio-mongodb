package io.github.zeal18.zio.mongodb.bson.codecs

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import io.github.zeal18.zio.mongodb.bson.codecs.internal.BsonCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.CollectionsCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.OptionCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.PrimitiveCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.TemporalCodecs
import org.bson.BsonInvalidOperationException
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.Codec as JCodec

abstract class Codec[A] extends Encoder[A] with Decoder[A] { self =>

  /** Returns a new [[Codec]] that encodes and decodes values of type `B` using this codec.
    */
  def bimap[B](mapF: A => B, contramapF: B => A): Codec[B] = new Codec[B] {
    override def encode(writer: BsonWriter, value: B, context: EncoderContext): Unit =
      self.encode(writer, contramapF(value), context)

    override def decode(reader: BsonReader, context: DecoderContext): B =
      mapF(self.decode(reader, context))
  }
}

object Codec
    extends PrimitiveCodecs
    with BsonCodecs
    with TemporalCodecs
    with CollectionsCodecs
    with OptionCodecs
    with DerivedCodec {
  def apply[A](implicit codec: Codec[A]): Codec[A] = codec

  def apply[A](encoder: Encoder[A], decoder: Decoder[A])(implicit ct: ClassTag[A]): Codec[A] =
    new Codec[A] {
      override def encode(writer: BsonWriter, value: A, context: EncoderContext): Unit =
        try encoder.encode(writer, value, context)
        catch {
          case e: BsonSerializationException =>
            throw BsonError.CodecError(
              ct.toString(),
              BsonError.SerializationError(e),
            ) // scalafix:ok
          case e: BsonInvalidOperationException =>
            throw BsonError.CodecError(
              ct.toString(),
              BsonError.SerializationError(e),
            ) // scalafix:ok
          case e: BsonError =>
            throw BsonError.CodecError(ct.toString(), e) // scalafix:ok
        }

      override def decode(reader: BsonReader, context: DecoderContext): A =
        try decoder.decode(reader, context)
        catch {
          case e: BsonSerializationException =>
            throw BsonError.CodecError(
              ct.toString(),
              BsonError.SerializationError(e),
            ) // scalafix:ok
          case e: BsonInvalidOperationException =>
            throw BsonError.CodecError(
              ct.toString(),
              BsonError.SerializationError(e),
            ) // scalafix:ok
          case e: BsonError =>
            throw BsonError.CodecError(ct.toString(), e) // scalafix:ok
        }
    }

  def apply[A](c: JCodec[A])(implicit ct: ClassTag[A]): Codec[A] = new Codec[A] {
    override def encode(writer: BsonWriter, value: A, context: EncoderContext): Unit =
      try c.encode(writer, value, context)
      catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(ct.toString(), BsonError.SerializationError(e)) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(ct.toString(), BsonError.SerializationError(e)) // scalafix:ok
      }

    override def decode(reader: BsonReader, context: DecoderContext): A =
      try c.decode(reader, context)
      catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(ct.toString(), BsonError.SerializationError(e)) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(ct.toString(), BsonError.SerializationError(e)) // scalafix:ok
      }
  }
}
