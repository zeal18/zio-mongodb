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
      (MongoDatabaseTest.random >>>
        MongoCollection.live(name)).build.map(_.get).withFinalizer(_.drop().orDie).flatMap(f)
    }

  def random[A: Tag: ClassTag](implicit
    c: Codec[A],
  ): ZLayer[MongoClient, Throwable, MongoCollection[A]] =
    MongoDatabaseTest.random >>>
      ZLayer.scoped(for {
        name <- Live.live(ZIO.randomWith(_.nextUUID.map(_.toString)))
        db   <- MongoCollection.live(name).build.map(_.get)
        _    <- ZIO.addFinalizer(db.drop().orDie)
      } yield db)
}
