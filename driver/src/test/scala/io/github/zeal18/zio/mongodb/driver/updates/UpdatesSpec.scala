package io.github.zeal18.zio.mongodb.driver.updates

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.driver.filters
import io.github.zeal18.zio.mongodb.driver.sorts
import io.github.zeal18.zio.mongodb.driver.updates
import zio.test.DefaultRunnableSpec
import zio.test.ZSpec
import zio.test.assertTrue

object UpdatesSpec extends DefaultRunnableSpec {
  private def testUpdate(title: String, update: updates.Update, expected: String) =
    test(title) {
      assertTrue(update.toBson.toBsonDocument.toString == expected)
    }

  override def spec: ZSpec[Environment, Failure] =
    suite("UpdatesSpec")(
      testUpdate(
        "combine",
        updates.combine(updates.set("a", 1), updates.set("b", 2)),
        """{"$set": {"a": 1, "b": 2}}""",
      ),
      testUpdate(
        "set",
        updates.set("a", 1),
        """{"$set": {"a": 1}}""",
      ),
      testUpdate(
        "unset",
        updates.unset("a"),
        """{"$unset": {"a": ""}}""",
      ),
      testUpdate("setOnInsert", updates.setOnInsert("a", 1), """{"$setOnInsert": {"a": 1}}"""),
      testUpdate("rename", updates.rename("a", "b"), """{"$rename": {"a": "b"}}"""),
      suite("inc")(
        testUpdate("inc int", updates.inc("a", 1), """{"$inc": {"a": 1}}"""),
        testUpdate("inc long", updates.inc("a", 1L), """{"$inc": {"a": 1}}"""),
        testUpdate("inc double", updates.inc("a", 1.0), """{"$inc": {"a": 1.0}}"""),
      ),
      suite("mul")(
        testUpdate("mul int", updates.mul("a", 1), """{"$mul": {"a": 1}}"""),
        testUpdate("mul long", updates.mul("a", 1L), """{"$mul": {"a": 1}}"""),
        testUpdate("mul double", updates.mul("a", 1.0), """{"$mul": {"a": 1.0}}"""),
      ),
      testUpdate("min", updates.min("a", 1), """{"$min": {"a": 1}}"""),
      testUpdate("max", updates.max("a", 1), """{"$max": {"a": 1}}"""),
      testUpdate("currentDate", updates.currentDate("a"), """{"$currentDate": {"a": true}}"""),
      testUpdate(
        "currentTimestamp",
        updates.currentTimestamp("a"),
        """{"$currentDate": {"a": {"$type": "timestamp"}}}""",
      ),
      testUpdate("addToSet", updates.addToSet("a", 1), """{"$addToSet": {"a": 1}}"""),
      testUpdate(
        "addEachToSet",
        updates.addEachToSet("a", Seq(1, 2)),
        """{"$addToSet": {"a": {"$each": [1, 2]}}}""",
      ),
      testUpdate("push", updates.push("a", 1), """{"$push": {"a": 1}}"""),
      suite("pushEach")(
        testUpdate(
          "without options",
          updates.pushEach("a", Seq(1, 2)),
          """{"$push": {"a": {"$each": [1, 2]}}}""",
        ),
        testUpdate(
          "with non-document sorting options",
          updates
            .pushEach("a", Seq(1, 2, 4), updates.PushOptions(Some(1), Some(2), Some(Left(false)))),
          """{"$push": {"a": {"$each": [1, 2, 4], "$position": 1, "$slice": 2, "$sort": -1}}}""",
        ),
        testUpdate(
          "with document sorting options",
          updates.pushEach(
            "a",
            Seq(1, 2, 4),
            updates.PushOptions(Some(5), Some(6), Some(Right(sorts.desc("a")))),
          ),
          """{"$push": {"a": {"$each": [1, 2, 4], "$position": 5, "$slice": 6, "$sort": {"a": -1}}}}""",
        ),
      ),
      testUpdate("pull", updates.pull("a", 1), """{"$pull": {"a": 1}}"""),
      testUpdate(
        "pullByFilter",
        updates.pullByFilter(filters.eq("b", 1)),
        """{"$pull": {"b": 1}}""",
      ),
      suite("pullAll")(
        testUpdate(
          "pullAll seq",
          updates.pullAll("a", Seq(1, 2)),
          """{"$pullAll": {"a": [1, 2]}}""",
        ),
        testUpdate(
          "pullAll set",
          updates.pullAll("a", Set(1, 2)),
          """{"$pullAll": {"a": [1, 2]}}""",
        ),
      ),
      testUpdate("popFirst", updates.popFirst("a"), """{"$pop": {"a": -1}}"""),
      testUpdate("popLast", updates.popLast("a"), """{"$pop": {"a": 1}}"""),
      suite("bitwise")(
        testUpdate("and int", updates.bitwiseAnd("a", 1), """{"$bit": {"a": {"and": 1}}}"""),
        testUpdate("and long", updates.bitwiseAnd("a", 1L), """{"$bit": {"a": {"and": 1}}}"""),
        testUpdate("or int", updates.bitwiseOr("a", 1), """{"$bit": {"a": {"or": 1}}}"""),
        testUpdate("or long", updates.bitwiseOr("a", 1L), """{"$bit": {"a": {"or": 1}}}"""),
        testUpdate("xor int", updates.bitwiseXor("a", 1), """{"$bit": {"a": {"xor": 1}}}"""),
        testUpdate("xor long", updates.bitwiseXor("a", 1L), """{"$bit": {"a": {"xor": 1}}}"""),
      ),
      suite("raw")(
        testUpdate(
          "bson",
          updates.raw(BsonDocument("$set" -> BsonDocument("a" -> BsonInt32(42)))),
          """{"$set": {"a": 42}}""",
        ),
        testUpdate("json", updates.raw("""{"$set": {"a": 42}}"""), """{"$set": {"a": 42}}"""),
      ),
    )
}
