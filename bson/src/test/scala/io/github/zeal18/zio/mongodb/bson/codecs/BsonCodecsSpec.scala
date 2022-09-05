package io.github.zeal18.zio.mongodb.bson.codecs

import scala.jdk.CollectionConverters.*

import io.github.zeal18.zio.mongodb.bson.BsonArray
import io.github.zeal18.zio.mongodb.bson.BsonBoolean
import io.github.zeal18.zio.mongodb.bson.BsonDateTime
import io.github.zeal18.zio.mongodb.bson.BsonDecimal128
import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonDouble
import io.github.zeal18.zio.mongodb.bson.BsonElement
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
import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import org.bson.BsonBinary
import org.bson.BsonValue
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import zio.test.ZIOSpecDefault

object BsonCodecsSpec extends ZIOSpecDefault {
  override def spec = suite("BsonCodecsSpec")(
    suite("BsonArray")(
      testCodecRoundtrip[BsonArray]("empty", new BsonArray(), "[]"),
      testCodecRoundtrip[BsonArray](
        "BsonArray(1)",
        new BsonArray(List(new BsonInt32(1)).asJava),
        "[1]",
      ),
      testCodecRoundtrip[BsonArray](
        "BsonArray(1, 2, 3)",
        new BsonArray(List(new BsonInt32(1), new BsonInt32(2), new BsonInt32(3)).asJava),
        "[1, 2, 3]",
      ),
    ),
    suite("BsonBinary")(
      testCodecRoundtrip[BsonBinary](
        "empty",
        new BsonBinary(Array.emptyByteArray),
        """{"$binary": {"base64": "", "subType": "00"}}""",
      ),
      testCodecRoundtrip[BsonBinary](
        "BsonBinary(1)",
        new BsonBinary(Array.apply(1.toByte)),
        """{"$binary": {"base64": "AQ==", "subType": "00"}}""",
      ),
      testCodecRoundtrip[BsonBinary](
        "BsonBinary(1, 2, 3)",
        new BsonBinary(Array.apply(1.toByte, 2.toByte, 3.toByte)),
        """{"$binary": {"base64": "AQID", "subType": "00"}}""",
      ),
    ),
    suite("BsonBoolean")(
      testCodecRoundtrip[BsonBoolean]("true", new BsonBoolean(true), "true"),
      testCodecRoundtrip[BsonBoolean]("false", new BsonBoolean(false), "false"),
    ),
    suite("BsonDateTime")(
      testCodecRoundtrip[BsonDateTime](
        "0",
        new BsonDateTime(0),
        """{"$date": "1970-01-01T00:00:00Z"}""",
      ),
      testCodecRoundtrip[BsonDateTime](
        "min",
        new BsonDateTime(Long.MinValue),
        """{"$date": {"$numberLong": "-9223372036854775808"}}""",
      ),
      testCodecRoundtrip[BsonDateTime](
        "max",
        new BsonDateTime(Long.MaxValue),
        """{"$date": {"$numberLong": "9223372036854775807"}}""",
      ),
    ),
    suite("BsonDecimal128")(
      testCodecRoundtrip[BsonDecimal128](
        "positive zero",
        new BsonDecimal128(Decimal128.POSITIVE_ZERO),
        """{"$numberDecimal": "0"}""",
      ),
      testCodecRoundtrip[BsonDecimal128](
        "negative zero",
        new BsonDecimal128(Decimal128.NEGATIVE_ZERO),
        """{"$numberDecimal": "-0"}""",
      ),
      testCodecRoundtrip[BsonDecimal128](
        "positive infinity",
        new BsonDecimal128(Decimal128.POSITIVE_INFINITY),
        """{"$numberDecimal": "Infinity"}""",
      ),
      testCodecRoundtrip[BsonDecimal128](
        "negative infinity",
        new BsonDecimal128(Decimal128.NEGATIVE_INFINITY),
        """{"$numberDecimal": "-Infinity"}""",
      ),
      testCodecRoundtrip[BsonDecimal128](
        "NaN",
        new BsonDecimal128(Decimal128.NaN),
        """{"$numberDecimal": "NaN"}""",
      ),
      testCodecRoundtrip[BsonDecimal128](
        "negative NaN",
        new BsonDecimal128(Decimal128.NEGATIVE_NaN),
        """{"$numberDecimal": "NaN"}""",
      ),
      testCodecRoundtrip[BsonDecimal128](
        "1, 1",
        new BsonDecimal128(Decimal128.fromIEEE754BIDEncoding(1, 1)),
        """{"$numberDecimal": "1.8446744073709551617E-6157"}""",
      ),
    ),
    suite("BsonDocument")(
      testCodecRoundtrip[BsonDocument]("empty", new BsonDocument(), "{}"),
      testCodecRoundtrip[BsonDocument](
        "BsonDocument(1)",
        new BsonDocument(List(new BsonElement("1", new BsonInt32(1))).asJava),
        """{"1": 1}""",
      ),
      testCodecRoundtrip[BsonDocument](
        "BsonDocument(1, 2, 3)",
        new BsonDocument(
          List(
            new BsonElement("1", new BsonInt32(1)),
            new BsonElement("2", new BsonInt32(2)),
            new BsonElement("3", new BsonInt32(3)),
          ).asJava,
        ),
        """{"1": 1, "2": 2, "3": 3}""",
      ),
    ),
    suite("BsonDouble")(
      testCodecRoundtrip[BsonDouble]("0", new BsonDouble(0), "0.0"),
      testCodecRoundtrip[BsonDouble](
        "min",
        new BsonDouble(Double.MinValue),
        "-1.7976931348623157E308",
      ),
      testCodecRoundtrip[BsonDouble](
        "max",
        new BsonDouble(Double.MaxValue),
        "1.7976931348623157E308",
      ),
    ),
    suite("BsonInt32")(
      testCodecRoundtrip[BsonInt32]("0", new BsonInt32(0), "0"),
      testCodecRoundtrip[BsonInt32]("min", new BsonInt32(Int.MinValue), "-2147483648"),
      testCodecRoundtrip[BsonInt32]("max", new BsonInt32(Int.MaxValue), "2147483647"),
    ),
    suite("BsonInt64")(
      testCodecRoundtrip[BsonInt64]("0", new BsonInt64(0), "0"),
      testCodecRoundtrip[BsonInt64]("min", new BsonInt64(Long.MinValue), "-9223372036854775808"),
      testCodecRoundtrip[BsonInt64]("max", new BsonInt64(Long.MaxValue), "9223372036854775807"),
    ),
    suite("BsonJavaScript")(
      testCodecRoundtrip[BsonJavaScript]("empty", new BsonJavaScript(""), """{"$code": ""}"""),
      testCodecRoundtrip[BsonJavaScript](
        "non-empty",
        new BsonJavaScript("javaScript code"),
        """{"$code": "javaScript code"}""",
      ),
    ),
    suite("BsonJavaScriptWithScope")(
      testCodecRoundtrip[BsonJavaScriptWithScope](
        "empty",
        new BsonJavaScriptWithScope("", new BsonDocument()),
        """{"$code": "", "$scope": {}}""",
      ),
      testCodecRoundtrip[BsonJavaScriptWithScope](
        "non-empty",
        new BsonJavaScriptWithScope(
          "javaScript code",
          new BsonDocument(List(new BsonElement("k", new BsonInt32(1))).asJava),
        ),
        """{"$code": "javaScript code", "$scope": {"k": 1}}""",
      ),
    ),
    suite("BsonMaxKey")(
      testCodecRoundtrip[BsonMaxKey]("maxKey", new BsonMaxKey(), """{"$maxKey": 1}"""),
    ),
    suite("BsonMinKey")(
      testCodecRoundtrip[BsonMinKey]("minKey", new BsonMinKey(), """{"$minKey": 1}"""),
    ),
    suite("BsonNull")(
      testCodecRoundtrip[BsonNull]("null", new BsonNull(), "null"),
    ),
    suite("BsonObjectId")(
      testCodecRoundtrip[BsonObjectId](
        "non-empty",
        new BsonObjectId(new ObjectId("507f1f77bcf86cd799439011")),
        """{"$oid": "507f1f77bcf86cd799439011"}""",
      ),
    ),
    suite("BsonRegularExpression")(
      testCodecRoundtrip[BsonRegularExpression](
        "non-empty",
        new BsonRegularExpression("regex", "options"),
        """{"$regularExpression": {"pattern": "regex", "options": "inoopst"}}""",
      ),
    ),
    suite("BsonString")(
      testCodecRoundtrip[BsonString]("empty", new BsonString(""), """"""""),
      testCodecRoundtrip[BsonString]("non-empty", new BsonString("string"), """"string""""),
    ),
    suite("BsonSymbol")(
      testCodecRoundtrip[BsonSymbol]("empty", new BsonSymbol(""), """{"$symbol": ""}"""),
      testCodecRoundtrip[BsonSymbol](
        "non-empty",
        new BsonSymbol("symbol"),
        """{"$symbol": "symbol"}""",
      ),
    ),
    suite("BsonTimestamp")(
      testCodecRoundtrip[BsonTimestamp](
        "0",
        new BsonTimestamp(),
        """{"$timestamp": {"t": 0, "i": 0}}""",
      ),
      testCodecRoundtrip[BsonTimestamp](
        "min long",
        new BsonTimestamp(Long.MinValue),
        """{"$timestamp": {"t": 2147483648, "i": 0}}""",
      ),
      testCodecRoundtrip[BsonTimestamp](
        "max long",
        new BsonTimestamp(Long.MaxValue),
        """{"$timestamp": {"t": 2147483647, "i": 4294967295}}""",
      ),
      testCodecRoundtrip[BsonTimestamp](
        "min seconds + increment",
        new BsonTimestamp(Int.MinValue, Int.MinValue),
        """{"$timestamp": {"t": 2147483648, "i": 2147483648}}""",
      ),
      testCodecRoundtrip[BsonTimestamp](
        "max seconds + increment",
        new BsonTimestamp(Int.MaxValue, Int.MaxValue),
        """{"$timestamp": {"t": 2147483647, "i": 2147483647}}""",
      ),
    ),
    suite("BsonUndefined")(
      testCodecRoundtrip[BsonUndefined](
        "undefined",
        new BsonUndefined(),
        """{"$undefined": true}""",
      ),
    ),
    suite("BsonValue")(
      testCodecRoundtrip[BsonValue]("BsonBoolean", new BsonBoolean(true), "true"),
    ),
    suite("ObjectId")(
      testCodecRoundtrip[ObjectId](
        "non-empty",
        new ObjectId("507f1f77bcf86cd799439011"),
        """{"$oid": "507f1f77bcf86cd799439011"}""",
      ),
    ),
    suite("Decimal128")(
      testCodecRoundtrip[Decimal128](
        "positive zero",
        Decimal128.POSITIVE_ZERO,
        """{"$numberDecimal": "0"}""",
      ),
      testCodecRoundtrip[Decimal128](
        "negative zero",
        Decimal128.NEGATIVE_ZERO,
        """{"$numberDecimal": "-0"}""",
      ),
      testCodecRoundtrip[Decimal128](
        "positive infinity",
        Decimal128.POSITIVE_INFINITY,
        """{"$numberDecimal": "Infinity"}""",
      ),
      testCodecRoundtrip[Decimal128](
        "negative infinity",
        Decimal128.NEGATIVE_INFINITY,
        """{"$numberDecimal": "-Infinity"}""",
      ),
      testCodecRoundtrip[Decimal128](
        "NaN",
        Decimal128.NaN,
        """{"$numberDecimal": "NaN"}""",
      ),
      testCodecRoundtrip[Decimal128](
        "negative NaN",
        Decimal128.NEGATIVE_NaN,
        """{"$numberDecimal": "NaN"}""",
      ),
      testCodecRoundtrip[Decimal128](
        "1, 1",
        Decimal128.fromIEEE754BIDEncoding(1, 1),
        """{"$numberDecimal": "1.8446744073709551617E-6157"}""",
      ),
    ),
  )
}
