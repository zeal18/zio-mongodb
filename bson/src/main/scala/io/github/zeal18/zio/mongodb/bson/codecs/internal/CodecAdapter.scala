package io.github.zeal18.zio.mongodb.bson.codecs.internal

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.Codec as JCodec

private[mongodb] case class CodecAdapter[A](adapted: Codec[A])(implicit ct: ClassTag[A])
    extends JCodec[A] {
  override def encode(
    writer: BsonWriter,
    value: A,
    encoderContext: EncoderContext,
  ): Unit = adapted.encode(writer, value, encoderContext)

  override def getEncoderClass(): Class[A] =
    implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]] // scalafix:ok

  override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
    adapted.decode(reader, decoderContext)

}
