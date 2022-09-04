package io.github.zeal18.zio.mongodb.testkit

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoCollection
import izumi.reflect.Tag
import zio.ZIO
import zio.ZLayer
import zio.random.Random
import zio.test.environment.Live

object MongoCollectionTest {
  def withRandomName[A: Tag: ClassTag, B](
    f: MongoCollection.Service[A] => ZIO[Live with MongoClient, Throwable, B],
  )(implicit c: Codec[A]): ZIO[Live with MongoClient, Throwable, B] =
    random.build.use(db => f(db.get))

  def withName[A: Tag: ClassTag, B](name: String)(
    f: MongoCollection.Service[A] => ZIO[Live with MongoClient, Throwable, B],
  )(implicit c: Codec[A]): ZIO[Live with MongoClient, Throwable, B] =
    (ZLayer.requires[Live with MongoClient] ++ MongoDatabaseTest.random >>>
      MongoCollection.live(name)).build.use(db => f(db.get))

  def random[A: Tag: ClassTag](implicit
    c: Codec[A],
  ): ZLayer[Live with MongoClient, Throwable, MongoCollection[A]] =
    ZLayer.requires[Live with MongoClient] ++ MongoDatabaseTest.random >>>
      ZLayer
        .fromEffect(for {
          random <- Live.live(ZIO.service[Random.Service])
          name   <- random.nextUUID.map(_.toString)
        } yield name)
        .flatMap(name => MongoCollection.live(name.get))
}
