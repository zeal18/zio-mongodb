package io.github.zeal18.zio.mongodb.driver.sorts

import io.github.zeal18.zio.mongodb.driver.sorts
import zio.test.ZIOSpecDefault
import zio.test.*

object SortSpec extends ZIOSpecDefault {
  private def testSort(title: String, sort: sorts.Sort, expected: String) =
    test(title) {
      assertTrue(sort.toBson.toBsonDocument.toString == expected)
    }

  override def spec = suite("SortSpec")(
    testSort("asc", sorts.asc("a"), """{"a": 1}"""),
    testSort("desc", sorts.desc("a"), """{"a": -1}"""),
    testSort("textScore", sorts.textScore("a"), """{"a": {"$meta": "textScore"}}"""),
    testSort(
      "compound",
      sorts.compound(sorts.asc("a"), sorts.desc("b")),
      """{"a": 1, "b": -1}""",
    ),
  )
}
