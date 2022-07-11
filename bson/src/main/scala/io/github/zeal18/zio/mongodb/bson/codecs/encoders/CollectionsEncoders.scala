package io.github.zeal18.zio.mongodb.bson.codecs.encoders

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import org.bson.BsonWriter
import org.bson.codecs.EncoderContext

trait CollectionsEncoders {
  implicit def listEncoder[A: Encoder]: Encoder[List[A]]     = iterableEncoder
  implicit def seqEncoder[A: Encoder]: Encoder[Seq[A]]       = iterableEncoder
  implicit def vectorEncoder[A: Encoder]: Encoder[Vector[A]] = iterableEncoder
  implicit def setEncoder[A: Encoder]: Encoder[Set[A]]       = iterableEncoder

  private def iterableEncoder[I[T] <: Iterable[T], T](implicit
    elementEncoder: Encoder[T],
    classTag: ClassTag[I[T]],
  ): Encoder[I[T]] =
    new Encoder[I[T]] {
      override def encode(writer: BsonWriter, x: I[T], ctx: EncoderContext): Unit = {
        writer.writeStartArray()
        x.foreach(elementEncoder.encode(writer, _, ctx))
        writer.writeEndArray()
      }
    }
}
