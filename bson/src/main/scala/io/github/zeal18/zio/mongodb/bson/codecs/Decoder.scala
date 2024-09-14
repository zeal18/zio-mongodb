package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonReader
import org.bson.codecs.Decoder as JDecoder
import org.bson.codecs.DecoderContext

trait Decoder[A] { self =>

  def decode(reader: BsonReader, decoderContext: DecoderContext): A

  /** Returns a new [[Decoder]] by applying a function that maps an A
    * to a B, after decoding as an A using this decoder.
    */
  def map[B](f: A => B): Decoder[B] = new Decoder[B] {
    override def decode(reader: BsonReader, decoderContext: DecoderContext): B =
      f(self.decode(reader, decoderContext))
  }
}

object Decoder {
  def apply[A](implicit d: Decoder[A]): Decoder[A] = d
  def apply[A](d: JDecoder[A]): Decoder[A] = new Decoder[A] {
    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      d.decode(reader, decoderContext)
  }

  implicit def fromCodec[A](implicit codec: Codec[A]): Decoder[A] = codec
}
