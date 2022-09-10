package io.github.zeal18.zio.mongodb.driver.filters

import scala.annotation.nowarn

import com.mongodb.client.model.TextSearchOptions
import com.mongodb.client.model.Filters as JFilters
import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation
import org.bson.BsonArray
import org.bson.BsonDocumentWriter
import org.bson.BsonRegularExpression
import org.bson.BsonString
import org.bson.BsonType
import org.bson.BsonValue
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

sealed trait Filter extends Bson { self =>
  @nowarn("msg=possible missing interpolator")
  override def toBsonDocument[TDocument <: Object](
    documentClass: Class[TDocument],
    codecRegistry: CodecRegistry,
  ): BsonDocument = {

    def simpleFilter(fieldName: String, value: BsonValue): BsonDocument =
      new BsonDocument(fieldName, value)
    def simpleEncodingFilter[A](
      fieldName: String,
      value: A,
      encoder: Encoder[A],
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeName(fieldName)
      encoder.encode(writer, value, EncoderContext.builder().build())
      writer.writeEndDocument()

      writer.getDocument()
    }
    def operatorFilter[A](
      operator: String,
      fieldName: String,
      value: A,
      encoder: Encoder[A],
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument();
      writer.writeName(fieldName);
      writer.writeStartDocument();
      writer.writeName(operator);
      encoder.encode(writer, value, EncoderContext.builder().build())
      writer.writeEndDocument();
      writer.writeEndDocument();

      writer.getDocument()
    }
    def iterableOperatorFilter[A](
      operator: String,
      fieldName: String,
      values: Iterable[A],
      encoder: Encoder[A],
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument();
      writer.writeName(fieldName);

      writer.writeStartDocument();
      writer.writeName(operator);
      writer.writeStartArray();
      values.foreach(encoder.encode(writer, _, EncoderContext.builder().build()))
      writer.writeEndArray();
      writer.writeEndDocument();

      writer.writeEndDocument();

      writer.getDocument()
    }

    def combineFilters(operator: String, filters: Set[Filter]): BsonDocument = {
      val clauses = new BsonArray()
      filters.foreach(f => clauses.add(f.toBsonDocument(documentClass, codecRegistry)))

      new BsonDocument(operator, clauses)
    }
    def notFilter(filter: Bson): BsonDocument =
      JFilters.not(filter).toBsonDocument(documentClass, codecRegistry)
    def textFilter(
      search: String,
      language: Option[String],
      caseSensitive: Option[Boolean],
      diacriticSensitive: Option[Boolean],
    ): BsonDocument = {
      val textOptions = new TextSearchOptions()
      language.foreach(textOptions.language)
      caseSensitive.foreach(textOptions.caseSensitive(_))
      diacriticSensitive.foreach(textOptions.diacriticSensitive(_))

      JFilters.text(search, textOptions).toBsonDocument(documentClass, codecRegistry)
    }

    self match {
      case Filter.Empty => new BsonDocument()
      case Filter.Eq(fieldName, value, encoder) =>
        simpleEncodingFilter(fieldName, value, encoder)
      case Filter.Ne(fieldName, value, encoder) =>
        operatorFilter("$ne", fieldName, value, encoder)
      case Filter.Gt(fieldName, value, encoder) =>
        operatorFilter("$gt", fieldName, value, encoder)
      case Filter.Gte(fieldName, value, encoder) =>
        operatorFilter("$gte", fieldName, value, encoder)
      case Filter.Lt(fieldName, value, encoder) =>
        operatorFilter("$lt", fieldName, value, encoder)
      case Filter.Lte(fieldName, value, encoder) =>
        operatorFilter("$lte", fieldName, value, encoder)
      case Filter.In(fieldName, value, encoder) =>
        iterableOperatorFilter("$in", fieldName, value.toSeq, encoder)
      case Filter.Nin(fieldName, value, encoder) =>
        iterableOperatorFilter("$nin", fieldName, value.toSeq, encoder)
      case Filter.And(filters) => combineFilters("$and", filters)
      case Filter.Or(filters)  => combineFilters("$or", filters)
      case Filter.Nor(filters) => combineFilters("$nor", filters)
      case Filter.Not(filter)  => notFilter(filter)
      case Filter.Exists(fieldName, exists) =>
        operatorFilter("$exists", fieldName, exists, Codec[Boolean])
      case Filter.Type(fieldName, bsonType) =>
        operatorFilter("$type", fieldName, bsonType.getValue(), Codec[Int])
      case Filter.Mod(fieldName, divisor, remainder) =>
        operatorFilter[Seq[Long]]("$mod", fieldName, Seq(divisor, remainder), Codec[Seq[Long]])
      case Filter.Regex(fieldName, pattern, options) =>
        simpleFilter(fieldName, new BsonRegularExpression(pattern, options))
      case Filter.Text(search, language, caseSensitive, diacriticSensitive) =>
        textFilter(search, language, caseSensitive, diacriticSensitive)
      case Filter.Where(javaScriptExpression) =>
        new BsonDocument("$where", new BsonString(javaScriptExpression))
      case Filter.Expr(expression) =>
        JFilters.expr(expression).toBsonDocument(documentClass, codecRegistry)
      case Filter.All(fieldName, values, encoder) =>
        iterableOperatorFilter("$all", fieldName, values, encoder)
      case Filter.ElemMatch(fieldName, filter) =>
        new BsonDocument(
          fieldName,
          new BsonDocument(
            "$elemMatch",
            filter.toBsonDocument(documentClass, codecRegistry),
          ),
        );
      case Filter.Size(fieldName, size) =>
        operatorFilter("$size", fieldName, size, Codec[Int])
      case Filter.BitsAllClear(fieldName, bitmask) =>
        operatorFilter("$bitsAllClear", fieldName, bitmask, Codec[Long])
      case Filter.BitsAllSet(fieldName, bitmask) =>
        operatorFilter("$bitsAllSet", fieldName, bitmask, Codec[Long])
      case Filter.BitsAnyClear(fieldName, bitmask) =>
        operatorFilter("$bitsAnyClear", fieldName, bitmask, Codec[Long])
      case Filter.BitsAnySet(fieldName, bitmask) =>
        operatorFilter("$bitsAnySet", fieldName, bitmask, Codec[Long])
      case Filter.JsonSchema(schema) =>
        simpleFilter("$jsonSchema", schema.toBsonDocument())
      case Filter.Raw(bson) => bson.toBsonDocument()
    }
  }
}

