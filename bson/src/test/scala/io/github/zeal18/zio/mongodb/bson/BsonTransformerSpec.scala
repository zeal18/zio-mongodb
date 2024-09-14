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

import io.github.zeal18.zio.mongodb.bson.collection.immutable
import io.github.zeal18.zio.mongodb.bson.collection.mutable
import zio.test.*

import java.util.Date

object BsonTransformerSpec extends ZIOSpecDefault {
  override def spec = suite("BsonTransformerSpec")(
    suite("The BsonTransformer companion")(
      test("should not transform BsonValues") {
        assertTrue(transform(BsonString("abc")) == BsonString("abc"))
      },
      test("should transform Binary") {
        assertTrue(transform(Array[Byte](128.toByte)) == BsonBinary(Array[Byte](128.toByte)))
      },
      test("should transform BigDecmial") {
        assertTrue(transform(BigDecimal(100)) == BsonDecimal128(100))
      },
      test("should transform Boolean") {
        assertTrue(transform(true) == BsonBoolean(true))
      },
      test("should transform DateTime") {
        assertTrue(transform(new Date(100)) == BsonDateTime(100))
      },
      test("should transform Decimal128") {
        assertTrue(transform(new Decimal128(100)) == BsonDecimal128(100))
      },
      test("should transform Double") {
        assertTrue(transform(2.0) == BsonDouble(2.0))
      },
      test("should transform ImmutableDocument") {
        assertTrue(
          transform(immutable.Document("a" -> 1, "b" -> "two", "c" -> false)) ==
            BsonDocument("a" -> 1, "b" -> "two", "c" -> false),
        )
      },
      test("should transform Int") {
        assertTrue(transform(1) == BsonInt32(1))
      },
      test("should transform KeyValuePairs[T]") {
        assertTrue(
          transform(Seq("a" -> "a", "b" -> "b", "c" -> "c")) ==
            BsonDocument("a" -> "a", "b" -> "b", "c" -> "c"),
        )
      },
      test("should transform Long") {
        assertTrue(transform(1L) == BsonInt64(1))
      },
      test("should transform MutableDocument") {
        assertTrue(
          transform(mutable.Document("a" -> 1, "b" -> "two", "c" -> false)) ==
            BsonDocument("a" -> 1, "b" -> "two", "c" -> false),
        )
      },
      test("should transform None") {
        assertTrue(transform(None) == BsonNull())
      },
      test("should transform ObjectId") {
        val objectId = new ObjectId()
        assertTrue(transform(objectId) == BsonObjectId(objectId))
      },
      test("should transform Option[T]") {
        assertTrue(transform(Some(1)) == new BsonInt32(1))
      },
      test("should transform Regex") {
        assertTrue(transform("/.*/".r) == BsonRegularExpression("/.*/"))
      },
      test("should transform Seq[T]") {
        assertTrue(transform(Seq("a", "b", "c")) == BsonArray("a", "b", "c"))
      },
      test("should transform String") {
        assertTrue(transform("abc") == BsonString("abc"))
      },
      // test("should thrown a runtime exception when no transformer available") {
      //   assertTrue("transform(BigInt(12))" shouldNot compile
      // },
    ),
  )

  implicit def transform[T](v: T)(implicit transformer: BsonTransformer[T]): BsonValue =
    transformer(v)
}
