package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import zio.ZIO
import zio.test.*

object MongoClientSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("MongoClientSpec")(
    testM("getDatabase") {
      for {
        client <- ZIO.service[MongoClient]
        db = client.getDatabase("db-test")
      } yield assertTrue(db.name == "db-test")
    },
    testM("listDatabaseNames") {
      for {
        client <- ZIO.service[MongoClient]
        db = client.getDatabase("db-test")
        _   <- db.createCollection("collection-test") // create collection to create the database
        dbs <- client.listDatabaseNames().runCollect
      } yield assertTrue(dbs.size > 0, dbs.contains("db-test"))
    },
  ).provideCustomLayerShared(MongoClientTest.live().orDie)
}
