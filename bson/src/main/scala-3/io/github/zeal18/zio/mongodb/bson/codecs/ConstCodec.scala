package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

/** Encodes nothing, but always decodes a specified constant value
  *
  * Could be used for complex coproducts when semantically having discriminator is enough,
  * but a codec is required. For example to provide as a part of MixedCoproductCodec(codecByName = ) function
  */
class ConstCodec[A](value: A) extends Codec[A]:
  override def decode(reader: BsonReader, decoderContext: DecoderContext): A              = value
  override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit = ()
