package io.github.zeal18.zio.mongodb.driver.indexes

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.driver.indexes
import zio.test.*
import zio.test.ZIOSpecDefault

import scala.annotation.nowarn

object IndexKeysSpec extends ZIOSpecDefault {
  private def testKey(title: String, index: indexes.IndexKey, expected: String) =
    test(title) {
      assertTrue(index.toBsonDocument().toString == expected)
    }

  @nowarn("cat=deprecation")
  override def spec = suite("IndexKeysSpec")(
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
    suite("raw")(
      testKey(
        "bson",
        indexes.raw(BsonDocument("a" -> BsonInt32(1), "b" -> BsonInt32(-1))),
        """{"a": 1, "b": -1}""",
      ),
      testKey(
        "json",
        indexes.raw("""{"a": 1, "b": -1}"""),
        """{"a": 1, "b": -1}""",
      ),
    ),
  )
}
