package io.github.zeal18.zio.mongodb.driver.indexes

import java.util as ju

import scala.jdk.CollectionConverters.*

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonElement
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.bson.BsonString
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.indexes.IndexKey.Raw
import org.bson.BsonValue
import org.bson.codecs.configuration.CodecRegistry

sealed trait IndexKey { self =>
  def toBson: Bson = new Bson {
    override def toBsonDocument[TDocument <: Object](
      documentClass: Class[TDocument],
      codecRegistry: CodecRegistry,
    ): BsonDocument = {

      def compoundIndex(fieldNames: Seq[String], value: BsonValue): BsonDocument = {
        val doc = new BsonDocument()
        fieldNames.foreach(doc.append(_, value))
        doc
      }

      self match {
        case IndexKey.Asc(fieldNames)  => compoundIndex(fieldNames, new BsonInt32(1))
        case IndexKey.Desc(fieldNames) => compoundIndex(fieldNames, new BsonInt32(-1))
        case IndexKey.Geo2dsphere(fieldNames) =>
          compoundIndex(fieldNames, new BsonString("2dsphere"))
        case IndexKey.Geo2d(fieldNames) => compoundIndex(fieldNames, new BsonString("2d"))
        case IndexKey.GeoHaystack(fieldName, additionalFieldName) =>
          new BsonDocument(
            ju.Arrays.asList(
              new BsonElement(fieldName, new BsonString("geoHaystack")),
              new BsonElement(additionalFieldName, new BsonInt32(1)),
            ),
          )
        case IndexKey.Text(fieldNames)   => compoundIndex(fieldNames, new BsonString("text"))
        case IndexKey.Hashed(fieldNames) => compoundIndex(fieldNames, new BsonString("hashed"));
        case IndexKey.Compound(indexes) =>
          val doc = new BsonDocument()
          indexes.foreach { index =>
            val indexDoc = index.toBson.toBsonDocument(documentClass, codecRegistry)

            indexDoc.keySet().asScala.foreach { key =>
              doc.append(key, indexDoc.get(key))
            }
          }

          doc
        case Raw(index) => index.toBsonDocument()
      }
    }
  }
}

object IndexKey {
  final case class Raw(index: Bson) extends IndexKey

  final case class Asc(fieldNames: Seq[String])                                extends IndexKey
  final case class Desc(fieldNames: Seq[String])                               extends IndexKey
  final case class Geo2dsphere(fieldNames: Seq[String])                        extends IndexKey
  final case class Geo2d(fieldNames: Seq[String])                              extends IndexKey
  final case class GeoHaystack(fieldName: String, additionalFieldName: String) extends IndexKey
  final case class Text(fieldNames: Seq[String])                               extends IndexKey
  final case class Hashed(fieldNames: Seq[String])                             extends IndexKey
  final case class Compound(indexes: Seq[IndexKey])                            extends IndexKey
}
