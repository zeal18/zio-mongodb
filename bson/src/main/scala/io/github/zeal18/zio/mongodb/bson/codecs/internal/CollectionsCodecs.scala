package io.github.zeal18.zio.mongodb.bson.codecs.internal

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import org.bson.BsonInvalidOperationException
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

import scala.collection.IterableFactory
import scala.reflect.ClassTag

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

      override def decode(reader: BsonReader, ctx: DecoderContext): I[T] =
        try {
          val firstType =
            if (reader.getCurrentBsonType() == BsonType.ARRAY) {
              reader.readStartArray()
              reader.readBsonType()
            } else {
              reader.getCurrentBsonType()
            }
          factory.unfold[T, (BsonType, Int)]((firstType, 0)) {
            case (BsonType.END_OF_DOCUMENT, _) =>
              reader.readEndArray()
              None
            case (_, index) =>
              val element =
                try
                  elementCodec.decode(reader, ctx)
                catch {
                  case e: BsonError => throw BsonError.ArrayError(index, e)
                }
              val nextType = reader.readBsonType()
              Some((element, (nextType, index + 1)))
          }
        } catch {
          case e: BsonSerializationException =>
            throw BsonError.CodecError(
              implicitly[ClassTag[I[T]]].toString(),
              BsonError.SerializationError(e),
            )
          case e: BsonInvalidOperationException =>
            throw BsonError.CodecError(
              implicitly[ClassTag[I[T]]].toString(),
              BsonError.SerializationError(e),
            )
          case e: BsonError =>
            throw BsonError.CodecError(implicitly[ClassTag[I[T]]].toString(), e)
        }

    }
}
