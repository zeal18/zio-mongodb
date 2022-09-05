package io.github.zeal18.zio.mongodb.driver.projections

import io.github.zeal18.zio.mongodb.driver.filters
import io.github.zeal18.zio.mongodb.driver.projections
import zio.test.ZIOSpecDefault
import zio.test.*

object ProjectionsSpec extends ZIOSpecDefault {
  private def testProjection(title: String, proj: projections.Projection, expected: String) =
    test(title) {
      assertTrue(proj.toBson.toBsonDocument.toString == expected)
    }

  override def spec = suite("ProjectionsSpec")(
    testProjection("computed", projections.computed("a", "b"), """{"a": "b"}"""),
    testProjection("include", projections.include("a", "b"), """{"a": 1, "b": 1}"""),
    testProjection("exclude", projections.exclude("a", "b"), """{"a": 0, "b": 0}"""),
    testProjection("excludeId", projections.excludeId(), """{"_id": 0}"""),
    testProjection("elemMatch", projections.elemMatch("a"), """{"a.$": 1}"""),
    testProjection(
      "elemMatch filter",
      projections.elemMatch("a", filters.eq("b")),
      """{"a": {"$elemMatch": {"_id": "b"}}}""",
    ),
    testProjection("meta", projections.meta("a", "b"), """{"a": {"$meta": "b"}}"""),
    testProjection(
      "metaTextScore",
      projections.metaTextScore("a"),
      """{"a": {"$meta": "textScore"}}""",
    ),
    testProjection("slice", projections.slice("a", 1), """{"a": {"$slice": 1}}"""),
    testProjection("slice skip", projections.slice("a", 1, 2), """{"a": {"$slice": [1, 2]}}"""),
    testProjection(
      "fields",
      projections.fields(projections.computed("a", "b"), projections.include("c", "d")),
      """{"a": "b", "c": 1, "d": 1}""",
    ),
  )
}
