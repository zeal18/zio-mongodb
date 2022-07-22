package io.github.zeal18.zio.mongodb.bson.codecs

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.internal.BsonCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.CollectionsCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.OptionCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.PrimitiveCodecs
import io.github.zeal18.zio.mongodb.bson.codecs.internal.TemporalCodecs
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.Codec as JCodec

abstract class Codec[A: ClassTag] extends Encoder[A] with Decoder[A] with JCodec[A] { self =>

  /** Returns a new [[Codec]] that encodes and decodes values of type `B` using this codec.
    */
  def bimap[B: ClassTag](mapF: A => B, contramapF: B => A): Codec[B] = new Codec[B] {
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
    with OptionCodecs {
  def apply[A](implicit codec: Codec[A]): Codec[A] = codec

  def apply[A: ClassTag](encoder: Encoder[A], decoder: Decoder[A]): Codec[A] = new Codec[A] {
    override def encode(writer: BsonWriter, value: A, context: EncoderContext): Unit =
      encoder.encode(writer, value, context)

    override def decode(reader: BsonReader, context: DecoderContext): A =
      decoder.decode(reader, context)
  }

  def apply[A: ClassTag](c: JCodec[A]): Codec[A] = new Codec[A] {
    override def encode(writer: BsonWriter, value: A, context: EncoderContext): Unit =
      c.encode(writer, value, context)

    override def decode(reader: BsonReader, context: DecoderContext): A =
      c.decode(reader, context)
  }
}
