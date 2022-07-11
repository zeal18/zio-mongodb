package io.github.zeal18.zio.mongodb.bson.codecs.encoders

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
import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
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

trait BsonEncoders {
  private lazy val bsonDocumentCodec: BsonDocumentCodec = new BsonDocumentCodec()

  implicit lazy val bsonArray: Encoder[BsonArray]     = Encoder[BsonArray](new BsonArrayCodec())
  implicit lazy val bsonBinary: Encoder[BsonBinary]   = Encoder[BsonBinary](new BsonBinaryCodec())
  implicit lazy val bsonBoolean: Encoder[BsonBoolean] = Encoder[BsonBoolean](new BsonBooleanCodec())
  implicit lazy val bsonDateTime: Encoder[BsonDateTime] =
    Encoder[BsonDateTime](new BsonDateTimeCodec())
  implicit lazy val bsonDecimal128: Encoder[BsonDecimal128] =
    Encoder[BsonDecimal128](new BsonDecimal128Codec())
  implicit lazy val bsonDocument: Encoder[BsonDocument] =
    Encoder[BsonDocument](bsonDocumentCodec)
  implicit lazy val bsonDouble: Encoder[BsonDouble] = Encoder[BsonDouble](new BsonDoubleCodec())
  implicit lazy val bsonInt32: Encoder[BsonInt32]   = Encoder[BsonInt32](new BsonInt32Codec())
  implicit lazy val bsonInt64: Encoder[BsonInt64]   = Encoder[BsonInt64](new BsonInt64Codec())
  implicit lazy val bsonJavaScript: Encoder[BsonJavaScript] =
    Encoder[BsonJavaScript](new BsonJavaScriptCodec())
  implicit lazy val bsonJavaScriptWithScope: Encoder[BsonJavaScriptWithScope] =
    Encoder[BsonJavaScriptWithScope](new BsonJavaScriptWithScopeCodec(bsonDocumentCodec))
  implicit lazy val bsonMaxKey: Encoder[BsonMaxKey] = Encoder[BsonMaxKey](new BsonMaxKeyCodec())
  implicit lazy val bsonMinKey: Encoder[BsonMinKey] = Encoder[BsonMinKey](new BsonMinKeyCodec())
  implicit lazy val bsonNull: Encoder[BsonNull]     = Encoder[BsonNull](new BsonNullCodec())
  implicit lazy val bsonObjectId: Encoder[BsonObjectId] =
    Encoder[BsonObjectId](new BsonObjectIdCodec())
  implicit lazy val bsonRegularExpression: Encoder[BsonRegularExpression] =
    Encoder[BsonRegularExpression](new BsonRegularExpressionCodec())
  implicit lazy val bsonString: Encoder[BsonString] = Encoder[BsonString](new BsonStringCodec())
  implicit lazy val bsonSymbol: Encoder[BsonSymbol] = Encoder[BsonSymbol](new BsonSymbolCodec())
  implicit lazy val bsonTimestamp: Encoder[BsonTimestamp] =
    Encoder[BsonTimestamp](new BsonTimestampCodec())
  implicit lazy val bsonUndefined: Encoder[BsonUndefined] =
    Encoder[BsonUndefined](new BsonUndefinedCodec())
  implicit lazy val bsonValue: Encoder[BsonValue]   = Encoder[BsonValue](new BsonValueCodec())
  implicit lazy val objectId: Encoder[ObjectId]     = Encoder[ObjectId](new ObjectIdCodec())
  implicit lazy val decimal128: Encoder[Decimal128] = Encoder[Decimal128](new Decimal128Codec())
}
