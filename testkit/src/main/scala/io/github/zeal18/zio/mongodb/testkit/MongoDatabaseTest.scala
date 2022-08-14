package io.github.zeal18.zio.mongodb.testkit

import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoDatabase
import zio.ZIO
import zio.ZLayer
import zio.random.Random
import zio.test.environment.Live

object MongoDatabaseTest {
  def withRandomName[A](
    f: MongoDatabase.Service => ZIO[Live with MongoClient, Throwable, A],
  ): ZIO[Live with MongoClient, Throwable, A] =
    random.build.use(db => f(db.get))

  def withName[A](name: String)(
    f: MongoDatabase.Service => ZIO[Live with MongoClient, Throwable, A],
  ): ZIO[Live with MongoClient, Throwable, A] =
    MongoDatabase.live(name).build.use(db => f(db.get))

  val random: ZLayer[Live with MongoClient, Throwable, MongoDatabase] =
    ZLayer
      .fromEffect(for {
        random <- Live.live(ZIO.service[Random.Service])
        name   <- random.nextUUID.map(_.toString)
      } yield name)
      .flatMap(name => MongoDatabase.live(name.get))
}
