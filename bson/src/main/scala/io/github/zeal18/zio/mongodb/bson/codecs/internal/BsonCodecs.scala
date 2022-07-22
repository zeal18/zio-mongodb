package io.github.zeal18.zio.mongodb.bson.codecs.internal

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
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
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

private[codecs] trait BsonCodecs {
  private lazy val bsonDocumentCodec: BsonDocumentCodec = new BsonDocumentCodec()

  implicit lazy val bsonArray: Codec[BsonArray]     = Codec[BsonArray](new BsonArrayCodec())
  implicit lazy val bsonBinary: Codec[BsonBinary]   = Codec[BsonBinary](new BsonBinaryCodec())
  implicit lazy val bsonBoolean: Codec[BsonBoolean] = Codec[BsonBoolean](new BsonBooleanCodec())
  implicit lazy val bsonDateTime: Codec[BsonDateTime] =
    Codec[BsonDateTime](new BsonDateTimeCodec())
  implicit lazy val bsonDecimal128: Codec[BsonDecimal128] =
    Codec[BsonDecimal128](new BsonDecimal128Codec())
  implicit lazy val bsonDocument: Codec[BsonDocument] =
    Codec[BsonDocument](bsonDocumentCodec)
  implicit lazy val bsonDouble: Codec[BsonDouble] = Codec[BsonDouble](new BsonDoubleCodec())
  implicit lazy val bsonInt32: Codec[BsonInt32]   = Codec[BsonInt32](new BsonInt32Codec())
  implicit lazy val bsonInt64: Codec[BsonInt64]   = Codec[BsonInt64](new BsonInt64Codec())
  implicit lazy val bsonJavaScript: Codec[BsonJavaScript] =
    Codec[BsonJavaScript](new BsonJavaScriptCodec())
  implicit lazy val bsonJavaScriptWithScope: Codec[BsonJavaScriptWithScope] =
    Codec[BsonJavaScriptWithScope](new BsonJavaScriptWithScopeCodec(bsonDocumentCodec))
  implicit lazy val bsonMaxKey: Codec[BsonMaxKey] = Codec[BsonMaxKey](new BsonMaxKeyCodec())
  implicit lazy val bsonMinKey: Codec[BsonMinKey] = Codec[BsonMinKey](new BsonMinKeyCodec())
  implicit lazy val bsonNull: Codec[BsonNull]     = Codec[BsonNull](new BsonNullCodec())
  implicit lazy val bsonObjectId: Codec[BsonObjectId] =
    Codec[BsonObjectId](new BsonObjectIdCodec())
  implicit lazy val bsonRegularExpression: Codec[BsonRegularExpression] =
    Codec[BsonRegularExpression](new BsonRegularExpressionCodec())
  implicit lazy val bsonString: Codec[BsonString] = Codec[BsonString](new BsonStringCodec())
  implicit lazy val bsonSymbol: Codec[BsonSymbol] = Codec[BsonSymbol](new BsonSymbolCodec())
  implicit lazy val bsonTimestamp: Codec[BsonTimestamp] =
    Codec[BsonTimestamp](new BsonTimestampCodec())
  implicit lazy val bsonUndefined: Codec[BsonUndefined] =
    Codec[BsonUndefined](new BsonUndefinedCodec())
  implicit lazy val bsonValue: Codec[BsonValue]   = Codec[BsonValue](new BsonValueCodec())
  implicit lazy val objectId: Codec[ObjectId]     = Codec[ObjectId](new ObjectIdCodec())
  implicit lazy val decimal128: Codec[Decimal128] = Codec[Decimal128](new Decimal128Codec())
}
