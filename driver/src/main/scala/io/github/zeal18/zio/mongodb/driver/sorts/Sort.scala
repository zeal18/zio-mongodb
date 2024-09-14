package io.github.zeal18.zio.mongodb.driver.sorts

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.BsonString
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import scala.jdk.CollectionConverters.*

sealed trait Sort extends Bson { self =>
  override def toBsonDocument[TDocument <: Object](
    documentClass: Class[TDocument],
    codecRegistry: CodecRegistry,
  ): BsonDocument = self match {
    case Sort.Asc(fieldName)  => new BsonDocument(fieldName, new BsonInt32(1))
    case Sort.Desc(fieldName) => new BsonDocument(fieldName, new BsonInt32(-1))
    case Sort.TextScore(fieldName) =>
      new BsonDocument(fieldName, new BsonDocument("$meta", new BsonString("textScore")))
    case Sort.Compound(sorts) =>
      val doc = new BsonDocument()
      sorts.foreach { sort =>
        val sortDoc = sort.toBsonDocument(documentClass, codecRegistry)

        sortDoc.keySet().asScala.foreach { key =>
          doc.append(key, sortDoc.get(key))
        }
      }

      doc
    case Sort.Raw(bson) => bson.toBsonDocument()
  }
}

object Sort {
  final case class Raw(bson: Bson) extends Sort

  final case class Asc(fieldName: String)        extends Sort
  final case class Desc(fieldName: String)       extends Sort
  final case class TextScore(fieldName: String)  extends Sort
  final case class Compound(sortings: Seq[Sort]) extends Sort
}
