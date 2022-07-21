package io.github.zeal18.zio.mongodb.bson.codecs

import scala.reflect.ClassTag

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

object Codec {
  implicit def apply[A: ClassTag](implicit e: Encoder[A], d: Decoder[A]): Codec[A] = new Codec[A] {
    override def encode(writer: BsonWriter, value: A, context: EncoderContext): Unit =
      e.encode(writer, value, context)

    override def decode(reader: BsonReader, context: DecoderContext): A =
      d.decode(reader, context)
  }

  def apply[A: ClassTag](c: JCodec[A]): Codec[A] = new Codec[A] {
    override def encode(writer: BsonWriter, value: A, context: EncoderContext): Unit =
      c.encode(writer, value, context)

    override def decode(reader: BsonReader, context: DecoderContext): A =
      c.decode(reader, context)
  }
}