object Filter {
  case object Empty extends Filter

  final case class Raw(filter: Bson) extends Filter

  final case class Eq[A](fieldName: String, value: A, encoder: Encoder[A])        extends Filter
  final case class Ne[A](fieldName: String, value: A, encoder: Encoder[A])        extends Filter
  final case class Gt[A](fieldName: String, value: A, encoder: Encoder[A])        extends Filter
  final case class Gte[A](fieldName: String, value: A, encoder: Encoder[A])       extends Filter
  final case class Lt[A](fieldName: String, value: A, encoder: Encoder[A])        extends Filter
  final case class Lte[A](fieldName: String, value: A, encoder: Encoder[A])       extends Filter
  final case class In[A](fieldName: String, values: Set[A], encoder: Encoder[A])  extends Filter
  final case class Nin[A](fieldName: String, values: Set[A], encoder: Encoder[A]) extends Filter

  final case class And(filters: Set[Filter])                   extends Filter
  final case class Or(filters: Set[Filter])                    extends Filter
  final case class Nor(filters: Set[Filter])                   extends Filter
  final case class Not(filter: Filter)                         extends Filter
  final case class Exists(fieldName: String, exists: Boolean)  extends Filter
  final case class Type(fieldName: String, bsonType: BsonType) extends Filter

  final case class Mod(fieldName: String, divisor: Long, remainder: Long)     extends Filter
  final case class Regex(fieldName: String, pattern: String, options: String) extends Filter

  final case class Text(
    search: String,
    language: Option[String],
    caseSensitive: Option[Boolean],
    diacriticSensitive: Option[Boolean],
  ) extends Filter
  final case class Where(javaScriptExpression: String)                            extends Filter
  final case class Expr(expression: Aggregation)                                  extends Filter
  final case class All[A](fieldName: String, values: Set[A], encoder: Encoder[A]) extends Filter
  final case class ElemMatch(fieldName: String, filter: Filter)                   extends Filter
  final case class Size(fieldName: String, size: Int)                             extends Filter

  final case class BitsAllClear(fieldName: String, bitmask: Long) extends Filter
  final case class BitsAllSet(fieldName: String, bitmask: Long)   extends Filter
  final case class BitsAnyClear(fieldName: String, bitmask: Long) extends Filter
  final case class BitsAnySet(fieldName: String, bitmask: Long)   extends Filter

  final case class JsonSchema(schema: Bson) extends Filter
}
