package io.github.zeal18.zio.mongodb.testkit

import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoDatabase
import zio.ZIO
import zio.ZLayer
import zio.test.Live

object MongoDatabaseTest {
  def withRandomName[A](
    f: MongoDatabase => ZIO[MongoClient, Throwable, A],
  ): ZIO[MongoClient, Throwable, A] =
    ZIO.scoped {
      random.build.flatMap(db => f(db.get))
    }

  def withName[A](name: String)(
    f: MongoDatabase => ZIO[MongoClient, Throwable, A],
  ): ZIO[MongoClient, Throwable, A] =
    ZIO.scoped {
      MongoDatabase.live(name).build.flatMap(db => f(db.get))
    }

  val random: ZLayer[MongoClient, Throwable, MongoDatabase] =
    ZLayer
      .fromZIO(Live.live(ZIO.random.flatMap(_.nextUUID.map(_.toString))))
      .flatMap(name => MongoDatabase.live(name.get))
}
