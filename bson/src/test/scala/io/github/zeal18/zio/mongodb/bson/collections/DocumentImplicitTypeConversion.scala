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

package io.github.zeal18.zio.mongodb.bson.collections

import io.github.zeal18.zio.mongodb.bson.*
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.bson.collection.mutable
import zio.test.*

object DocumentImplicitTypeConversionSpec extends ZIOSpecDefault {
  val emptyDoc: Document = Document.empty

  override def spec = suite("DocumentImplicitTypeConversionSpec")(
    suite("Document additions and updates")(
      test("should support simple additions") {
        val doc1: Document = Document() + ("key" -> "value")

        val doc2: Document = doc1 + ("key2" -> 2)

        assertTrue(doc1 == Document("key" -> BsonString("value"))) &&
        assertTrue(doc2 == Document("key" -> BsonString("value"), "key2" -> BsonInt32(2)))
      },
      test("should support multiple additions") {
        val doc1: Document =
          emptyDoc + ("key" -> "value", "key2" -> 2, "key3" -> true, "key4" -> None)

        assertTrue(
          doc1 == Document(
            "key"  -> BsonString("value"),
            "key2" -> BsonInt32(2),
            "key3" -> BsonBoolean(true),
            "key4" -> BsonNull(),
          ),
        )
      },
      test("should support addition of a traversable") {
        val doc1: Document =
          emptyDoc ++ Document("key" -> "value", "key2" -> 2, "key3" -> true, "key4" -> None)
        assertTrue(
          doc1 == Document(
            "key"  -> BsonString("value"),
            "key2" -> BsonInt32(2),
            "key3" -> BsonBoolean(true),
            "key4" -> BsonNull(),
          ),
        )
      },
      test("should support updated") {
        val doc1: Document = emptyDoc.updated("key", "value")

        assertTrue(doc1 != emptyDoc) &&
        assertTrue(doc1 == Document("key" -> BsonString("value")))
      },
      test("should be creatable from mixed types") {
        val doc1: Document = Document(
          "a" -> "string",
          "b" -> true,
          "c" -> List("a", "b", "c"),
          "d" -> Document("a" -> "string", "b" -> true, "c" -> List("a", "b", "c")),
        )

        val doc2: mutable.Document = mutable.Document(
          "a" -> "string",
          "b" -> true,
          "c" -> List("a", "b", "c"),
          "d" ->
            mutable.Document("a" -> "string", "b" -> true, "c" -> List("a", "b", "c")),
        )

        val bsonDoc1 = doc1.toBsonDocument
        val bsonDoc2 = doc2.toBsonDocument

        assertTrue(bsonDoc1 == bsonDoc2)
      },
    ),
  )
}
