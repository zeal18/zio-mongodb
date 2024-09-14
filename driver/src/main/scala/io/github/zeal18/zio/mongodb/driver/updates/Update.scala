package io.github.zeal18.zio.mongodb.driver.updates

import com.mongodb.client.model.Updates as JUpdates
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.BsonInt32
import org.bson.BsonInt64
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import scala.jdk.CollectionConverters.*

sealed trait Update extends Bson { self =>
  override def toBsonDocument[TDocument <: Object](
    documentClass: Class[TDocument],
    codecRegistry: CodecRegistry,
  ): BsonDocument = {
    def simpleUpdate[A](
      operator: String,
      fieldName: String,
      value: A,
      encoder: Encoder[A],
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeName(operator)

      writer.writeStartDocument()
      writer.writeName(fieldName)
      encoder.encode(writer, value, EncoderContext.builder().build())
      writer.writeEndDocument()

      writer.writeEndDocument()

      writer.getDocument()
    }

    def withEachUpdate[A](
      operator: String,
      fieldName: String,
      values: Iterable[A],
      encoder: Encoder[A],
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
        encoder.encode(writer, value, context)
      }
      writer.writeEndArray()

      writer.writeEndDocument()
      writer.writeEndDocument()
      writer.writeEndDocument()

      writer.getDocument()
    }

    def pushUpdate[A](
      operator: String,
      fieldName: String,
      values: Iterable[A],
      options: Option[PushOptions],
      encoder: Encoder[A],
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
        encoder.encode(writer, value, context)
      }
      writer.writeEndArray()

      options.foreach { options =>
        options.position.foreach { position =>
          writer.writeInt32(s"$$position", position)
        }
        options.slice.foreach { slice =>
          writer.writeInt32(s"$$slice", slice)
        }
        options.sort.foreach {
          case Left(sort) => writer.writeInt32(s"$$sort", if (sort) 1 else -1)
          case Right(sort) =>
            writer.writeName(s"$$sort")
            val codec        = codecRegistry.get(classOf[BsonDocument])
            val sortDocument = sort.toBsonDocument(classOf[BsonDocument], codecRegistry)
            codec.encode(writer, sortDocument, EncoderContext.builder().build())
        }
      }

      writer.writeEndDocument()
      writer.writeEndDocument()
      writer.writeEndDocument()

      writer.getDocument()
    }

    def pullAllUpdate[A](
      fieldName: String,
      values: Iterable[A],
      encoder: Encoder[A],
    ): BsonDocument = {
      val writer  = new BsonDocumentWriter(new BsonDocument())
      val context = EncoderContext.builder().build()

      writer.writeStartDocument()
      writer.writeName("$pullAll")

      writer.writeStartDocument()
      writer.writeName(fieldName)

      writer.writeStartArray()
      values.foreach(value => encoder.encode(writer, value, context))
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
        JUpdates.combine(updates.asJava).toBsonDocument(documentClass, codecRegistry)
      case Update.Set(fieldName, value, encoder) =>
        simpleUpdate("$set", fieldName, value, encoder)
      case Update.Unset(fieldName) =>
        simpleUpdate("$unset", fieldName, "", Codec[String])
      case Update.SetOnInsert(fieldName, value, encoder) =>
        simpleUpdate("$setOnInsert", fieldName, value, encoder)
      case Update.Rename(fieldName, newFieldName) =>
        simpleUpdate("$rename", fieldName, newFieldName, Codec[String])
      case Update.Increment(fieldName, number, encoder) =>
        simpleUpdate("$inc", fieldName, number, encoder)
      case Update.Multiply(fieldName, number, encoder) =>
        simpleUpdate("$mul", fieldName, number, encoder)
      case Update.Min(fieldName, value, encoder) =>
        simpleUpdate("$min", fieldName, value, encoder)
      case Update.Max(fieldName, value, encoder) =>
        simpleUpdate("$max", fieldName, value, encoder)
      case Update.CurrentDate(fieldName) =>
        simpleUpdate("$currentDate", fieldName, true, Codec[Boolean])
      case Update.CurrentTimestamp(fieldName) =>
        new BsonDocument()
        simpleUpdate(
          "$currentDate",
          fieldName,
          new BsonDocument("$type", new BsonString("timestamp")),
          Codec[BsonDocument],
        )
      case Update.AddToSet(fieldName, value, encoder) =>
        simpleUpdate("$addToSet", fieldName, value, encoder)
      case Update.AddEachToSet(fieldName, values, encoder) =>
        withEachUpdate("$addToSet", fieldName, values, encoder)
      case Update.Push(fieldName, value, encoder) =>
        simpleUpdate("$push", fieldName, value, encoder)
      case Update.PushEach(fieldName, values, options, encoder) =>
        pushUpdate("$push", fieldName, values, options, encoder)
      case Update.Pull(fieldName, value, encoder) =>
        simpleUpdate("$pull", fieldName, value, encoder)
      case Update.PullByFilter(filter) =>
        val writer = new BsonDocumentWriter(new BsonDocument())
        writer.writeStartDocument()
        writer.writeName("$pull")
        Codec[BsonDocument].encode(
          writer,
          filter.toBsonDocument(documentClass, codecRegistry),
          EncoderContext.builder().build(),
        )
        writer.writeEndDocument()
        writer.getDocument()
      case Update.PullAll(fieldName, values, encoder) =>
        pullAllUpdate(fieldName, values, encoder)
      case Update.PopFirst(fieldName) =>
        simpleUpdate("$pop", fieldName, -1, Codec[Int])
      case Update.PopLast(fieldName) =>
        simpleUpdate("$pop", fieldName, 1, Codec[Int])
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
      case Update.Raw(bson) => bson.toBsonDocument()
    }
  }
}

