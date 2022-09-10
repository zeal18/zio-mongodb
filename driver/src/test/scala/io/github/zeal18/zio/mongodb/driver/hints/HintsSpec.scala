package io.github.zeal18.zio.mongodb.driver.hints

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.driver.hints
import io.github.zeal18.zio.mongodb.driver.indexes
import io.github.zeal18.zio.mongodb.driver.indexes.Index
import zio.test.DefaultRunnableSpec
import zio.test.ZSpec
import zio.test.assertTrue

object HintsSpec extends DefaultRunnableSpec {
  private def testHint(title: String, hint: hints.Hint, expected: String) =
    test(title) {
      hint.toBson match {
        case Left(string)    => assertTrue(string == expected)
        case Right(document) => assertTrue(document.toString() == expected)
      }
    }

  override def spec: ZSpec[Environment, Failure] =
    suite("HintsSpec")(
      testHint("name", hints.indexName("index_name_1"), "index_name_1"),
      testHint("key", hints.indexKey(indexes.asc("indexed-field")), "{\"indexed-field\": 1}"),
      testHint(
        "index",
        hints.index(Index(indexes.desc("indexed-field"))),
        "{\"indexed-field\": -1}",
      ),
      testHint("forwardScan", hints.forwardScan, "{\"$natural\": 1}"),
      testHint("reverseScan", hints.reverseScan, "{\"$natural\": -1}"),
      suite("raw")(
        testHint(
          "bson",
          hints.raw(BsonDocument("index_name" -> BsonInt32(1))),
          """{"index_name": 1}""",
        ),
        testHint("json", hints.raw("""{"index_name": 1}"""), """{"index_name": 1}"""),
      ),
    )
}
