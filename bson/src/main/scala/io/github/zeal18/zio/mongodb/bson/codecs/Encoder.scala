package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonWriter
import org.bson.codecs.Encoder as JEncoder
import org.bson.codecs.EncoderContext

abstract class Encoder[A] { self =>

  /** Encode an instance of the type parameter {@code A} into a BSON value.
    * @param writer the BSON writer to encode into
    * @param value the value to encode
    */
  def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit

  /** Returns a new [[Encoder]] by applying a function that maps a B
    * to an A, before encoding as an A using this encoder.
    */
  def contramap[B](f: B => A): Encoder[B] = new Encoder[B] {
    override def encode(writer: BsonWriter, value: B, encoderContext: EncoderContext): Unit =
      self.encode(writer, f(value), encoderContext)
  }
}

object Encoder {
  def apply[A](implicit e: Encoder[A]): Encoder[A] = e
  def apply[A](e: JEncoder[A]): Encoder[A]         = new Encoder[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      e.encode(writer, value, encoderContext)
  }

  implicit def fromCodec[A](implicit codec: Codec[A]): Encoder[A] = codec
}
