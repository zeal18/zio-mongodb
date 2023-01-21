package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoDatabase
import io.github.zeal18.zio.mongodb.driver.filters
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoDatabaseTest
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.*

object Test {

  val client: ZLayer[Any, Throwable, MongoClient] =
    MongoClient.live("mongodb://localhost:27017")

  val database: ZLayer[Any, Throwable, MongoDatabase] =
    client >>> ZLayer.fromZIO(ZIO.serviceWith[MongoClient](_.getDatabase("database-name")))
}

case class User(@BsonId id: Int, name: String)

trait UsersDal {
  def insert(user: User): Task[Boolean]
  def get(id: Int): Task[Option[User]]
  def delete(id: Int): Task[Boolean]
}

object UsersDal {
  val live: ZLayer[MongoDatabase, Nothing, UsersDal] = ZLayer.fromZIO(for {
    mongo <- ZIO.service[MongoDatabase]

    coll =
      mongo
        .getCollection[User]("users")
        .withReadConcern(ReadConcern.MAJORITY)
        .withWriteConcern(WriteConcern.MAJORITY)
  } yield new UsersDal {
    override def insert(user: User): Task[Boolean] =
      coll.insertOne(user).map(_.wasAcknowledged())

    override def get(id: Int): Task[Option[User]] =
      coll.find(filters.eq(id)).runHead

    override def delete(id: Int): Task[Boolean] =
      coll.deleteOne(filters.eq(id)).map(_.getDeletedCount() > 0)
  })
}

object UsersDalSpec extends ZIOSpecDefault {
  private val env =
    MongoDatabaseTest.random >>>
      UsersDal.live

  override def spec = suite("UsersDalSpec")(
    test("insert user") {
      for {
        dal <- ZIO.service[UsersDal]

        inserted <- dal.insert(User(3, "John Doe"))
      } yield assertTrue(inserted)
    },
    test("get user") {
      for {
        dal <- ZIO.service[UsersDal]
        user = User(5, "John Doe")

        _      <- dal.insert(user)
        result <- dal.get(5)
      } yield assertTrue(result.get == user)
    },
    test("delete user") {
      for {
        dal <- ZIO.service[UsersDal]
        user = User(5, "John Doe")

        _       <- dal.insert(user)
        deleted <- dal.delete(5)
        result  <- dal.get(5)
      } yield assertTrue(deleted, result.isEmpty)
    },
  ).provideSomeLayer(env).provideLayerShared(MongoClientTest.live())
}
