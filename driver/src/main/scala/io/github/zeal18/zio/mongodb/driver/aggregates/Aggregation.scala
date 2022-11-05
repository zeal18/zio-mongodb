package io.github.zeal18.zio.mongodb.driver.aggregates

import scala.annotation.nowarn
import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import io.github.zeal18.zio.mongodb.driver.sorts
import org.bson.BsonBoolean
import org.bson.BsonDocumentWriter
import org.bson.BsonInt32
import org.bson.BsonString
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

sealed trait Aggregation extends Bson { self =>
  @nowarn("msg=possible missing interpolator*")
  override def toBsonDocument[TDocument <: Object](
    documentClass: Class[TDocument],
    codecRegistry: CodecRegistry,
  ): BsonDocument = {
    val documentClass =
      implicitly[ClassTag[BsonDocument]].runtimeClass
        .asInstanceOf[Class[BsonDocument]] // scalafix:ok
    val context = EncoderContext.builder().build()

    def simplePipelineStage(name: String, value: Bson): BsonDocument =
      new BsonDocument(name, value.toBsonDocument(documentClass, codecRegistry))

    def facetStage(facets: Seq[Facet]): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())
      writer.writeStartDocument()
      writer.writeName("$facet")
      writer.writeStartDocument()
      facets.foreach { facet =>
        writer.writeName(facet.name)
        writer.writeStartArray()
        facet.pipeline.foreach { p =>
          val document = p.toBsonDocument(documentClass, codecRegistry)
          Codec[BsonDocument].encode(writer, document, context)
        }
        writer.writeEndArray()

      }
      writer.writeEndDocument()
      writer.writeEndDocument()

      writer.getDocument()
    }

    def unwind(fieldName: String, unwindOptions: UnwindOptions): BsonDocument = {
      val options = new BsonDocument("path", new BsonString(fieldName))

      unwindOptions.preserveNullAndEmptyArrays.foreach { preserveNullAndEmptyArrays =>
        options.append(
          "preserveNullAndEmptyArrays",
          BsonBoolean.valueOf(preserveNullAndEmptyArrays),
        )
      }

      unwindOptions.includeArrayIndex.foreach { includeArrayIndex =>
        options.append("includeArrayIndex", new BsonString(includeArrayIndex))
      }

      new BsonDocument("$unwind", options);
    }

    def groupStage[A](id: A, accumulators: Seq[Accumulator], codec: Codec[A]): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeStartDocument("$group")
      writer.writeName("_id")
      codec.encode(writer, id, context)
      accumulators.foreach { acc =>
        val bsonField = acc.toBsonField

        writer.writeName(bsonField.getName())
        codecRegistry
          .get(documentClass)
          .encode(
            writer,
            bsonField.getValue().toBsonDocument(documentClass, codecRegistry),
            context,
          )
      }

      writer.writeEndDocument()
      writer.writeEndDocument()

      writer.getDocument()
    }

    def lookupStage(
      from: String,
      let: Seq[Variable[?]],
      pipeline: Seq[Aggregation],
      as: String,
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()

      writer.writeStartDocument("$lookup")

      writer.writeString("from", from)

      if (let.nonEmpty) {
        writer.writeStartDocument("let")
        let.foreach { variable =>
          writer.writeName(variable.name)
          variable.encode(writer)
        }
        writer.writeEndDocument()
      }

      writer.writeName("pipeline")
      writer.writeStartArray()
      pipeline.foreach { stage =>
        codecRegistry
          .get(documentClass)
          .encode(
            writer,
            stage.toBsonDocument(documentClass, codecRegistry),
            context,
          )
      }
      writer.writeEndArray()

      writer.writeString("as", as)
      writer.writeEndDocument()

      writer.getDocument()
    }

    self match {
      case Aggregation.Match(filter) =>
        simplePipelineStage("$match", filter)
      case Aggregation.Limit(limit) =>
        new BsonDocument("$limit", new BsonInt32(limit))
      case Aggregation.Count(field) =>
        new BsonDocument("$count", new BsonString(field))
      case Aggregation.Facets(facets) =>
        facetStage(facets)
      case Aggregation.Unwind(fieldName, unwindOptions) =>
        unwind(fieldName, unwindOptions)
      case Aggregation.Group(id, fieldAccumulators, codec) =>
        groupStage(id, fieldAccumulators, codec)
      case Aggregation.Project(projection) =>
        simplePipelineStage("$project", projection)
      case Aggregation.Sort(sort) =>
        new BsonDocument(
          "$sort",
          sort.toBsonDocument(documentClass, codecRegistry),
        )
      case Aggregation.Lookup(from, localField, foreignField, as) =>
        new BsonDocument(
          "$lookup",
          new BsonDocument("from", new BsonString(from))
            .append("localField", new BsonString(localField))
            .append("foreignField", new BsonString(foreignField))
            .append("as", new BsonString(as)),
        )
      case Aggregation.LookupPipeline(from, let, pipeline, as) =>
        lookupStage(from, let, pipeline, as)
      case Aggregation.Raw(bson) => bson.toBsonDocument()
    }
  }
}

object Aggregation {
  final case class Match(filter: Filter)                                   extends Aggregation
  final case class Limit(limit: Int)                                       extends Aggregation
  final case class Count(field: String)                                    extends Aggregation
  final case class Facets(facets: Seq[Facet])                              extends Aggregation
  final case class Unwind(fieldName: String, unwindOptions: UnwindOptions) extends Aggregation
  final case class Group[Id](id: Id, fieldAccumulators: Seq[Accumulator], codec: Codec[Id])
      extends Aggregation
  final case class Project(projection: Projection) extends Aggregation
  final case class Lookup(from: String, localField: String, foreignField: String, as: String)
      extends Aggregation
  final case class Sort(sort: sorts.Sort) extends Aggregation
  final case class LookupPipeline(
    from: String,
    let: Seq[Variable[?]],
    pipeline: Seq[Aggregation],
    as: String,
  ) extends Aggregation
  final case class Raw(filter: Bson) extends Aggregation
}
