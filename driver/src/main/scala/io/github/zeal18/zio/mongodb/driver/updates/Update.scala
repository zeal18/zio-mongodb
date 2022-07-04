package io.github.zeal18.zio.mongodb.driver.updates

import scala.jdk.CollectionConverters.*

import com.mongodb.client.model.Updates as JUpdates
import io.github.zeal18.zio.mongodb.bson.codecs.booleanCodec
import io.github.zeal18.zio.mongodb.bson.codecs.bsonDocumentCodec
import io.github.zeal18.zio.mongodb.bson.codecs.intCodec
import io.github.zeal18.zio.mongodb.bson.codecs.stringCodec
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.codecs.Codec
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

sealed trait Update { self =>
  def toBson: Bson = new Bson {
    override def toBsonDocument[TDocument <: Object](
      documentClass: Class[TDocument],
      codecRegistry: CodecRegistry,
    ): BsonDocument = {
      def simpleUpdate[A](
        operator: String,
        fieldName: String,
        value: A,
        codec: Codec[A],
      ): BsonDocument = {
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName(operator)

        writer.writeStartDocument()
        writer.writeName(fieldName)
        codec.encode(writer, value, EncoderContext.builder().build())
        writer.writeEndDocument()

        writer.writeEndDocument()

        writer.getDocument()
      }

      def withEachUpdate[A](
        operator: String,
        fieldName: String,
        values: Seq[A],
        codec: Codec[A],
      ): BsonDocument = {
        val writer  = new BsonDocumentWriter(new BsonDocument())
        val context = EncoderContext.builder().build()

        writer.writeStartDocument()
        writer.writeName(operator)

        writer.writeStartDocument()
        writer.writeName(fieldName)
        writer.writeStartDocument()

        writer.writeStartArray("$each")
        values.foreach { value =>
          codec.encode(writer, value, context)
        }
        writer.writeEndArray()

        writer.writeEndDocument()
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      }

      def pullAllUpdate[A](fieldName: String, values: Seq[A], codec: Codec[A]): BsonDocument = {
        val writer  = new BsonDocumentWriter(new BsonDocument())
        val context = EncoderContext.builder().build()

        writer.writeStartDocument()
        writer.writeName("$pullAll")

        writer.writeStartDocument()
        writer.writeName(fieldName)

        writer.writeStartArray()
        values.foreach(value => codec.encode(writer, value, context))
        writer.writeEndArray()

        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      }

      def createBitUpdateDocumentInt(
        fieldName: String,
        bitwiseOperator: String,
        value: Int,
      ): BsonDocument =
        createBitUpdateDocument(fieldName, bitwiseOperator, new BsonInt32(value))

      def createBitUpdateDocumentLong(
        fieldName: String,
        bitwiseOperator: String,
        value: Long,
      ): BsonDocument =
        createBitUpdateDocument(fieldName, bitwiseOperator, new BsonInt64(value))

      def createBitUpdateDocument(fieldName: String, bitwiseOperator: String, value: BsonValue) =
        new BsonDocument(
          "$bit",
          new BsonDocument(fieldName, new BsonDocument(bitwiseOperator, value)),
        )

      self match {
        case Update.Combine(updates) =>
          JUpdates
            .combine(updates.map(_.toBson).asJava)
            .toBsonDocument(documentClass, codecRegistry)
        case Update.Set(fieldName, value, codec) =>
          simpleUpdate("$set", fieldName, value, codec)
        case Update.Unset(fieldName) =>
          simpleUpdate("$unset", fieldName, "", stringCodec)
        case Update.SetOnInsert(fieldName, value, codec) =>
          simpleUpdate("$setOnInsert", fieldName, value, codec)
        case Update.Rename(fieldName, newFieldName) =>
          simpleUpdate("$rename", fieldName, newFieldName, stringCodec)
        case Update.Increment(fieldName, number, codec) =>
          simpleUpdate("$inc", fieldName, number, codec)
        case Update.Multiply(fieldName, number, codec) =>
          simpleUpdate("$mul", fieldName, number, codec)
        case Update.Min(fieldName, value, codec) =>
          simpleUpdate("$min", fieldName, value, codec)
        case Update.Max(fieldName, value, codec) =>
          simpleUpdate("$max", fieldName, value, codec)
        case Update.CurrentDate(fieldName) =>
          simpleUpdate("$currentDate", fieldName, true, booleanCodec)
        case Update.CurrentTimestamp(fieldName) =>
          new BsonDocument()
          simpleUpdate(
            "$currentDate",
            fieldName,
            new BsonDocument("$type", new BsonString("timestamp")),
            bsonDocumentCodec,
          )
        case Update.AddToSet(fieldName, value, codec) =>
          simpleUpdate("$addToSet", fieldName, value, codec)
        case Update.AddEachToSet(fieldName, values, codec) =>
          withEachUpdate("$addToSet", fieldName, values, codec)
        case Update.Push(fieldName, value, codec) =>
          simpleUpdate("$push", fieldName, value, codec)
        case Update.PushEach(fieldName, values, codec) =>
          withEachUpdate("$push", fieldName, values, codec)
        case Update.Pull(fieldName, value, codec) =>
          simpleUpdate("$pull", fieldName, value, codec)
        case Update.PullByFilter(filter) =>
          val writer = new BsonDocumentWriter(new BsonDocument())
          writer.writeStartDocument()
          writer.writeName("$pull")
          bsonDocumentCodec.encode(
            writer,
            filter.toBson.toBsonDocument(documentClass, codecRegistry),
            EncoderContext.builder().build(),
          )
          writer.writeEndDocument()
          writer.getDocument()
        case Update.PullAll(fieldName, values, codec) =>
          pullAllUpdate(fieldName, values, codec)
        case Update.PopFirst(fieldName) =>
          simpleUpdate("$pop", fieldName, -1, intCodec)
        case Update.PopLast(fieldName) =>
          simpleUpdate("$pop", fieldName, 1, intCodec)
        case Update.BitwiseAndInt(fieldName, value) =>
          createBitUpdateDocumentInt(fieldName, "and", value)
        case Update.BitwiseAndLong(fieldName, value) =>
          createBitUpdateDocumentLong(fieldName, "and", value)
        case Update.BitwiseOrInt(fieldName, value) =>
          createBitUpdateDocumentInt(fieldName, "or", value)
        case Update.BitwiseOrLong(fieldName, value) =>
          createBitUpdateDocumentLong(fieldName, "or", value)
        case Update.BitwiseXorInt(fieldName, value) =>
          createBitUpdateDocumentInt(fieldName, "xor", value)
        case Update.BitwiseXorLong(fieldName, value) =>
          createBitUpdateDocumentLong(fieldName, "xor", value)
      }
    }
  }
}

