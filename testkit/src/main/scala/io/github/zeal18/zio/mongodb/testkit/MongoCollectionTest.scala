package io.github.zeal18.zio.mongodb.testkit

import scala.reflect.ClassTag

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.driver.MongoClient
import io.github.zeal18.zio.mongodb.driver.MongoCollection
import izumi.reflect.Tag
import zio.ZIO
import zio.ZLayer
import zio.test.Live

object MongoCollectionTest {
  def withRandomName[A: Tag: ClassTag, B](
    f: MongoCollection[A] => ZIO[MongoClient, Throwable, B],
  )(implicit c: Codec[A]): ZIO[MongoClient, Throwable, B] =
    ZIO.scoped {
      random.build.flatMap(db => f(db.get))
    }

  def withName[A: Tag: ClassTag, B](name: String)(
    f: MongoCollection[A] => ZIO[MongoClient, Throwable, B],
  )(implicit c: Codec[A]): ZIO[MongoClient, Throwable, B] =
    ZIO.scoped {
      (ZLayer.environment[MongoClient] ++ MongoDatabaseTest.random >>>
        MongoCollection.live(name)).build.flatMap(db => f(db.get))
    }

  def random[A: Tag: ClassTag](implicit
    c: Codec[A],
  ): ZLayer[MongoClient, Throwable, MongoCollection[A]] =
    ZLayer.environment[MongoClient] ++ MongoDatabaseTest.random >>>
      ZLayer
        .fromZIO(Live.live(ZIO.random.flatMap(_.nextUUID.map(_.toString))))
        .flatMap(name => MongoCollection.live(name.get))
}
