package io.github.zeal18.zio.mongodb.bson.codecs.internal

import scala.collection.IterableFactory
import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

private[codecs] trait CollectionsCodecs {
  implicit def list[A: Codec]: Codec[List[A]]     = iterable(List)
  implicit def seq[A: Codec]: Codec[Seq[A]]       = iterable(Seq)
  implicit def vector[A: Codec]: Codec[Vector[A]] = iterable(Vector)
  implicit def set[A: Codec]: Codec[Set[A]]       = iterable(Set)

  private def iterable[I[T] <: Iterable[T], T](factory: IterableFactory[I])(implicit
    elementCodec: Codec[T],
    classTag: ClassTag[I[T]],
  ): Codec[I[T]] =
    new Codec[I[T]] {
      override def encode(writer: BsonWriter, x: I[T], ctx: EncoderContext): Unit = {
        writer.writeStartArray()
        x.foreach(elementCodec.encode(writer, _, ctx))
        writer.writeEndArray()
      }

      override def decode(reader: BsonReader, ctx: DecoderContext): I[T] = {
        val firstType =
          if (reader.getCurrentBsonType() == BsonType.ARRAY) {
            reader.readStartArray()
            reader.readBsonType()
          } else {
            reader.getCurrentBsonType()
          }
        factory.unfold[T, BsonType](firstType) {
          case BsonType.END_OF_DOCUMENT =>
            reader.readEndArray()
            None
          case _ =>
            val element  = elementCodec.decode(reader, ctx)
            val nextType = reader.readBsonType()
            Some((element, nextType))
        }
      }

    }
}
