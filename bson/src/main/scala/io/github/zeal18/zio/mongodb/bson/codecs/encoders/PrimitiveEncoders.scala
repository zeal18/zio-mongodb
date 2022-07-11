package io.github.zeal18.zio.mongodb.bson.codecs.encoders

import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import org.bson.codecs.BooleanCodec
import org.bson.codecs.ByteCodec
import org.bson.codecs.DoubleCodec
import org.bson.codecs.FloatCodec
import org.bson.codecs.IntegerCodec
import org.bson.codecs.LongCodec
import org.bson.codecs.ShortCodec
import org.bson.codecs.StringCodec

trait PrimitiveEncoders {
  implicit lazy val boolean: Encoder[Boolean] =
    Encoder(new BooleanCodec()).contramap(boolean2Boolean)
  implicit lazy val int: Encoder[Int] =
    Encoder(new IntegerCodec()).contramap(int2Integer)
  implicit lazy val long: Encoder[Long] =
    Encoder(new LongCodec()).contramap(long2Long)
  implicit lazy val short: Encoder[Short] =
    Encoder(new ShortCodec()).contramap(short2Short)
  implicit lazy val double: Encoder[Double] =
    Encoder(new DoubleCodec()).contramap(double2Double)
  implicit lazy val float: Encoder[Float] =
    Encoder(new FloatCodec()).contramap(float2Float)
  implicit lazy val string: Encoder[String] =
    Encoder(new StringCodec())
  implicit lazy val byte: Encoder[Byte] =
    Encoder(new ByteCodec()).contramap(byte2Byte)
}
