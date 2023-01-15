/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zeal18.zio.mongodb.bson

import java.util.Date

import scala.jdk.CollectionConverters.*

import zio.test.*

object BsonValueSpec extends ZIOSpecDefault {
  override def spec = suite("BsonValueSpec")(
    test("BsonArray companion should create a BsonArray") {

      val values: List[BsonNumber] = List(BsonInt32(1), BsonInt64(2), new BsonDouble(3.0))
      val bsonArray                = BsonArray.fromIterable(values)
      val expected                 = new BsonArray(values.asJava)

      val implicitBsonArray = BsonArray(1, 2L, 3.0)

      assertTrue(BsonArray() == new BsonArray()) &&
      assertTrue(bsonArray == expected) &&
      assertTrue(implicitBsonArray == expected)
    },
    test("BsonBinary companion should create a BsonBinary") {
      val byteArray = Array[Byte](80.toByte, 5, 4, 3, 2, 1)

      assertTrue(BsonBinary(byteArray) == new BsonBinary(byteArray))
    },
    test("BsonBoolean companion should create a BsonBoolean") {
      assertTrue(BsonBoolean(false) == new BsonBoolean(false)) &&
      assertTrue(BsonBoolean(true) == new BsonBoolean(true))
    },
    test("BsonDateTime companion should create a BsonDateTime") {
      val date = new Date()

      assertTrue(BsonDateTime(date) == new BsonDateTime(date.getTime)) &&
      assertTrue(BsonDateTime(1000) == new BsonDateTime(1000))
    },
    test("BsonDecimal128 companion should create a BsonDecimal128") {
      val expected = new BsonDecimal128(new Decimal128(100))

      assertTrue(BsonDecimal128(100) == expected) &&
      assertTrue(BsonDecimal128("100") == expected) &&
      assertTrue(BsonDecimal128(BigDecimal(100)) == expected) &&
      assertTrue(BsonDecimal128(new Decimal128(100)) == expected)
    },
    test("BsonDocument companion should create a BsonDocument") {
      val expected = new BsonDocument("a", BsonInt32(1))
      expected.put("b", BsonDouble(2.0))

      assertTrue(BsonDocument() == new BsonDocument()) &&
      assertTrue(BsonDocument("a" -> 1, "b" -> 2.0) == expected) &&
      assertTrue(BsonDocument(Seq(("a", BsonInt32(1)), ("b", BsonDouble(2.0)))) == expected) &&
      assertTrue(BsonDocument("{a: 1, b: 2.0}") == expected)
    },
    test("BsonDouble companion should create a BsonDouble") {
      assertTrue(BsonDouble(2.0) == new BsonDouble(2.0))
    },
    test("BsonInt32 companion should create a BsonInt32") {
      assertTrue(BsonInt32(1) == new BsonInt32(1))
    },
    test("BsonInt64 companion should create a BsonInt64") {
      assertTrue(BsonInt64(1) == new BsonInt64(1))
    },
    test("BsonJavaScript companion should create a BsonJavaScript") {
      assertTrue(BsonJavaScript("function(){}") == new BsonJavaScript("function(){}"))
    },
    test("BsonJavaScriptWithScope companion should create a BsonJavaScriptWithScope") {
      val function = "function(){}"
      val scope    = new BsonDocument("a", new BsonInt32(1))
      val expected = new BsonJavaScriptWithScope(function, scope)

      assertTrue(BsonJavaScriptWithScope(function, scope) == expected) &&
      assertTrue(BsonJavaScriptWithScope(function, "a" -> 1) == expected) &&
      assertTrue(BsonJavaScriptWithScope(function, Document("a" -> 1)) == expected)
    },
    test("BsonMaxKey companion should create a BsonMaxKey") {
      assertTrue(BsonMaxKey() == new BsonMaxKey())
    },
    test("BsonMinKey companion should create a BsonMinKey") {
      assertTrue(BsonMinKey() == new BsonMinKey())
    },
    test("BsonNull companion should create a BsonNull") {
      assertTrue(BsonNull() == new BsonNull())
    },
    test("BsonNumber companion should create a BsonNumber") {
      assertTrue(BsonNumber(1) == BsonInt32(1)) &&
      assertTrue(BsonNumber(1L) == BsonInt64(1)) &&
      assertTrue(BsonNumber(1.0) == BsonDouble(1.0))
    },
    test("BsonObjectId companion should create a BsonObjectId") {
      val bsonObjectId = BsonObjectId()
      val objectId     = bsonObjectId.getValue
      val hexString    = objectId.toHexString
      val expected     = new BsonObjectId(bsonObjectId.getValue)

      assertTrue(bsonObjectId == expected) &&
      assertTrue(BsonObjectId(hexString) == expected) &&
      assertTrue(BsonObjectId(objectId) == expected)
    },
    test("BsonRegularExpression companion should create a BsonRegularExpression") {
      assertTrue(BsonRegularExpression("/(.*)/") == new BsonRegularExpression("/(.*)/")) &&
      assertTrue(BsonRegularExpression("/(.*)/".r) == new BsonRegularExpression("/(.*)/")) &&
      assertTrue(BsonRegularExpression("/(.*)/", "?i") == new BsonRegularExpression("/(.*)/", "?i"))
    },
    test("BsonString companion should create a BsonString") {
      assertTrue(BsonString("aBc") == new BsonString("aBc"))
    },
    test("BsonSymbol companion should create a BsonSymbol") {
      assertTrue(BsonSymbol(Symbol("sym")) == new BsonSymbol("sym"))
    },
    test("BsonTimestamp companion should create a BsonTimestamp") {
      assertTrue(BsonTimestamp() == new BsonTimestamp(0, 0)) &&
      assertTrue(BsonTimestamp(10, 1) == new BsonTimestamp(10, 1))
    },
    test("BsonUndefined companion should create a BsonUndefined") {
      assertTrue(BsonUndefined() == new BsonUndefined())
    },
  )
}
