package io.github.zeal18.zio.mongodb.bson

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.TemporalAmount

import scala.collection.IterableFactory
import scala.reflect.ClassTag

import io.github.cbartosiak.bson.codecs.jsr310.offsetdatetime.OffsetDateTimeAsStringCodec
import org.bson.BsonDocument
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.BooleanCodec
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.BsonValueCodec
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.DoubleCodec
import org.bson.codecs.EncoderContext
import org.bson.codecs.IntegerCodec
import org.bson.codecs.LongCodec
import org.bson.codecs.ObjectIdCodec
import org.bson.codecs.StringCodec
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.types.ObjectId

package object codecs {

  /** Type alias to the `BsonTypeClassMap`
    */
  type BsonTypeClassMap = org.bson.codecs.BsonTypeClassMap

  /** Companion to return the default `BsonTypeClassMap`
    */
  object BsonTypeClassMap {
    def apply(): BsonTypeClassMap = new BsonTypeClassMap()
  }

  /** Type alias to the `BsonTypeCodecMap`
    */
  type BsonTypeCodecMap = org.bson.codecs.BsonTypeCodecMap

  implicit class CodecOps[A](private val codec: Codec[A]) extends AnyVal {
    def bimap[B](map: A => B, contramap: B => A)(implicit c: ClassTag[B]): Codec[B] = new Codec[B] {
      override def encode(writer: BsonWriter, value: B, context: EncoderContext): Unit =
        codec.encode(writer, contramap(value), context)

      override def decode(reader: BsonReader, context: DecoderContext): B =
        map(codec.decode(reader, context))

      override def getEncoderClass(): Class[B] =
        c.runtimeClass.asInstanceOf[Class[B]] // scalafix:ok
    }
  }

  implicit lazy val booleanCodec: Codec[Boolean] =
    (new BooleanCodec()).bimap(Boolean2boolean, boolean2Boolean)
  implicit lazy val stringCodec: Codec[String] = new StringCodec()

  implicit lazy val intCodec: Codec[Int] =
    (new IntegerCodec()).bimap(Integer2int, int2Integer)
  implicit lazy val longCodec: Codec[Long] =
    (new LongCodec()).bimap(Long2long, long2Long)
  implicit lazy val doubleCodec: Codec[Double] =
    (new DoubleCodec()).bimap(Double2double, double2Double)

  implicit lazy val bsonValueCodec: Codec[BsonValue]       = new BsonValueCodec()
  implicit lazy val bsonDocumentCodec: Codec[BsonDocument] = new BsonDocumentCodec()

  implicit lazy val objectIdCodec: Codec[ObjectId] = new ObjectIdCodec()

  implicit lazy val instantCodec: Codec[Instant]               = new InstantCodec()
  implicit lazy val offsetDateTimeCodec: Codec[OffsetDateTime] = new OffsetDateTimeAsStringCodec()
  implicit lazy val localDateCodec: Codec[LocalDate]           = new LocalDateCodec()
  implicit lazy val temporalAmountCodec: Codec[TemporalAmount] = new TemporalAmountCodec()

  implicit def listCodec[A](implicit elementCodec: Codec[A]): Codec[List[A]] =
    iterableCodec(List)
  implicit def vectorCodec[A](implicit elementCodec: Codec[A]): Codec[Vector[A]] =
    iterableCodec(Vector)
  implicit def sequenceCodec[A](implicit elementCodec: Codec[A]): Codec[Seq[A]] =
    iterableCodec(Seq)
  implicit def setCodec[A](implicit elementCodec: Codec[A]): Codec[Set[A]] =
    iterableCodec(Set)

  private def iterableCodec[I[T] <: Iterable[T], T](
    factory: IterableFactory[I],
  )(implicit elementCodec: Codec[T], classTag: ClassTag[I[T]]): Codec[I[T]] =
    new Codec[I[T]] {
      override def encode(writer: BsonWriter, x: I[T], ctx: EncoderContext): Unit = {
        writer.writeStartArray()
        x.foreach(elementCodec.encode(writer, _, ctx))
        writer.writeEndArray()
      }

      override def getEncoderClass(): Class[I[T]] =
        classTag.runtimeClass.asInstanceOf[Class[I[T]]] // scalafix:ok

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
