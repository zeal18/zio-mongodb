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

import scala.collection.mutable

import io.github.zeal18.zio.mongodb.bson.BsonBoolean
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.bson.BsonString
import io.github.zeal18.zio.mongodb.bson.collection.mutable.Document
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BsonValue
import org.bson.json.JsonParseException
import zio.ZIO
import zio.test.*

object MutableDocumentSpec extends ZIOSpecDefault {

  val emptyDoc: Document = Document.empty
  val doc: Document      = Document("key" -> "value", "key2" -> "value2", "key3" -> "value3")
  val docMap: Map[String, BsonValue] = doc.toMap

  override def spec = suite("MutableDocumentSpec")(
    suite("Document lookups")(
      test("should be the same as empty documents") {
        assertTrue(emptyDoc == Document())
      },
      test("should support construction via json") {
        ZIO
          .attempt(Document("not Json"))
          .cause
          .map(e => assertTrue(e.failureOption.get.isInstanceOf[JsonParseException]), // scalafix:ok
          )
          .map(_ && assertTrue(Document("{a: 1, b: true}") == Document("a" -> 1, "b" -> true)))
      },
      test("should support get()") {
        assertTrue(doc.get("key").get == BsonString("value")) &&
        assertTrue(doc.get("nonexistent").isEmpty)
      },
      test("should support direct lookup") {
        for {
          // When the key doesn't exist
          nonexistent <- ZIO
            .attempt(doc("nonexistent"))
            .cause
            .map(e =>
              assertTrue(e.failureOption.get.isInstanceOf[NoSuchElementException]), // scalafix:ok
            )
          // When the key exists but the type doesn't match"
          wrongtype <- ZIO
            .attempt(doc[BsonArray]("key"))
            .cause
            .map(e =>
              assertTrue(e.failureOption.get.isInstanceOf[NoSuchElementException]), // scalafix:ok
            )
        } yield nonexistent && wrongtype &&
          assertTrue(doc("key") == BsonString("value")) &&
          assertTrue(doc[BsonString]("key") == BsonString("value"))
      },
      test("should support getOrElse") {
        assertTrue(doc.getOrElse("key", BsonBoolean(false)) == BsonString("value")) &&
        assertTrue(doc.getOrElse("nonexistent", BsonBoolean(false)) == BsonBoolean(false))
      },
      test("should support contains") {
        assertTrue(doc.contains("key")) &&
        assertTrue(!doc.contains("nonexistent"))
      },
    ),
    suite("Document additions and updates")(
      test("should support simple additions") {
        val doc1: Document = emptyDoc + ("key" -> "value")
        val doc2: Document = doc1 + ("key2"    -> "value2")

        assertTrue(doc1 != emptyDoc) &&
        assertTrue(doc1 == Document("key" -> "value")) &&
        assertTrue(doc1 != doc2) &&
        assertTrue(doc2 == Document("key" -> "value", "key2" -> "value2"))
      },
      test("should support multiple additions") {
        val doc1: Document = emptyDoc + ("key" -> "value", "key2" -> "value2", "key3" -> "value3")
        val doc2: Document = doc1 + ("key4"    -> "value4")

        assertTrue(doc1 != emptyDoc) &&
        assertTrue(doc1 == Document("key" -> "value", "key2" -> "value2", "key3" -> "value3")) &&
        assertTrue(doc2 != doc1) &&
        assertTrue(
          doc2 == Document(
            "key"  -> "value",
            "key2" -> "value2",
            "key3" -> "value3",
            "key4" -> "value4",
          ),
        )
      },
      test("should support addition of a traversable") {
        val doc1: Document =
          emptyDoc ++ Set("key" -> BsonString("value"), "key2" -> BsonString("value2"))
        val doc2: Document = doc1 ++ List("key3" -> BsonString("value3"))

        assertTrue(doc1 != emptyDoc) &&
        assertTrue(
          doc1 == Document("key" -> BsonString("value"), "key2" -> BsonString("value2")),
        ) &&
        assertTrue(doc1 != doc2) &&
        assertTrue(
          doc2 == Document(
            "key"  -> BsonString("value"),
            "key2" -> BsonString("value2"),
            "key3" -> BsonString("value3"),
          ),
        )
      },
      test("should support updated") {
        val doc1: Document = emptyDoc updated ("key", "value")
        val doc2: Document = doc1 updated ("key2" -> "value2")

        assertTrue(doc1 != emptyDoc) &&
        assertTrue(doc1 == Document("key" -> "value")) &&
        assertTrue(doc2 != doc1) &&
        assertTrue(doc2 == Document("key" -> "value", "key2" -> "value2"))
      },
    ),
    suite("Document removals")(
      test("should support subtractions") {
        val doc1: Document = doc - "nonexistent key"
        val doc2: Document = doc - "key"

        assertTrue(doc1 == doc) &&
        assertTrue(doc1 != doc2) &&
        assertTrue(doc2 == Document("key2" -> "value2", "key3" -> "value3"))
      },
      test("should support multiple subtractions") {
        val doc1: Document = doc - ("key", "key2")

        assertTrue(doc1 != doc) &&
        assertTrue(doc1 == Document("key3" -> "value3"))
      },
      test("should support subtraction of a traversable") {
        val doc1: Document = doc -- Set("key", "key2")
        val doc2: Document = doc -- List("key3")

        assertTrue(doc1 != doc) &&
        assertTrue(doc1 == Document("key3" -> "value3")) &&
        assertTrue(doc2 != doc1) &&
        assertTrue(doc2 == Document("key" -> "value", "key2" -> "value2"))
      },
    ),
    suite("Document subcollections")(
      test("should provide keys in the order set") {
        val doc1: Document = doc + ("aNewKey" -> "1")

        assertTrue(doc.keys == Set("key", "key2", "key3")) &&
        assertTrue(doc1.keys == Set("key", "key2", "key3", "aNewKey"))
      },
      test("should provide a keySet in the order set") {
        val doc1: Document = doc + ("aNewKey" -> "1")

        assertTrue(doc.keySet == Set("key", "key2", "key3")) &&
        assertTrue(doc1.keySet == Set("key", "key2", "key3", "aNewKey"))
      },
      test("should provide a keysIterator in the order set") {
        val doc1: Document = doc + ("aNewKey" -> "1")

        assertTrue(doc.keysIterator.toSet == Set("key", "key2", "key3")) &&
        assertTrue(doc1.keysIterator.toSet == Set("key", "key2", "key3", "aNewKey"))
      },
      test("should provide values in the order set") {
        val doc1: Document = doc + ("aNewKey" -> 1)
        assertTrue(
          doc.values.toSet == Set[BsonValue](
            BsonString("value"),
            BsonString("value2"),
            BsonString("value3"),
          ),
        ) &&
        assertTrue(
          doc1.values.toSet == Set[BsonValue](
            BsonString("value"),
            BsonString("value2"),
            BsonString("value3"),
            BsonInt32(1),
          ),
        )
      },
      test("should provide a valueSet in the order set") {
        val doc1: Document = doc + ("aNewKey" -> 1)

        assertTrue(
          doc.valuesIterator.toSet == Set[BsonValue](
            BsonString("value"),
            BsonString("value2"),
            BsonString("value3"),
          ),
        ) &&
        assertTrue(
          doc1.valuesIterator.toSet == Set[BsonValue](
            BsonString("value"),
            BsonString("value2"),
            BsonString("value3"),
            BsonInt32(1),
          ),
        )
      },
    ),
    suite("Document transformations")(
      test("should be filterable by keys") {
        val doc1: Document = doc.filterKeys(k => k == "key")

        assertTrue(doc1 == Document("key" -> "value"))
      },
    ),
    suite("Traversable helpers")(
      test("should work as expected") {
        val map = mutable.Map[String, BsonValue]()
        doc foreach (kv => map += kv)

        assertTrue(doc.toMap[String, BsonValue] == map.toMap[String, BsonValue])
      },
      test("should be able to create new Documents from iterable") {
        val doc1 = Document(docMap)
        assertTrue(doc == doc1)
      },
      test("should be mappable thanks to CanBuildFrom") {
        val doc1: Document = docMap.map(kv => kv).to(Document)

        assertTrue(Document.empty.map(kv => kv) == Document.empty) &&
        assertTrue(doc1 == doc)
      },
      test("should return a BsonDocument") {
        val bsonDoc: BsonDocument = doc.toBsonDocument

        assertTrue(doc.underlying == bsonDoc)
      },
      test("should return a Json representation") {
        assertTrue(doc.toJson() == """{"key": "value", "key2": "value2", "key3": "value3"}""")
      },
    ),
    suite("Documents")(
      test("support Traversable like builders") {
        val doc1 = doc.filter(kv => kv._1 == "key")

        assertTrue(doc1 != doc) &&
        assertTrue(doc1 == Document("key" -> "value"))
      },
    ),
    suite("Mutable Documents")(
      test("should have maplike mutability") {
        val doc1 = Document.empty
        val _    = doc1 += (("x", BsonString("x")))

        assertTrue(doc1 == Document("x" -> BsonString("x")))
      },
      test("should support inline additions") {
        val doc1: Document =
          Document.empty += ("key" -> BsonString("value"), "key2" -> BsonString("value2"))

        assertTrue(
          doc1 == Document("key" -> BsonString("value"), "key2" -> BsonString("value2")),
        )
      },
      test("should support multiple inline additions") {
        val doc1: Document =
          Document.empty += ("key" -> BsonString("value"), "key2" -> BsonString("value2"))
        val doc2: Document = doc1 += ("key3" -> BsonString("value3"))

        assertTrue(doc1 == doc2) &&
        assertTrue(
          doc2 == Document(
            "key"  -> BsonString("value"),
            "key2" -> BsonString("value2"),
            "key3" -> BsonString("value3"),
          ),
        )
      },
      test("should support inline addition of a traversable") {
        val doc1: Document =
          Document.empty ++= Set("key" -> BsonString("value"), "key2" -> BsonString("value2"))

        assertTrue(
          doc1 == Document("key" -> BsonString("value"), "key2" -> BsonString("value2")),
        )
      },
      test("should support multiple inline addition of a traversable") {
        val doc1: Document =
          Document.empty ++= Set("key" -> BsonString("value"), "key2" -> BsonString("value2"))
        val doc2: Document = doc1 ++= List("key3" -> BsonString("value3"))

        assertTrue(doc1 == doc2) &&
        assertTrue(
          doc2 == Document(
            "key"  -> BsonString("value"),
            "key2" -> BsonString("value2"),
            "key3" -> BsonString("value3"),
          ),
        )
      },
      test("should support put") {
        val doc1: Document = Document.empty

        assertTrue(doc1.put("key", BsonString("value")).isEmpty) &&
        assertTrue(doc1 == Document("key" -> BsonString("value"))) &&
        assertTrue(doc1.put("key", BsonString("newValue")).get == BsonString("value")) &&
        assertTrue(doc1 == Document("key" -> BsonString("newValue")))
      },
      test("should support getOrElseUpdate") {
        val doc1: Document = Document.empty

        assertTrue(doc1.getOrElseUpdate("key", BsonString("value")) == BsonString("value")) &&
        assertTrue(doc1 == Document("key" -> BsonString("value"))) &&
        assertTrue(doc1.getOrElseUpdate("key", BsonString("newValue")) == BsonString("value")) &&
        assertTrue(doc1 == Document("key" -> BsonString("value")))
      },
      test("should support inline update") {
        val doc1: Document = Document.empty
        doc1.update("key", BsonString("value"))

        assertTrue(doc1 == Document("key" -> BsonString("value")))
      },
      test("should support multiple inline updates") {
        val doc1: Document = Document.empty
        doc1.update("key", BsonString("value"))
        doc1.update("key2", BsonString("value2"))

        assertTrue(doc1 == Document("key" -> BsonString("value"), "key2" -> BsonString("value2")))
      },
    ),
    suite("Document removals")(
      test("should support inline subtraction") {
        val doc1: Document = doc.copy() -= "nonexistent key"

        assertTrue(doc1 == doc)
      },
      test("should support multiple inline subtractions") {
        val doc1: Document = doc.copy() -= "nonexistent key"
        val doc2: Document = doc1 -= "key"

        assertTrue(doc2 == Document("key2" -> BsonString("value2"), "key3" -> BsonString("value3")))
      },
      test("should support multiple inline subtractions") {
        val doc1: Document = doc.copy() -= ("key", "key2")

        assertTrue(doc != doc1) &&
        assertTrue(doc1 == Document("key3" -> BsonString("value3")))
      },
      test("should support inline subtraction of a traversable") {
        val doc1: Document = doc.copy() --= Set("key", "key2")

        assertTrue(doc != doc1) &&
        assertTrue(doc1 == Document("key3" -> BsonString("value3")))
      },
      test("should support multiple inline subtractions of a traversable") {
        val doc1: Document = doc.copy() --= Set("key", "key2")
        val doc2: Document = doc1 --= List("key3")

        assertTrue(doc1 == doc2) &&
        assertTrue(doc2 == Document())
      },
      test("should support remove") {
        val doc1: Document = Document("key" -> BsonString("value"))

        assertTrue(doc1.remove("key").get == BsonString("value")) &&
        assertTrue(doc1 == Document()) &&
        assertTrue(doc1.remove("noKey").isEmpty) &&
        assertTrue(doc1 == Document())
      },
      test("should support retain") {
        val doc1: Document = Document("key" -> BsonString("value"), "key2" -> BsonString("value2"))

        val _ = doc1.retain((k, _) => k == "key")
        assertTrue(doc1 == Document("key" -> BsonString("value")))
      },
      test("should support clear") {
        val doc1: Document = Document("key" -> BsonString("value"), "key2" -> BsonString("value2"))

        doc1.clear()
        assertTrue(doc1 == Document())
      },
      test("should support transform") {
        val doc1: Document = Document("key" -> BsonString("value"), "key2" -> BsonString("value2"))

        val _ = doc1.transform((_, v) => BsonString(v.asString().getValue.toUpperCase))
        assertTrue(doc1 == Document("key" -> BsonString("VALUE"), "key2" -> BsonString("VALUE2")))
      },
    ),
  )
}