object Update {
  final case class Raw(bson: Bson) extends Update

  final case class Combine(updates: Seq[Update])                                               extends Update
  final case class Set[A](fieldName: String, value: A, encoder: Encoder[A])                    extends Update
  final case class Unset(fieldName: String)                                                    extends Update
  final case class SetOnInsert[A](fieldName: String, value: A, encoder: Encoder[A])            extends Update
  final case class Rename(fieldName: String, newFieldName: String)                             extends Update
  final case class Increment[A](fieldName: String, number: A, encoder: Encoder[A])             extends Update
  final case class Multiply[A](fieldName: String, number: A, encoder: Encoder[A])              extends Update
  final case class Min[A](fieldName: String, value: A, encoder: Encoder[A])                    extends Update
  final case class Max[A](fieldName: String, value: A, encoder: Encoder[A])                    extends Update
  final case class CurrentDate(fieldName: String)                                              extends Update
  final case class CurrentTimestamp(fieldName: String)                                         extends Update
  final case class AddToSet[A](fieldName: String, value: A, encoder: Encoder[A])               extends Update
  final case class AddEachToSet[A](fieldName: String, value: Iterable[A], encoder: Encoder[A]) extends Update
  final case class Push[A](fieldName: String, value: A, encoder: Encoder[A])                   extends Update
  final case class PushEach[A](
    fieldName: String,
    values: Iterable[A],
    options: Option[PushOptions],
    encoder: Encoder[A],
  ) extends Update
  final case class Pull[A](fieldName: String, value: A, encoder: Encoder[A])               extends Update
  final case class PullByFilter(filter: Filter)                                            extends Update
  final case class PullAll[A](fieldName: String, values: Iterable[A], encoder: Encoder[A]) extends Update
  final case class PopFirst(fieldName: String)                                             extends Update
  final case class PopLast(fieldName: String)                                              extends Update
  final case class BitwiseAndInt(fieldName: String, value: Int)                            extends Update
  final case class BitwiseAndLong(fieldName: String, value: Long)                          extends Update
  final case class BitwiseOrInt(fieldName: String, value: Int)                             extends Update
  final case class BitwiseOrLong(fieldName: String, value: Long)                           extends Update
  final case class BitwiseXorInt(fieldName: String, value: Int)                            extends Update
  final case class BitwiseXorLong(fieldName: String, value: Long)                          extends Update
}
