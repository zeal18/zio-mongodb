package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoCollectionTest
import org.bson.BsonDocument
import org.bson.BsonType
import org.bson.types.ObjectId
import zio.Chunk
import zio.test.*

object FiltersItSpec extends ZIOSpecDefault {
  final private case class Person(
    @BsonId
    id: Int,
    name: String,
  )
  final private case class Person2(
    @BsonId
    id: Int,
    name: String,
    age: Option[Int]
  )

  override def spec =
    suite("FiltersItSpec")(
      suite("equal")(
        test("eq _id") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.eq(42)).runToChunk
              result2 <- collection.find(filters.eq(43)).runToChunk
            } yield assertTrue(result1 == Chunk(person1), result2 == Chunk(person2))
          }
        },
        test("eq name") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.eq("name", "foo")).runToChunk
              result2 <- collection.find(filters.eq("name", "bar")).runToChunk
            } yield assertTrue(result1 == Chunk(person1), result2 == Chunk(person2))
          }
        },
        test("is null age") {
          val person1 = Person2(id = 42, name = "foo", age = None)
          val person2 = Person2(id = 43, name = "bar", age = Some(42))
          val person3 = Person2(id = 44, name = "baz", age = None)

          MongoCollectionTest.withRandomName[Person2, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2, person3))

              result1 <- collection.find(filters.isNull("age")).runToChunk
              result2 <- collection.find(filters.equal("age",None)).runToChunk
            } yield assertTrue(result1.length == 2 && result2.length == 2)
          }
        },
        test("not null age") {
          val person1 = Person2(id = 42, name = "foo", age = None)
          val person2 = Person2(id = 43, name = "bar", age = Some(42))
          val person3 = Person2(id = 44, name = "baz", age = None)

          MongoCollectionTest.withRandomName[Person2, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2, person3))

              result1 <- collection.find(filters.notNull("age")).runToChunk
              result2 <- collection.find(filters.notEqual("age",None)).runToChunk
            } yield assertTrue(result1.length == 1 && result2.length == 1)
          }
        }
      ),
      test("ne") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2))

            result1 <- collection.find(filters.ne("name", "foo")).runToChunk
          } yield assertTrue(result1 == Chunk(person2))
        }
      },
      suite("gt/gte")(
        test("gt") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.gt("_id", 42)).runToChunk
            } yield assertTrue(result1 == Chunk(person2))
          }
        },
        test("gte") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.gte("_id", 43)).runToChunk
            } yield assertTrue(result1 == Chunk(person2))
          }
        },
      ),
      suite("lt/lte")(
        test("lt") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.lt("_id", 43)).runToChunk
            } yield assertTrue(result1 == Chunk(person1))
          }
        },
        test("lte") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.lte("_id", 42)).runToChunk
            } yield assertTrue(result1 == Chunk(person1))
          }
        },
      ),
      test("in") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection.find(filters.in("_id", Seq(42, 44))).runToChunk
            result2 <- collection.find(filters.in("name", Seq("bar"))).runToChunk
          } yield assertTrue(result1 == Chunk(person1, person3), result2 == Chunk(person2))
        }
      },
      test("nin") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection.find(filters.nin("_id", Seq(42, 44))).runToChunk
            result2 <- collection.find(filters.nin("name", Seq("bar"))).runToChunk
          } yield assertTrue(result1 == Chunk(person2), result2 == Chunk(person1, person3))
        }
      },
      test("and") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection
              .find(
                filters.and(
                  filters.in("_id", Seq(42, 44)),
                  filters.eq("name", "baz"),
                ),
              )
              .runToChunk
          } yield assertTrue(result1 == Chunk(person3))
        }
      },
      test("or") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection
              .find(
                filters.or(
                  filters.eq(42),
                  filters.eq("name", "baz"),
                ),
              )
              .runToChunk
          } yield assertTrue(result1 == Chunk(person1, person3))
        }
      },
      test("nor") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection
              .find(
                filters.nor(
                  filters.eq(42),
                  filters.eq("name", "baz"),
                ),
              )
              .runToChunk
          } yield assertTrue(result1 == Chunk(person2))
        }
      },
      test("not") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection.find(filters.not(filters.eq(43))).runToChunk
          } yield assertTrue(result1 == Chunk(person1, person3))
        }
      },
      test("exists") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val doc = Document("_id" -> new ObjectId(), "foo" -> "bar")
          for {
            _ <- collection.insertOne(doc)

            result1 <- collection.find(filters.exists("foo", true)).runToChunk
            result2 <- collection.find(filters.exists("foo", false)).runToChunk
            result3 <- collection.find(filters.exists("baz", true)).runToChunk
            result4 <- collection.find(filters.exists("baz", false)).runToChunk
          } yield assertTrue(
            result1 == Chunk(doc),
            result2 == Chunk.empty,
            result3 == Chunk.empty,
            result4 == Chunk(doc),
          )
        }
      },
      test("type") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val doc = Document("_id" -> new ObjectId(), "foo" -> "bar")
          for {
            _ <- collection.insertOne(doc)

            result1 <- collection.find(filters.`type`("foo", BsonType.STRING)).runToChunk
            result2 <- collection.find(filters.`type`("foo", BsonType.INT32)).runToChunk
            result3 <- collection.find(filters.`type`("baz", BsonType.STRING)).runToChunk
          } yield assertTrue(
            result1 == Chunk(doc),
            result2 == Chunk.empty,
            result3 == Chunk.empty,
          )
        }
      },
      test("regex") {
        val person1 = Person(id = 42, name = "foo")
        val person2 = Person(id = 43, name = "bar")
        val person3 = Person(id = 44, name = "baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2, person3))

            result1 <- collection.find(filters.regex("name", "ba.?")).runToChunk
          } yield assertTrue(result1 == Chunk(person2, person3))
        }
      },
      test("text") {
        val person1 = Person(id = 42, name = "foo bar")
        val person2 = Person(id = 43, name = "bar baz")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.createIndex(indexes.text("name"))
            _ <- collection.insertMany(Chunk(person1, person2))

            result1 <- collection.find(filters.text("bar")).runToChunk
            result2 <- collection.find(filters.text("foo")).runToChunk
          } yield assertTrue(result1.toSet == Set(person1, person2), result2 == Chunk(person1))
        }
      },
      test("where") {
        val person1 = Person(id = 42, name = "Steve")
        val person2 = Person(id = 43, name = "Anya")

        MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(person1, person2))

            result1 <- collection
              .find(
                filters.where(
                  """function() { return (hex_md5(this.name) == "9b53e667f30cd329dca1ec9e6a83e994") }""",
                ),
              )
              .runToChunk
          } yield assertTrue(result1.toSet == Set(person2))
        }
      },
      test("expr") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val doc1 = Document("_id" -> 1, "budget" -> 400, "spent" -> 450)
          val doc2 = Document("_id" -> 2, "budget" -> 100, "spent" -> 150)
          val doc3 = Document("_id" -> 3, "budget" -> 100, "spent" -> 50)
          val doc4 = Document("_id" -> 4, "budget" -> 500, "spent" -> 300)
          val doc5 = Document("_id" -> 5, "budget" -> 200, "spent" -> 650)

          for {
            _ <- collection.insertMany(Chunk(doc1, doc2, doc3, doc4, doc5))

            result <- collection.find(filters.expr(aggregates.raw("""{"$gt": ["$spent", "$budget"]}"""))).runToChunk
          } yield assertTrue(result == Chunk(doc1, doc2, doc5))
        }
      },
      test("mod") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val doc1 = Document("_id" -> 1, "budget" -> 4)
          val doc2 = Document("_id" -> 2, "budget" -> 1)
          val doc3 = Document("_id" -> 3, "budget" -> 1)
          val doc4 = Document("_id" -> 4, "budget" -> 5)
          val doc5 = Document("_id" -> 5, "budget" -> 2)

          for {
            _ <- collection.insertMany(Chunk(doc1, doc2, doc3, doc4, doc5))

            result <- collection.find(filters.mod("budget", 2, 0)).runToChunk
          } yield assertTrue(result == Chunk(doc1, doc5))
        }
      },
      test("all") {
        val doc1 = Document("_id" -> 1, "tags" -> Seq("A", "B", "C"))
        val doc2 = Document("_id" -> 2, "tags" -> Seq("B", "C"))
        val doc3 = Document("_id" -> 3, "tags" -> Seq("A", "C"))

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2, doc3))

            result1 <- collection.find(filters.all("tags", Chunk("A", "C"))).runToChunk
          } yield assertTrue(result1.toSet == Set(doc1, doc3))
        }
      },
      test("elemMatch") {
        val doc1 = Document("_id" -> 1, "results" -> Seq(82, 85, 88))
        val doc2 = Document("_id" -> 2, "results" -> Seq(75, 88, 89))

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2))

            result1 <- collection
              .find(
                filters.elemMatch("results", filters.raw("""{"$gte": 80, "$lte": 85}""")),
              )
              .runToChunk
          } yield assertTrue(result1.toSet == Set(doc1))
        }
      },
      test("size") {
        val doc1 = Document("_id" -> 1, "results" -> Seq(82))
        val doc2 = Document("_id" -> 2, "results" -> Seq(82, 85))
        val doc3 = Document("_id" -> 3, "results" -> Seq(82, 85, 88))

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2, doc3))

            result1 <- collection.find(filters.size("results", 1)).runToChunk
            result2 <- collection.find(filters.size("results", 2)).runToChunk
            result3 <- collection.find(filters.size("results", 3)).runToChunk
          } yield assertTrue(result1 == Chunk(doc1), result2 == Chunk(doc2), result3 == Chunk(doc3))
        }
      },
      test("bitsAllClear") {
        val doc1 = Document("_id" -> 1, "a" -> 54, "binaryValueofA" -> "0011 0110")
        val doc2 = Document("_id" -> 2, "a" -> 20, "binaryValueofA" -> "0001 0100")

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2))

            result1 <- collection.find(filters.bitsAllClear("a", 1)).runToChunk  // 0000 0001
            result2 <- collection.find(filters.bitsAllClear("a", 9)).runToChunk  // 0000 1001
            result3 <- collection.find(filters.bitsAllClear("a", 41)).runToChunk // 0010 0001
          } yield assertTrue(
            result1 == Chunk(doc1, doc2),
            result2 == Chunk(doc1, doc2),
            result3 == Chunk(doc2),
          )
        }
      },
      test("bitsAllSet") {
        val doc1 = Document("_id" -> 1, "a" -> 54, "binaryValueofA" -> "0011 0110")
        val doc2 = Document("_id" -> 2, "a" -> 20, "binaryValueofA" -> "0001 0100")

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2))

            result1 <- collection.find(filters.bitsAllSet("a", 20)).runToChunk // 0001 0100
            result2 <- collection.find(filters.bitsAllSet("a", 22)).runToChunk // 0001 0110
          } yield assertTrue(
            result1 == Chunk(doc1, doc2),
            result2 == Chunk(doc1),
          )
        }
      },
      test("bitsAnyClear") {
        val doc1 = Document("_id" -> 1, "a" -> 54, "binaryValueofA" -> "0011 0110")
        val doc2 = Document("_id" -> 2, "a" -> 20, "binaryValueofA" -> "0001 0100")

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2))

            result1 <- collection.find(filters.bitsAnyClear("a", 6)).runToChunk  // 0000 0110
            result2 <- collection.find(filters.bitsAnyClear("a", 14)).runToChunk // 0000 1110
          } yield assertTrue(
            result1 == Chunk(doc2),
            result2 == Chunk(doc1, doc2),
          )
        }
      },
      test("bitsAnySet") {
        val doc1 = Document("_id" -> 1, "a" -> 54, "binaryValueofA" -> "0011 0110")
        val doc2 = Document("_id" -> 2, "a" -> 20, "binaryValueofA" -> "0001 0100")

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2))

            result1 <- collection.find(filters.bitsAnySet("a", 96)).runToChunk // 0110 0000
            result2 <- collection.find(filters.bitsAnySet("a", 12)).runToChunk // 0000 1100
          } yield assertTrue(
            result1 == Chunk(doc1),
            result2 == Chunk(doc1, doc2),
          )
        }
      },
      test("jsonSchema") {
        val doc1 = Document("_id" -> 1, "bar" -> 20)
        val doc2 = Document("_id" -> 2, "name" -> "foo")

        val schema = BsonDocument.parse("""|{
                                           |  required: [ "name" ],
                                           |  properties: {
                                           |    name: {
                                           |      bsonType: "string",
                                           |      description: "must be a string and is required"
                                           |    }
                                           |  }
                                           |}""".stripMargin)

        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          for {
            _ <- collection.insertMany(Chunk(doc1, doc2))

            result <- collection.find(filters.jsonSchema(schema)).runToChunk
          } yield assertTrue(
            result == Chunk(doc2),
          )
        }
      },
      suite("raw")(
        test("bson") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.raw(Document("name" -> "foo"))).runToChunk
              result2 <- collection.find(filters.raw(Document("name" -> "bar"))).runToChunk
            } yield assertTrue(result1 == Chunk(person1), result2 == Chunk(person2))
          }
        },
        test("json") {
          val person1 = Person(id = 42, name = "foo")
          val person2 = Person(id = 43, name = "bar")

          MongoCollectionTest.withRandomName[Person, TestResult] { collection =>
            for {
              _ <- collection.insertMany(Chunk(person1, person2))

              result1 <- collection.find(filters.raw("""{"name": "foo"}""")).runToChunk
              result2 <- collection.find(filters.raw("""{"name": "bar"}""")).runToChunk
            } yield assertTrue(result1 == Chunk(person1), result2 == Chunk(person2))
          }
        },
      ),
    ).provideLayerShared(MongoClientTest.live().orDie)
}
