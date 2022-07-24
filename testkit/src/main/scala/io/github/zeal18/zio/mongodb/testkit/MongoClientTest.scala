package io.github.zeal18.zio.mongodb.testkit

import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import de.flapdoodle.embed.mongo.distribution.Version
import io.github.zeal18.zio.mongodb.driver.MongoClient
import zio.Has
import zio.ZLayer
import zio.test.environment.Live

object MongoClientTest {
  def live(
    version: IFeatureAwareVersion = Version.Main.PRODUCTION,
  ): ZLayer[Live, Throwable, Has[MongoClient]] =
    EmbeddedMongo.live(version).map { process =>
      val net              = process.get.getConfig.net
      val connectionString = s"mongodb://${net.getBindIp()}:${net.getPort}"

      Has(MongoClient(connectionString))
    }
}
