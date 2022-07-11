package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.decoders.BsonDecoders
import io.github.zeal18.zio.mongodb.bson.codecs.decoders.CollectionsDecoders
import io.github.zeal18.zio.mongodb.bson.codecs.decoders.OptionDecoders
import io.github.zeal18.zio.mongodb.bson.codecs.decoders.PrimitiveDecoders
import io.github.zeal18.zio.mongodb.bson.codecs.decoders.TemporalDecoders
import org.bson.BsonReader
import org.bson.codecs.DecoderContext
import org.bson.codecs.Decoder as JDecoder

trait Decoder[A] extends JDecoder[A] { self =>

  /** Returns a new [[Decoder]] by applying a function that maps an A
    * to a B, after decoding as an A using this decoder.
    */
  def map[B](f: A => B): Decoder[B] = new Decoder[B] {
    override def decode(reader: BsonReader, decoderContext: DecoderContext): B =
      f(self.decode(reader, decoderContext))
  }
}

object Decoder
    extends PrimitiveDecoders
    with BsonDecoders
    with TemporalDecoders
    with CollectionsDecoders
    with OptionDecoders {
  def apply[A](implicit d: Decoder[A]): Decoder[A] = d
  def apply[A](d: JDecoder[A]): Decoder[A] = new Decoder[A] {
    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      d.decode(reader, decoderContext)
  }
}
