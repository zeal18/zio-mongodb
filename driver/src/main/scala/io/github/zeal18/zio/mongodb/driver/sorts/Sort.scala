package io.github.zeal18.zio.mongodb.driver.sorts

import scala.jdk.CollectionConverters.*

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.BsonString
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

sealed trait Sort { self =>
  def toBson: Bson = new Bson {
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
          val sortDoc = sort.toBson.toBsonDocument(documentClass, codecRegistry)

          sortDoc.keySet().asScala.foreach { key =>
            doc.append(key, sortDoc.get(key))
          }
        }

        doc
    }
  }
}

object Sort {
  case class Asc(fieldName: String)        extends Sort
  case class Desc(fieldName: String)       extends Sort
  case class TextScore(fieldName: String)  extends Sort
  case class Compound(sortings: Seq[Sort]) extends Sort
}