object Update {
  final case class Combine(updates: Seq[Update])                                      extends Update
  final case class Set[A](fieldName: String, value: A, codec: Codec[A])               extends Update
  final case class Unset(fieldName: String)                                           extends Update
  final case class SetOnInsert[A](fieldName: String, value: A, codec: Codec[A])       extends Update
  final case class Rename(fieldName: String, newFieldName: String)                    extends Update
  final case class Increment[A](fieldName: String, number: A, codec: Codec[A])        extends Update
  final case class Multiply[A](fieldName: String, number: A, codec: Codec[A])         extends Update
  final case class Min[A](fieldName: String, value: A, codec: Codec[A])               extends Update
  final case class Max[A](fieldName: String, value: A, codec: Codec[A])               extends Update
  final case class CurrentDate(fieldName: String)                                     extends Update
  final case class CurrentTimestamp(fieldName: String)                                extends Update
  final case class AddToSet[A](fieldName: String, value: A, codec: Codec[A])          extends Update
  final case class AddEachToSet[A](fieldName: String, value: Seq[A], codec: Codec[A]) extends Update
  final case class Push[A](fieldName: String, value: A, codec: Codec[A])              extends Update
  final case class PushEach[A](fieldName: String, values: Seq[A], codec: Codec[A])    extends Update
  final case class Pull[A](fieldName: String, value: A, codec: Codec[A])              extends Update
  final case class PullByFilter(filter: Filter)                                       extends Update
  final case class PullAll[A](fieldName: String, values: Seq[A], codec: Codec[A])     extends Update
  final case class PopFirst(fieldName: String)                                        extends Update
  final case class PopLast(fieldName: String)                                         extends Update
  final case class BitwiseAndInt(fieldName: String, value: Int)                       extends Update
  final case class BitwiseAndLong(fieldName: String, value: Long)                     extends Update
  final case class BitwiseOrInt(fieldName: String, value: Int)                        extends Update
  final case class BitwiseOrLong(fieldName: String, value: Long)                      extends Update
  final case class BitwiseXorInt(fieldName: String, value: Int)                       extends Update
  final case class BitwiseXorLong(fieldName: String, value: Long)                     extends Update
}
