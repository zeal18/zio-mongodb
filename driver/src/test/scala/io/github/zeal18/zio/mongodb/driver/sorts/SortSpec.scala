package io.github.zeal18.zio.mongodb.driver.sorts

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.driver.sorts
import zio.test.*

object SortSpec extends DefaultRunnableSpec {
  private def testSort(title: String, sort: sorts.Sort, expected: String) =
    test(title) {
      assertTrue(sort.toBson.toBsonDocument.toString == expected)
    }

  override def spec: ZSpec[Environment, Failure] =
    suite("SortSpec")(
      testSort("asc", sorts.asc("a"), """{"a": 1}"""),
      testSort("desc", sorts.desc("a"), """{"a": -1}"""),
      testSort("textScore", sorts.textScore("a"), """{"a": {"$meta": "textScore"}}"""),
      testSort(
        "compound",
        sorts.compound(sorts.asc("a"), sorts.desc("b")),
        """{"a": 1, "b": -1}""",
      ),
      suite("raw")(
        testSort("bson", sorts.raw(BsonDocument("a" -> BsonInt32(42))), """{"a": 42}"""),
        testSort("json", sorts.raw("""{"$set": {"a": 42}}"""), """{"$set": {"a": 42}}"""),
      ),
    )
}
