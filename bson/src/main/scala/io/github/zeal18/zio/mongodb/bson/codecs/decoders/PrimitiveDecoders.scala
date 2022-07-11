package io.github.zeal18.zio.mongodb.bson.codecs.decoders

import io.github.zeal18.zio.mongodb.bson.codecs.Decoder
import org.bson.codecs.BooleanCodec
import org.bson.codecs.ByteCodec
import org.bson.codecs.DoubleCodec
import org.bson.codecs.FloatCodec
import org.bson.codecs.IntegerCodec
import org.bson.codecs.LongCodec
import org.bson.codecs.ShortCodec
import org.bson.codecs.StringCodec

trait PrimitiveDecoders {
  implicit lazy val boolean: Decoder[Boolean] =
    Decoder(new BooleanCodec()).map(Boolean2boolean)
  implicit lazy val int: Decoder[Int] =
    Decoder(new IntegerCodec()).map(Integer2int)
  implicit lazy val long: Decoder[Long] =
    Decoder(new LongCodec()).map(Long2long)
  implicit lazy val short: Decoder[Short] =
    Decoder(new ShortCodec()).map(Short2short)
  implicit lazy val double: Decoder[Double] =
    Decoder(new DoubleCodec()).map(Double2double)
  implicit lazy val float: Decoder[Float] =
    Decoder(new FloatCodec()).map(Float2float)
  implicit lazy val string: Decoder[String] =
    Decoder(new StringCodec())
  implicit lazy val byte: Decoder[Byte] =
    Decoder(new ByteCodec()).map(Byte2byte)
}
