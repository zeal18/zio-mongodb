package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.BsonString
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoDatabaseTest
import zio.test.*

object MongoDatabaseSpec extends ZIOSpecDefault {
  override def spec = suite("MongoDatabaseSpec")(
    test("createCollection") {
      MongoDatabaseTest.withRandomName { db =>
        for {
          _           <- db.createCollection("test-collection")
          collections <- db.listCollections().runHead.map(_.flatMap(_.get("name")))
        } yield assertTrue(collections.contains(BsonString("test-collection")))
      }
    },
  ).provideLayerShared(MongoClientTest.live())
}
