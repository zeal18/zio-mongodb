package io.github.zeal18.zio.mongodb.bson.codecs.decoders

import io.github.zeal18.zio.mongodb.bson.BsonArray
import io.github.zeal18.zio.mongodb.bson.BsonBinary
import io.github.zeal18.zio.mongodb.bson.BsonBoolean
import io.github.zeal18.zio.mongodb.bson.BsonDateTime
import io.github.zeal18.zio.mongodb.bson.BsonDecimal128
import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonDouble
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.bson.BsonInt64
import io.github.zeal18.zio.mongodb.bson.BsonJavaScript
import io.github.zeal18.zio.mongodb.bson.BsonJavaScriptWithScope
import io.github.zeal18.zio.mongodb.bson.BsonMaxKey
import io.github.zeal18.zio.mongodb.bson.BsonMinKey
import io.github.zeal18.zio.mongodb.bson.BsonNull
import io.github.zeal18.zio.mongodb.bson.BsonObjectId
import io.github.zeal18.zio.mongodb.bson.BsonRegularExpression
import io.github.zeal18.zio.mongodb.bson.BsonString
import io.github.zeal18.zio.mongodb.bson.BsonSymbol
import io.github.zeal18.zio.mongodb.bson.BsonTimestamp
import io.github.zeal18.zio.mongodb.bson.BsonUndefined
import io.github.zeal18.zio.mongodb.bson.BsonValue
import io.github.zeal18.zio.mongodb.bson.Decimal128
import io.github.zeal18.zio.mongodb.bson.ObjectId
import io.github.zeal18.zio.mongodb.bson.codecs.Decoder
import org.bson.codecs.BsonArrayCodec
import org.bson.codecs.BsonBinaryCodec
import org.bson.codecs.BsonBooleanCodec
import org.bson.codecs.BsonDateTimeCodec
import org.bson.codecs.BsonDecimal128Codec
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.BsonDoubleCodec
import org.bson.codecs.BsonInt32Codec
import org.bson.codecs.BsonInt64Codec
import org.bson.codecs.BsonJavaScriptCodec
import org.bson.codecs.BsonJavaScriptWithScopeCodec
import org.bson.codecs.BsonMaxKeyCodec
import org.bson.codecs.BsonMinKeyCodec
import org.bson.codecs.BsonNullCodec
import org.bson.codecs.BsonObjectIdCodec
import org.bson.codecs.BsonRegularExpressionCodec
import org.bson.codecs.BsonStringCodec
import org.bson.codecs.BsonSymbolCodec
import org.bson.codecs.BsonTimestampCodec
import org.bson.codecs.BsonUndefinedCodec
import org.bson.codecs.BsonValueCodec
import org.bson.codecs.Decimal128Codec
import org.bson.codecs.ObjectIdCodec

trait BsonDecoders {
  private lazy val bsonDocumentCodec: BsonDocumentCodec = new BsonDocumentCodec()

  implicit lazy val bsonArray: Decoder[BsonArray]     = Decoder[BsonArray](new BsonArrayCodec())
  implicit lazy val bsonBinary: Decoder[BsonBinary]   = Decoder[BsonBinary](new BsonBinaryCodec())
  implicit lazy val bsonBoolean: Decoder[BsonBoolean] = Decoder[BsonBoolean](new BsonBooleanCodec())
  implicit lazy val bsonDateTime: Decoder[BsonDateTime] =
    Decoder[BsonDateTime](new BsonDateTimeCodec())
  implicit lazy val bsonDecimal128: Decoder[BsonDecimal128] =
    Decoder[BsonDecimal128](new BsonDecimal128Codec())
  implicit lazy val bsonDocument: Decoder[BsonDocument] =
    Decoder[BsonDocument](bsonDocumentCodec)
  implicit lazy val bsonDouble: Decoder[BsonDouble] = Decoder[BsonDouble](new BsonDoubleCodec())
  implicit lazy val bsonInt32: Decoder[BsonInt32]   = Decoder[BsonInt32](new BsonInt32Codec())
  implicit lazy val bsonInt64: Decoder[BsonInt64]   = Decoder[BsonInt64](new BsonInt64Codec())
  implicit lazy val bsonJavaScript: Decoder[BsonJavaScript] =
    Decoder[BsonJavaScript](new BsonJavaScriptCodec())
  implicit lazy val bsonJavaScriptWithScope: Decoder[BsonJavaScriptWithScope] =
    Decoder[BsonJavaScriptWithScope](new BsonJavaScriptWithScopeCodec(bsonDocumentCodec))
  implicit lazy val bsonMaxKey: Decoder[BsonMaxKey] = Decoder[BsonMaxKey](new BsonMaxKeyCodec())
  implicit lazy val bsonMinKey: Decoder[BsonMinKey] = Decoder[BsonMinKey](new BsonMinKeyCodec())
  implicit lazy val bsonNull: Decoder[BsonNull]     = Decoder[BsonNull](new BsonNullCodec())
  implicit lazy val bsonObjectId: Decoder[BsonObjectId] =
    Decoder[BsonObjectId](new BsonObjectIdCodec())
  implicit lazy val bsonRegularExpression: Decoder[BsonRegularExpression] =
    Decoder[BsonRegularExpression](new BsonRegularExpressionCodec())
  implicit lazy val bsonString: Decoder[BsonString] = Decoder[BsonString](new BsonStringCodec())
  implicit lazy val bsonSymbol: Decoder[BsonSymbol] = Decoder[BsonSymbol](new BsonSymbolCodec())
  implicit lazy val bsonTimestamp: Decoder[BsonTimestamp] =
    Decoder[BsonTimestamp](new BsonTimestampCodec())
  implicit lazy val bsonUndefined: Decoder[BsonUndefined] =
    Decoder[BsonUndefined](new BsonUndefinedCodec())
  implicit lazy val bsonValue: Decoder[BsonValue]   = Decoder[BsonValue](new BsonValueCodec())
  implicit lazy val objectId: Decoder[ObjectId]     = Decoder[ObjectId](new ObjectIdCodec())
  implicit lazy val decimal128: Decoder[Decimal128] = Decoder[Decimal128](new Decimal128Codec())
}
