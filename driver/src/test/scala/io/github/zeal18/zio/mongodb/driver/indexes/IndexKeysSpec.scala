package io.github.zeal18.zio.mongodb.driver.indexes

import scala.annotation.nowarn

import io.github.zeal18.zio.mongodb.driver.indexes
import zio.test.ZSpec
import zio.test.*

object IndexKeysSpec extends DefaultRunnableSpec {
  private def testKey(title: String, index: indexes.IndexKey, expected: String) =
    test(title) {
      assertTrue(index.toBson.toBsonDocument.toString == expected)
    }

  @nowarn("cat=deprecation")
  override def spec: ZSpec[Environment, Failure] = suite("IndexKeysSpec")(
    testKey("ascending", indexes.asc("a", "b"), """{"a": 1, "b": 1}"""),
    testKey("descending", indexes.desc("a", "b"), """{"a": -1, "b": -1}"""),
    testKey(
      "geo2dsphere",
      indexes.geo2dsphere("a", "b"),
      """{"a": "2dsphere", "b": "2dsphere"}""",
    ),
    testKey(
      "geo2d",
      indexes.geo2d("a", "b"),
      """{"a": "2d", "b": "2d"}""",
    ),
    testKey(
      "geoHaystack",
      indexes.geoHaystack("a", "additionalField"),
      """{"a": "geoHaystack", "additionalField": 1}""",
    ),
    testKey("text", indexes.text("a", "b"), """{"a": "text", "b": "text"}"""),
    testKey("hashed", indexes.hashed("a", "b"), """{"a": "hashed", "b": "hashed"}"""),
    suite("compound")(
      testKey(
        "basic",
        indexes.compound(indexes.asc("a"), indexes.desc("b")),
        """{"a": 1, "b": -1}""",
      ),
      testKey(
        "overriding",
        indexes.compound(indexes.asc("a"), indexes.desc("a")),
        """{"a": -1}""",
      ),
    ),
  )
}
