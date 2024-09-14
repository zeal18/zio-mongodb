package io.github.zeal18.zio.mongodb.driver.indexes

import com.mongodb.client.model.Collation
import com.mongodb.client.model.IndexOptions as JIndexOptions
import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.filters.Filter

import java.util.concurrent.TimeUnit
import scala.annotation.nowarn

@nowarn("cat=deprecation")
final case class IndexOptions(
  name: Option[String] = None,
  background: Boolean = false,
  unique: Boolean = false,
  partialFilterExpression: Option[Filter] = None,
  sparse: Boolean = false,
  expireAfterSeconds: Option[Long] = None,
  hidden: Boolean = false,
  storageEngine: Option[Document] = None,
  weights: Option[Document] = None,
  defaultLanguage: Option[String] = None,
  languageOverride: Option[String] = None,
  textIndexVersion: Option[Int] = None,
  sphereIndexVersion: Option[Int] = None,
  bits: Option[Int] = None,
  min: Option[Double] = None,
  max: Option[Double] = None,
  @deprecated("geoHaystack is deprecated in MongoDB 4.4", "4.4")
  bucketSize: Option[Long] = None,
  collation: Option[Collation] = None,
  wildcardProjection: Option[Document] = None,
) {
  private[driver] def toJava: JIndexOptions = {
    val options =
      new JIndexOptions().background(background).unique(unique).sparse(sparse).hidden(hidden)

    name.foreach(options.name(_))
    partialFilterExpression.foreach(filter => options.partialFilterExpression(filter))
    expireAfterSeconds.foreach(options.expireAfter(_, TimeUnit.SECONDS))
    storageEngine.foreach(options.storageEngine(_))
    weights.foreach(options.weights(_))
    defaultLanguage.foreach(options.defaultLanguage(_))
    languageOverride.foreach(options.languageOverride(_))
    textIndexVersion.foreach(options.textVersion(_))
    sphereIndexVersion.foreach(options.sphereVersion(_))
    bits.foreach(options.bits(_))
    min.foreach(options.min(_))
    max.foreach(options.max(_))
    bucketSize.foreach(size => options.bucketSize(size.toDouble))
    collation.foreach(options.collation(_))
    wildcardProjection.foreach(options.wildcardProjection(_))

    options
  }
}
