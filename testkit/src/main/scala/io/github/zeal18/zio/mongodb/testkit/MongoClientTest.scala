package io.github.zeal18.zio.mongodb.testkit

import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion
import de.flapdoodle.embed.mongo.distribution.Version
import io.github.zeal18.zio.mongodb.driver.MongoClient
import zio.ZLayer
import zio.test.Live

object MongoClientTest {
  def live(
    version: IFeatureAwareVersion = Version.Main.V4_4,
  ): ZLayer[Live, Throwable, MongoClient] =
    EmbeddedMongo.live(version).flatMap { process =>
      val address          = process.get.current().getServerAddress()
      val connectionString = s"mongodb://${address.getHost()}:${address.getPort()}"

      MongoClient.live(connectionString)
    }
}
