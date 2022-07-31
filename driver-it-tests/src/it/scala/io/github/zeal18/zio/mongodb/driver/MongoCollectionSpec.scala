package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.indexes
import io.github.zeal18.zio.mongodb.driver.indexes.CreateIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.DropIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.Index
import io.github.zeal18.zio.mongodb.driver.indexes.IndexOptions
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoDatabaseTest
import zio.Chunk
import zio.ZIO
import zio.duration.*
import zio.test.*

object MongoCollectionSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("MongoCollectionSpec")(
    suite("indexes")(
      suite("createIndex")(
        testM("createIndex") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- collection.createIndex(
                indexes.compound(indexes.asc("a"), indexes.desc("b")),
              )
            } yield assertTrue(result == "a_1_b_-1")
          }
        },
        testM("createIndex with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- collection.createIndex(
                indexes.asc("a"),
                options = IndexOptions(name = Some("index-name")),
              )
            } yield assertTrue(result == "index-name")
          }
        },
        testM("createIndex in session") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- client.startSession().use { session =>
                collection.createIndex(session, indexes.geo2d("a"))
              }
            } yield assertTrue(result == "a_2d")
          }
        },
        testM("createIndex in session with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- client.startSession().use { session =>
                collection.createIndex(
                  session,
                  indexes.text("a"),
                  IndexOptions(name = Some("text-index")),
                )
              }
            } yield assertTrue(result == "text-index")
          }
        },
      ),
      suite("createIndexes")(
        testM("createIndexes") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- collection.createIndexes(
                Index(indexes.asc("a")),
                Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
              )
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
        testM("createIndexes with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- collection.createIndexes(
                Seq(
                  Index(indexes.asc("a")),
                  Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
                ),
                CreateIndexOptions(maxTime = 10.seconds),
              )
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
        testM("createIndexes in session") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- client.startSession().use { session =>
                collection.createIndexes(
                  session,
                  Index(indexes.asc("a")),
                  Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
                )
              }
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
        testM("createIndexes in session with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              result <- client.startSession().use { session =>
                collection.createIndexes(
                  session,
                  Seq(
                    Index(indexes.asc("a")),
                    Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
                  ),
                  CreateIndexOptions(maxTime = 10.seconds),
                )
              }
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
      ),
      suite("dropIndex")(
        testM("dropIndex by name") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexName <- collection.createIndex(indexes.asc("a"))
              _         <- collection.dropIndex(indexName)

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by name with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexName <- collection.createIndex(indexes.asc("a"))
              _         <- collection.dropIndex(indexName, DropIndexOptions(10.seconds))

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexKey = indexes.asc("a")
              _ <- collection.createIndex(indexKey)
              _ <- collection.dropIndex(indexKey)

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexKey = indexes.asc("a")
              _ <- collection.createIndex(indexKey)
              _ <- collection.dropIndex(indexKey, DropIndexOptions(10.seconds))

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by name in session") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexName <- collection.createIndex(indexes.asc("a"))
              _ <- client.startSession().use { session =>
                collection.dropIndex(session, indexName)
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by name in session with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexName <- collection.createIndex(indexes.asc("a"))
              _ <- client.startSession().use { session =>
                collection.dropIndex(session, indexName, DropIndexOptions(10.seconds))
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key in session") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexKey = indexes.asc("a")
              _ <- collection.createIndex(indexKey)
              _ <- client.startSession().use { session =>
                collection.dropIndex(session, indexKey)
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key in session with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              indexKey = indexes.asc("a")
              _ <- collection.createIndex(indexKey)
              _ <- client.startSession().use { session =>
                collection.dropIndex(session, indexKey, DropIndexOptions(10.seconds))
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
      ),
      suite("dropIndexes")(
        testM("dropIndexes") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- collection.dropIndexes()

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndexes with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              _ <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- collection.dropIndexes(DropIndexOptions(10.seconds))

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndexes in session") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- client.startSession().use { session =>
                collection.dropIndexes(session)
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndexes in session with options") {
          MongoDatabaseTest.withRandomName { db =>
            for {
              client <- ZIO.service[MongoClient]
              _      <- db.createCollection("test-collection")
              collection = db.getCollection[Document]("test-collection")

              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- client.startSession().use { session =>
                collection.dropIndexes(session, DropIndexOptions(10.seconds))
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
      ),
    ),
  ).provideCustomLayerShared(MongoClientTest.live().orDie)
}
