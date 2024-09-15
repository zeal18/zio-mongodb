package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.BsonString
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.indexes
import io.github.zeal18.zio.mongodb.driver.indexes.CreateIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.DropIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.Index
import io.github.zeal18.zio.mongodb.driver.indexes.IndexOptions
import io.github.zeal18.zio.mongodb.driver.model.InsertManyOptions
import io.github.zeal18.zio.mongodb.driver.model.InsertOneOptions
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoCollectionTest
import zio.Chunk
import zio.*
import zio.test.*

object TransactionsSpec extends ZIOSpecDefault {
  private case class Model(a: Int, b: String)

  override def spec = suite("TransactionsSpec")(
    suite("transactions")(
      test("committing transaction") {
        val model = Model(a = 42, b = "foo")

        MongoCollectionTest.withRandomName[Model, TestResult] { collection =>
          ZIO.scoped[Any] {
            for {
              session <- collection.startSession()
              _ = session.startTransaction()
              _ <- collection.insertOne(session, model)
              _ <- session.commitTransaction()
            } yield ()
          } *>
            collection.find().runToChunk.map(result => assertTrue(result == Chunk(model)))
        }
      },
      test("abording transaction") {
        val model = Model(a = 42, b = "foo")

        MongoCollectionTest.withRandomName[Model, TestResult] { collection =>
          ZIO.scoped[Any] {
            for {
              session <- collection.startSession()
              _ = session.startTransaction()
              _ <- collection.insertOne(session, model)
              _ <- session.abortTransaction()
            } yield ()
          } *>
            collection.find().runToChunk.map(result => assertTrue(result.isEmpty))
        }
      },
      test("not committed transaction is aborted") {
        val model = Model(a = 42, b = "foo")

        MongoCollectionTest.withRandomName[Model, TestResult] { collection =>
          ZIO.scoped[Any] {
            for {
              session <- collection.startSession()
              _ = session.startTransaction()
              _ <- collection.insertOne(session, model)
            } yield ()
          } *>
            collection.find().runToChunk.map(result => assertTrue(result.isEmpty))
        }
      },
      test("starting transaction twice") {
        val model = Model(a = 42, b = "foo")

        MongoCollectionTest.withRandomName[Model, TestResult] { collection =>
          ZIO.scoped[Any] {
            for {
              session <- collection.startSession()
              _ = session.startTransaction()
              _ = session.startTransaction()
              _ <- collection.insertOne(session, model)
            } yield ()
          } *>
            collection.find().runToChunk.map(result => assertTrue(result.isEmpty))
        }
      },
    ),
  ).provideLayerShared(MongoClientTest.liveSharded())
}
