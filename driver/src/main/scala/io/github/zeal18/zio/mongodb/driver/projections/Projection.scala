package io.github.zeal18.zio.mongodb.driver.projections
import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import org.bson.BsonArray
import org.bson.BsonDocumentWriter
import org.bson.BsonInt32
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import java.util as ju
import scala.jdk.CollectionConverters.*

sealed trait Projection extends Bson { self =>
  override def toBsonDocument[TDocument <: Object](
    documentClass: Class[TDocument],
    codecRegistry: CodecRegistry,
  ): BsonDocument = {
    def simpleExpression[A](fieldName: String, value: A, codec: Codec[A]): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeName(fieldName)
      codec.encode(writer, value, EncoderContext.builder().build())
      writer.writeEndDocument()

      writer.getDocument()
    }

    def combine(fieldNames: Seq[String], value: BsonValue): BsonDocument = {
      val document = new BsonDocument()
      fieldNames.foreach { fieldName =>
        document.remove(fieldName)
        document.append(fieldName, value)
      }

      document
    }

    def fields(projections: Seq[Projection]): BsonDocument = {
      val combinedDocument = new BsonDocument()
      projections.foreach { sort =>
        val sortDocument = sort.toBsonDocument(documentClass, codecRegistry)
        sortDocument.keySet().asScala.foreach { key =>
          combinedDocument.remove(key)
          combinedDocument.append(key, sortDocument.get(key))
        }
      }
      combinedDocument
    }

    self match {
      case Projection.Computed(fieldName, expression, codec) =>
        simpleExpression(fieldName, expression, codec)
      case Projection.Include(fieldNames) =>
        combine(fieldNames, new BsonInt32(1))
      case Projection.Exclude(fieldNames) =>
        combine(fieldNames, new BsonInt32(0))
      case Projection.ExcludeId =>
        new BsonDocument("_id", new BsonInt32(0))
      case Projection.ElemFirstMatch(fieldName) =>
        new BsonDocument(fieldName + ".$", new BsonInt32(1))
      case Projection.ElemMatch(fieldName, filter) =>
        new BsonDocument(
          fieldName,
          new BsonDocument(
            "$elemMatch",
            filter.toBsonDocument(documentClass, codecRegistry),
          ),
        )
      case Projection.Meta(fieldName, metaFieldName) =>
        new BsonDocument(fieldName, new BsonDocument("$meta", new BsonString(metaFieldName)))
      case Projection.Slice(fieldName, skip, limit) =>
        if (skip <= 0)
          new BsonDocument(fieldName, new BsonDocument("$slice", new BsonInt32(limit)))
        else
          new BsonDocument(
            fieldName,
            new BsonDocument(
              "$slice",
              new BsonArray(ju.Arrays.asList(new BsonInt32(skip), new BsonInt32(limit))),
            ),
          )
      case Projection.Fields(projections) => fields(projections)
      case Projection.Raw(bson)           => bson.toBsonDocument()
    }
  }
}

object Projection {
  final case class Raw(bson: Bson) extends Projection

  final case class Computed[A](fieldName: String, expression: A, codec: Codec[A]) extends Projection
  final case class Include(fieldNames: Seq[String])                               extends Projection
  final case class Exclude(fieldNames: Seq[String])                               extends Projection
  case object ExcludeId                                                           extends Projection
  final case class ElemFirstMatch(fieldName: String)                              extends Projection
  final case class ElemMatch(fieldName: String, filter: Filter)                   extends Projection
  final case class Meta(fieldName: String, metaFieldName: String)                 extends Projection
  final case class Slice(fieldName: String, skip: Int, limit: Int)                extends Projection
  final case class Fields(fields: Seq[Projection])                                extends Projection
}
