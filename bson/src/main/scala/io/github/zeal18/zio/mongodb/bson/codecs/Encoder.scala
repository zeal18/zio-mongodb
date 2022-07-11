package io.github.zeal18.zio.mongodb.bson.codecs

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.encoders.BsonEncoders
import io.github.zeal18.zio.mongodb.bson.codecs.encoders.CollectionsEncoders
import io.github.zeal18.zio.mongodb.bson.codecs.encoders.OptionEncoders
import io.github.zeal18.zio.mongodb.bson.codecs.encoders.PrimitiveEncoders
import io.github.zeal18.zio.mongodb.bson.codecs.encoders.TemporalEncoders
import org.bson.BsonWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.Encoder as JEncoder

abstract class Encoder[A: ClassTag] extends JEncoder[A] { self =>

  /** Encode an instance of the type parameter {@code A} into a BSON value.
    * @param writer the BSON writer to encode into
    * @param value the value to encode
    * @param encoderContext the encoder context
    */
  def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit

  override def getEncoderClass(): Class[A] =
    implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]] // scalafix:ok

  /** Returns a new [[Encoder]] by applying a function that maps a B
    * to an A, before encoding as an A using this encoder.
    */
  def contramap[B: ClassTag](f: B => A): Encoder[B] = new Encoder[B] {
    override def encode(writer: BsonWriter, value: B, encoderContext: EncoderContext): Unit =
      self.encode(writer, f(value), encoderContext)
  }
}

object Encoder
    extends PrimitiveEncoders
    with BsonEncoders
    with TemporalEncoders
    with CollectionsEncoders
    with OptionEncoders {
  def apply[A](implicit e: Encoder[A]): Encoder[A] = e
  def apply[A: ClassTag](e: JEncoder[A]): Encoder[A] = new Encoder[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      e.encode(writer, value, encoderContext)
  }
}
