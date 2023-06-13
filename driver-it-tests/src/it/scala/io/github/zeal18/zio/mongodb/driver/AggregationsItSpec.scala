package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.aggregates.Facet
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
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
      test("bucket") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{ "_id" : 1, "last_name" : "Bernard", "first_name" : "Emil", "year_born" : 1868, "year_died" : 1941, "nationality" : "France" }""",
            """{ "_id" : 2, "last_name" : "Rippl-Ronai", "first_name" : "Joszef", "year_born" : 1861, "year_died" : 1927, "nationality" : "Hungary" }""",
            """{ "_id" : 3, "last_name" : "Ostroumova", "first_name" : "Anna", "year_born" : 1871, "year_died" : 1955, "nationality" : "Russia" }""",
            """{ "_id" : 4, "last_name" : "Van Gogh", "first_name" : "Vincent", "year_born" : 1853, "year_died" : 1890, "nationality" : "Holland" }""",
            """{ "_id" : 5, "last_name" : "Maurer", "first_name" : "Alfred", "year_born" : 1868, "year_died" : 1932, "nationality" : "USA" }""",
            """{ "_id" : 6, "last_name" : "Munch", "first_name" : "Edvard", "year_born" : 1863, "year_died" : 1944, "nationality" : "Norway" }""",
            """{ "_id" : 7, "last_name" : "Redon", "first_name" : "Odilon", "year_born" : 1840, "year_died" : 1916, "nationality" : "France" }""",
            """{ "_id" : 8, "last_name" : "Diriks", "first_name" : "Edvard", "year_born" : 1855, "year_died" : 1930, "nationality" : "Norway" }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected =
            Chunk(
              Document(
                BsonDocument.parse(
                  """|{ "_id" : 1860, "count" : 4, "artists" : [
                     |  { "name" : "Emil Bernard", "year_born" : 1868 },
                     |  { "name" : "Joszef Rippl-Ronai", "year_born" : 1861 },
                     |  { "name" : "Alfred Maurer", "year_born" : 1868 },
                     |  { "name" : "Edvard Munch", "year_born" : 1863 }
                     |]}""".stripMargin,
                ),
              ),
            )

          for {
            _ <- collection.insertMany(documents)

            result <- collection
              .aggregate(
                aggregates.bucket(
                  groupBy = expressions.fieldPath("$year_born"),
                  boundaries = Seq(1840, 1850, 1860, 1870, 1880),
                  default = "Other",
                  output = Map(
                    "count" -> accumulators.sum(expressions.const(1)),
                    "artists" -> accumulators.push(
                      expressions.obj(
                        "name" -> expressions.concat(
                          expressions.fieldPath("$first_name"),
                          expressions.const(" "),
                          expressions.fieldPath("$last_name"),
                        ),
                        "year_born" -> expressions.fieldPath("$year_born"),
                      ),
                    ),
                  ),
                ),
                aggregates.`match`(filters.gt("count", 3)),
              )
              .runToChunk
          } yield assertTrue(result == expected)
        }
      },
      test("bucketAuto") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{ "_id" : 1, "title" : "The Pillars of Society", "artist" : "Grosz", "year" : 1926, "price" : NumberDecimal("199.99"), "dimensions" : { "height" : 39, "width" : 21, "units" : "in" } }""",
            """{ "_id" : 2, "title" : "Melancholy III", "artist" : "Munch", "year" : 1902, "price" : NumberDecimal("280.00"), "dimensions" : { "height" : 49, "width" : 32, "units" : "in" } }""",
            """{ "_id" : 3, "title" : "Dancer", "artist" : "Miro", "year" : 1925, "price" : NumberDecimal("76.04"), "dimensions" : { "height" : 25, "width" : 20, "units" : "in" } }""",
            """{ "_id" : 4, "title" : "The Great Wave off Kanagawa", "artist" : "Hokusai", "price" : NumberDecimal("167.30"), "dimensions" : { "height" : 24, "width" : 36, "units" : "in" } }""",
            """{ "_id" : 5, "title" : "The Persistence of Memory", "artist" : "Dali", "year" : 1931, "price" : NumberDecimal("483.00"), "dimensions" : { "height" : 20, "width" : 24, "units" : "in" } }""",
            """{ "_id" : 6, "title" : "Composition VII", "artist" : "Kandinsky", "year" : 1913, "price" : NumberDecimal("385.00"), "dimensions" : { "height" : 30, "width" : 46, "units" : "in" } }""",
            """{ "_id" : 7, "title" : "The Scream", "artist" : "Munch", "price" : NumberDecimal("159.00"), "dimensions" : { "height" : 24, "width" : 18, "units" : "in" } }""",
            """{ "_id" : 8, "title" : "Blue Flower", "artist" : "O'Keefe", "year" : 1918, "price" : NumberDecimal("118.42"), "dimensions" : { "height" : 24, "width" : 20, "units" : "in" } }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected =
            Chunk(
              """|{
                 |  "_id" : {
                 |    "min" : NumberDecimal("76.04"),
                 |    "max" : NumberDecimal("159.00")
                 |  },
                 |  "count" : 2
                 |}""".stripMargin,
              """|{
                 |  "_id" : {
                 |    "min" : NumberDecimal("159.00"),
                 |    "max" : NumberDecimal("199.99")
                 |  },
                 |  "count" : 2
                 |}""".stripMargin,
              """|{
                 |  "_id" : {
                 |    "min" : NumberDecimal("199.99"),
                 |    "max" : NumberDecimal("385.00")
                 |  },
                 |  "count" : 2
                 |}""".stripMargin,
              """|{
                 |  "_id" : {
                 |    "min" : NumberDecimal("385.00"),
                 |    "max" : NumberDecimal("483.00")
                 |  },
                 |  "count" : 2
                 |}""".stripMargin,
            ).map(d => Document(BsonDocument.parse(d)))

          for {
            _ <- collection.insertMany(documents)

            result <- collection
              .aggregate(
                aggregates.bucketAuto(
                  groupBy = expressions.fieldPath("$price"),
                  buckets = 4,
                ),
              )
              .runToChunk
          } yield assertTrue(result == expected)
        }
      },
      test("sortByCount") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{ "_id" : 1, "title" : "The Pillars of Society", "artist" : "Grosz", "year" : 1926, "tags" : [ "painting", "satire", "Expressionism", "caricature" ] }""",
            """{ "_id" : 2, "title" : "Melancholy III", "artist" : "Munch", "year" : 1902, "tags" : [ "woodcut", "Expressionism" ] }""",
            """{ "_id" : 3, "title" : "Dancer", "artist" : "Miro", "year" : 1925, "tags" : [ "oil", "Surrealism", "painting" ] }""",
            """{ "_id" : 4, "title" : "The Great Wave off Kanagawa", "artist" : "Hokusai", "tags" : [ "woodblock", "ukiyo-e" ] }""",
            """{ "_id" : 5, "title" : "The Persistence of Memory", "artist" : "Dali", "year" : 1931, "tags" : [ "Surrealism", "painting", "oil" ] }""",
            """{ "_id" : 6, "title" : "Composition VII", "artist" : "Kandinsky", "year" : 1913, "tags" : [ "oil", "painting", "abstract" ] }""",
            """{ "_id" : 7, "title" : "The Scream", "artist" : "Munch", "year" : 1893, "tags" : [ "Expressionism", "painting", "oil" ] }""",
            """{ "_id" : 8, "title" : "Blue Flower", "artist" : "O'Keefe", "year" : 1918, "tags" : [ "abstract", "painting" ] }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected =
            Chunk(
              """{ "_id" : "painting", "count" : 6 }""",
              """{ "_id" : "oil", "count" : 4 }""",
              """{ "_id" : "Expressionism", "count" : 3 }""",
              """{ "_id" : "Surrealism", "count" : 2 }""",
              """{ "_id" : "abstract", "count" : 2 }""",
              """{ "_id" : "woodcut", "count" : 1 }""",
              """{ "_id" : "satire", "count" : 1 }""",
              """{ "_id" : "caricature", "count" : 1 }""",
              """{ "_id" : "woodblock", "count" : 1 }""",
              """{ "_id" : "ukiyo-e", "count" : 1 }""",
            ).map(d => Document(BsonDocument.parse(d)))

          for {
            _ <- collection.insertMany(documents)

            result <- collection
              .aggregate(
                aggregates.unwind("$tags"),
                aggregates.sortByCount(expressions.fieldPath("$tags")),
              )
              .runToChunk
          } yield assertTrue(result.toSet == expected.toSet, result.take(3) == expected.take(3))
        }
      },
      test("facet") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{ "_id" : 1, "title" : "The Pillars of Society", "artist" : "Grosz", "year" : 1926, "price" : NumberDecimal("199.99"), "tags" : [ "painting", "satire", "Expressionism", "caricature" ] }""",
            """{ "_id" : 2, "title" : "Melancholy III", "artist" : "Munch", "year" : 1902, "price" : NumberDecimal("280.00"), "tags" : [ "woodcut", "Expressionism" ] }""",
            """{ "_id" : 3, "title" : "Dancer", "artist" : "Miro", "year" : 1925, "price" : NumberDecimal("76.04"), "tags" : [ "oil", "Surrealism", "painting" ] }""",
            """{ "_id" : 4, "title" : "The Great Wave off Kanagawa", "artist" : "Hokusai", "price" : NumberDecimal("167.30"), "tags" : [ "woodblock", "ukiyo-e" ] }""",
            """{ "_id" : 5, "title" : "The Persistence of Memory", "artist" : "Dali", "year" : 1931, "price" : NumberDecimal("483.00"), "tags" : [ "Surrealism", "painting", "oil" ] }""",
            """{ "_id" : 6, "title" : "Composition VII", "artist" : "Kandinsky", "year" : 1913, "price" : NumberDecimal("385.00"), "tags" : [ "oil", "painting", "abstract" ] }""",
            """{ "_id" : 7, "title" : "The Scream", "artist" : "Munch", "year" : 1893, "tags" : [ "Expressionism", "painting", "oil" ] }""",
            """{ "_id" : 8, "title" : "Blue Flower", "artist" : "O'Keefe", "year" : 1918, "price" : NumberDecimal("118.42"), "tags" : [ "abstract", "painting" ] }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected = Chunk(
            Document(
              BsonDocument.parse(
                """|{
                   |  "categorizedByYears(Auto)" : [
                   |    { "_id" : { "min" : null, "max" : 1902 }, "count" : 2 },
                   |    { "_id" : { "min" : 1902, "max" : 1918 }, "count" : 2 },
                   |    { "_id" : { "min" : 1918, "max" : 1926 }, "count" : 2 },
                   |    { "_id" : { "min" : 1926, "max" : 1931 }, "count" : 2 }
                   |  ],
                   |  "categorizedByPrice" : [
                   |    {
                   |      "_id" : 0,
                   |      "count" : 2,
                   |      "titles" : [
                   |        "Dancer",
                   |        "Blue Flower"
                   |      ]
                   |    },
                   |    {
                   |      "_id" : 150,
                   |      "count" : 2,
                   |      "titles" : [
                   |        "The Pillars of Society",
                   |        "The Great Wave off Kanagawa"
                   |      ]
                   |    },
                   |    {
                   |      "_id" : 200,
                   |      "count" : 1,
                   |      "titles" : [
                   |        "Melancholy III"
                   |      ]
                   |    },
                   |    {
                   |      "_id" : 300,
                   |      "count" : 1,
                   |      "titles" : [
                   |        "Composition VII"
                   |      ]
                   |    },
                   |    {
                   |      "_id" : "Other",
                   |      "count" : 1,
                   |      "titles" : [
                   |        "The Persistence of Memory"
                   |      ]
                   |    }
                   |  ],
                   |  "categorizedByTags" : [
                   |    { "_id" : "painting", "count" : 6 },
                   |    { "_id" : "oil", "count" : 4 },
                   |    { "_id" : "Expressionism", "count" : 3 }
                   |  ]
                   |}""".stripMargin,
              ),
            ),
          )

          for {
            _ <- collection.insertMany(documents)

            result <- collection
              .aggregate(
                aggregates.facet(
                  Facet(
                    "categorizedByTags",
                    aggregates.unwind("$tags"),
                    aggregates.sortByCount(expressions.fieldPath("$tags")),
                    aggregates.limit(3),
                  ),
                  Facet(
                    "categorizedByPrice",
                    aggregates.`match`(filters.exists("price")),
                    aggregates.bucket(
                      groupBy = expressions.fieldPath("$price"),
                      boundaries = Chunk(0, 150, 200, 300, 400),
                      default = "Other",
                      output = Map(
                        "count"  -> accumulators.sum(expressions.const(1)),
                        "titles" -> accumulators.push(expressions.fieldPath("$title")),
                      ),
                    ),
                  ),
                  Facet(
                    "categorizedByYears(Auto)",
                    aggregates.bucketAuto(
                      groupBy = expressions.fieldPath("$year"),
                      buckets = 4,
                    ),
                  ),
                ),
              )
              .runToChunk
          } yield assertTrue(result == expected)
        }
      },
      suite("unwind")(
        test("array") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{ "_id" : 1, "item" : "ABC1", sizes: ["S", "M", "L"] }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{ "_id" : 1, "item" : "ABC1", "sizes" : "S" }""",
              """{ "_id" : 1, "item" : "ABC1", "sizes" : "M" }""",
              """{ "_id" : 1, "item" : "ABC1", "sizes" : "L" }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result <- collection.aggregate(aggregates.unwind("$sizes")).runToChunk
            } yield assertTrue(result == expected)
          }
        },
        test("missing and non-array values") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{ "_id" : 1, "item" : "Shirt", "sizes": [ "S", "M", "L"] }""",
              """{ "_id" : 2, "item" : "Shorts", "sizes" : [ ] }""",
              """{ "_id" : 3, "item" : "Hat", "sizes": "M" }""",
              """{ "_id" : 4, "item" : "Gloves" }""",
              """{ "_id" : 5, "item" : "Scarf", "sizes" : null }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{ _id: 1, item: 'Shirt', sizes: 'S' }""",
              """{ _id: 1, item: 'Shirt', sizes: 'M' }""",
              """{ _id: 1, item: 'Shirt', sizes: 'L' }""",
              """{ _id: 3, item: 'Hat', sizes: 'M' }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result <- collection.aggregate(aggregates.unwind("$sizes")).runToChunk
            } yield assertTrue(result == expected)
          }
        },
        test("preserveNullAndEmptyArrays") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{ "_id" : 1, "item" : "ABC", price: NumberDecimal("80"), "sizes": [ "S", "M", "L"] }""",
              """{ "_id" : 2, "item" : "EFG", price: NumberDecimal("120"), "sizes" : [ ] }""",
              """{ "_id" : 3, "item" : "IJK", price: NumberDecimal("160"), "sizes": "M" }""",
              """{ "_id" : 4, "item" : "LMN" , price: NumberDecimal("10") }""",
              """{ "_id" : 5, "item" : "XYZ", price: NumberDecimal("5.75"), "sizes" : null }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{ "_id" : 1, "item" : "ABC", "price" : NumberDecimal("80"), "sizes" : "S" }""",
              """{ "_id" : 1, "item" : "ABC", "price" : NumberDecimal("80"), "sizes" : "M" }""",
              """{ "_id" : 1, "item" : "ABC", "price" : NumberDecimal("80"), "sizes" : "L" }""",
              """{ "_id" : 2, "item" : "EFG", "price" : NumberDecimal("120") }""",
              """{ "_id" : 3, "item" : "IJK", "price" : NumberDecimal("160"), "sizes" : "M" }""",
              """{ "_id" : 4, "item" : "LMN", "price" : NumberDecimal("10") }""",
              """{ "_id" : 5, "item" : "XYZ", "price" : NumberDecimal("5.75"), "sizes" : null }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result <- collection
                .aggregate(aggregates.unwind("$sizes", preserveNullAndEmptyArrays = Some(true)))
                .runToChunk
            } yield assertTrue(result == expected)
          }
        },
        test("includeArrayIndex") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{ "_id" : 1, "item" : "ABC", price: NumberDecimal("80"), "sizes": [ "S", "M", "L"] }""",
              """{ "_id" : 2, "item" : "EFG", price: NumberDecimal("120"), "sizes" : [ ] }""",
              """{ "_id" : 3, "item" : "IJK", price: NumberDecimal("160"), "sizes": "M" }""",
              """{ "_id" : 4, "item" : "LMN" , price: NumberDecimal("10") }""",
              """{ "_id" : 5, "item" : "XYZ", price: NumberDecimal("5.75"), "sizes" : null }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{ "_id" : 1, "item" : "ABC", "price" : NumberDecimal("80"), "sizes" : "S", "arrayIndex" : NumberLong(0) }""",
              """{ "_id" : 1, "item" : "ABC", "price" : NumberDecimal("80"), "sizes" : "M", "arrayIndex" : NumberLong(1) }""",
              """{ "_id" : 1, "item" : "ABC", "price" : NumberDecimal("80"), "sizes" : "L", "arrayIndex" : NumberLong(2) }""",
              """{ "_id" : 3, "item" : "IJK", "price" : NumberDecimal("160"), "sizes" : "M", "arrayIndex" : null }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result <- collection
                .aggregate(aggregates.unwind("$sizes", includeArrayIndex = Some("arrayIndex")))
                .runToChunk
            } yield assertTrue(result == expected)
          }
        },
      ),
    ).provideLayerShared(MongoClientTest.live().orDie)
}
