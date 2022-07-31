package io.github.zeal18.zio.mongodb.testkit

import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoDatabase
import zio.Has
import zio.ZIO
import zio.ZLayer
import zio.random.Random
import zio.test.environment.Live

object MongoDatabaseTest {
  def withRandomName[A](
    f: MongoDatabase => ZIO[Live with Has[MongoClient], Throwable, A],
  ): ZIO[Live with Has[MongoClient], Throwable, A] =
    random.build.use(db => f(db.get))

  def withName[A](name: String)(
    f: MongoDatabase => ZIO[Live with Has[MongoClient], Throwable, A],
  ): ZIO[Live with Has[MongoClient], Throwable, A] =
    live(name).build.use(db => f(db.get))

  val random: ZLayer[Live with Has[MongoClient], Throwable, Has[MongoDatabase]] =
    ZLayer
      .fromEffect(for {
        random <- Live.live(ZIO.service[Random.Service])
        name   <- random.nextString(10)
      } yield name)
      .flatMap(name => live(name.get))

  def live(name: String): ZLayer[Has[MongoClient], Throwable, Has[MongoDatabase]] =
    ZLayer.fromEffect(
      for {
        client <- ZIO.service[MongoClient]
        db = client.getDatabase(name)
      } yield db,
    )
}
