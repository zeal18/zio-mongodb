# zio-mongodb
One more ZIO wrapper around the official MongoDB Java driver but better ;)

The main goals of the project:
* the first class ZIO support
* encapsulate Java driver runtime codecs resolution by a compile time solution
* Scala 3 support
* codecs derivation support
* split query building and results processing
* type-safe query building DSL
* provide zio-test integrated test-kit

# Quick start

## Import the library

```sbt
libraryDependencies ++= Seq(
  "io.github.zeal18" %% "zio-mongodb-bson"    % "0.10.2",
  "io.github.zeal18" %% "zio-mongodb-driver"  % "0.10.2",
  "io.github.zeal18" %% "zio-mongodb-testkit" % "0.10.2" % Test
)
```

## Create client and database layers

```scala
import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoDatabase
import zio.ZIO
import zio.ZLayer

val client: ZLayer[Any, Throwable, MongoClient] =
  MongoClient.live("mongodb://localhost:27017")

val database: ZLayer[Any, Throwable, MongoDatabase] =
  client >>> ZLayer.fromZIO(ZIO.serviceWith[MongoClient](_.getDatabase("database-name")))
```

## Create DAL

```scala
import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.driver.MongoDatabase
import io.github.zeal18.zio.mongodb.driver.filters
import zio.Task
import zio.ZLayer

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
```

## Test the DAL with Embedded MongoDB

```scala
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoDatabaseTest
import zio.ZIO
import zio.test.*

object UsersDalSpec extends ZIOSpecDefault {
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
  ).provideSomeLayer(MongoDatabaseTest.random >>> UsersDal.live)
    .provideLayerShared(MongoClientTest.live())
}
```

NOTE: we provide `MongoClientTest` as a shared layer to prevent relaunching the MongoDB instance for every test case. At the same time we want to recreate databases with random names to isolate test cases from each other.

# ZIO 1.x and ZIO 2.x support

ZIO 2.x is the main ZIO version supported by the library starting from the `0.6.0` version. 
ZIO 1.x will be supported for some time in `0.5.x` releases.
