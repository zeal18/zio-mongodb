package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoCollectionTest
import org.bson.BsonDocument
import zio.Chunk
import zio.test.*

object AggregationsItSpec extends ZIOSpecDefault {
  override def spec =
    suite("AggregationsItSpec")(
      suite("match")(
        test("query") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
              """{"_id": ObjectId("512bc962e835e68f199c8687"), "author": "dave", "score": 85, "views": 521 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b259"), "author": "annT", "score": 60, "views": 50 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25b"), "author": "ty", "score": 95, "views": 1000 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
              """{"_id": ObjectId("512bc962e835e68f199c8687"), "author": "dave", "score": 85, "views": 521 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result1 <- collection
                .aggregate(aggregates.`match`(filters.eq("author", "dave")))
                .runToChunk
            } yield assertTrue(result1 == expected)
          }
        },
        test("expression") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
              """{"_id": ObjectId("512bc962e835e68f199c8687"), "author": "dave", "score": 85, "views": 521 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b259"), "author": "annT", "score": 60, "views": 50 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25b"), "author": "ty", "score": 95, "views": 1000 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25b"), "author": "ty", "score": 95, "views": 1000 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result1 <- collection
                .aggregate(
                  aggregates.`match`(
                    expressions.gte(expressions.fieldPath("$score"), expressions.const(90)),
                  ),
                )
                .runToChunk
            } yield assertTrue(result1 == expected)
          }
        },
      ),
      test("limit") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
            """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected = Chunk(
            """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
            """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
          ).map(d => Document(BsonDocument.parse(d)))

          for {
            _ <- collection.insertMany(documents)

            result1 <- collection.aggregate(aggregates.limit(2)).runToChunk
          } yield assertTrue(result1 == expected)
        }
      },
      test("count") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
            """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected1 = Chunk(Document(BsonDocument.parse("""{"count": 4}""")))
          val expected2 = Chunk(Document(BsonDocument.parse("""{"authors_count": 4}""")))

          for {
            _ <- collection.insertMany(documents)

            result1 <- collection.aggregate(aggregates.count()).runToChunk
            result2 <- collection.aggregate(aggregates.count("authors_count")).runToChunk
          } yield assertTrue(result1 == expected1 && result2 == expected2)
        }
      },
    ).provideLayerShared(MongoClientTest.live().orDie)
}
