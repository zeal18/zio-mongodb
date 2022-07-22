package io.github.zeal18.zio.mongodb.bson.codecs.internal

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import org.bson.codecs.BooleanCodec
import org.bson.codecs.ByteCodec
import org.bson.codecs.DoubleCodec
import org.bson.codecs.FloatCodec
import org.bson.codecs.IntegerCodec
import org.bson.codecs.LongCodec
import org.bson.codecs.ShortCodec
import org.bson.codecs.StringCodec

private[codecs] trait PrimitiveCodecs {
  implicit lazy val boolean: Codec[Boolean] =
    Codec(new BooleanCodec()).bimap(Boolean2boolean, boolean2Boolean)
  implicit lazy val int: Codec[Int] =
    Codec(new IntegerCodec()).bimap(Integer2int, int2Integer)
  implicit lazy val long: Codec[Long] =
    Codec(new LongCodec()).bimap(Long2long, long2Long)
  implicit lazy val short: Codec[Short] =
    Codec(new ShortCodec()).bimap(Short2short, short2Short)
  implicit lazy val double: Codec[Double] =
    Codec(new DoubleCodec()).bimap(Double2double, double2Double)
  implicit lazy val float: Codec[Float] =
    Codec(new FloatCodec()).bimap(Float2float, float2Float)
  implicit lazy val string: Codec[String] =
    Codec(new StringCodec())
  implicit lazy val byte: Codec[Byte] =
    Codec(new ByteCodec()).bimap(Byte2byte, byte2Byte)
}
