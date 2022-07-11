package io.github.zeal18.zio.mongodb.bson.codecs.decoders

import scala.collection.IterableFactory

import io.github.zeal18.zio.mongodb.bson.codecs.Decoder
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.codecs.DecoderContext

trait CollectionsDecoders {
  implicit def listDecoder[A: Decoder]: Decoder[List[A]]     = iterableDecoder(List)
  implicit def seqDecoder[A: Decoder]: Decoder[Seq[A]]       = iterableDecoder(Seq)
  implicit def vectorDecoder[A: Decoder]: Decoder[Vector[A]] = iterableDecoder(Vector)
  implicit def setDecoder[A: Decoder]: Decoder[Set[A]]       = iterableDecoder(Set)

  private def iterableDecoder[I[T] <: Iterable[T], T](factory: IterableFactory[I])(implicit
    elementDecoder: Decoder[T],
  ): Decoder[I[T]] =
    new Decoder[I[T]] {
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
            val element  = elementDecoder.decode(reader, ctx)
            val nextType = reader.readBsonType()
            Some((element, nextType))
        }
      }
    }
}
