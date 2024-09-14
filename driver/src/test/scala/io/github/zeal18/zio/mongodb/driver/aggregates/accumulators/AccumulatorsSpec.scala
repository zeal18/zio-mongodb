package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import io.github.zeal18.zio.mongodb.driver.sorts
import zio.test.*
import zio.test.ZIOSpecDefault

import scala.annotation.nowarn

@nowarn("msg=possible missing interpolator")
object AccumulatorsSpec extends ZIOSpecDefault {
  private def testAccumulator(
    title: String,
    acc: accumulators.Accumulator,
    expectedValue: String,
  ) =
    test(title) {
      assertTrue(acc.toBsonDocument().toString == expectedValue)
    }

  override def spec = suite("AccumulatorsSpec")(
    testAccumulator("sum", accumulators.sum(expressions.const(1)), """{"$sum": 1}"""),
    testAccumulator("avg", accumulators.avg(expressions.const(2)), """{"$avg": 2}"""),
    testAccumulator("first", accumulators.first(expressions.const(3)), """{"$first": 3}"""),
    testAccumulator(
      "firstN",
      accumulators.firstN(expressions.const(3), expressions.fieldPath("$a")),
      """{"$firstN": {"input": 3, "n": "$a"}}""",
    ),
    testAccumulator("last", accumulators.last(expressions.const(4)), """{"$last": 4}"""),
    testAccumulator(
      "lastN",
      accumulators.lastN(expressions.const(4), expressions.fieldPath("$b")),
      """{"$lastN": {"input": 4, "n": "$b"}}""",
    ),
    testAccumulator("max", accumulators.max(expressions.const(5)), """{"$max": 5}"""),
    testAccumulator(
      "maxN",
      accumulators.maxN(expressions.const(5), expressions.fieldPath("$c")),
      """{"$maxN": {"input": 5, "n": "$c"}}""",
    ),
    testAccumulator("min", accumulators.min(expressions.const(6)), """{"$min": 6}"""),
    testAccumulator(
      "minN",
      accumulators.minN(expressions.const(6), expressions.fieldPath("$d")),
      """{"$minN": {"input": 6, "n": "$d"}}""",
    ),
    testAccumulator("push", accumulators.push(expressions.const(7)), """{"$push": 7}"""),
    testAccumulator(
      "addToSet",
      accumulators.addToSet(expressions.const(8)),
      """{"$addToSet": 8}""",
    ),
    testAccumulator(
      "mergeObjects",
      accumulators.mergeObjects(expressions.const(9)),
      """{"$mergeObjects": 9}""",
    ),
    testAccumulator(
      "stdDevPop",
      accumulators.stdDevPop(expressions.const(1)),
      """{"$stdDevPop": 1}""",
    ),
    testAccumulator(
      "stdDevSamp",
      accumulators.stdDevSamp(expressions.const(2)),
      """{"$stdDevSamp": 2}""",
    ),
    testAccumulator(
      "accumulator",
      accumulators.accumulator(
        initFunction = "initF",
        initArgs = Some(expressions.const(Seq("initArg"))),
        accumulateFunction = "accF",
        accumulateArgs = Some(expressions.const(Seq("accArg"))),
        mergeFunction = "mergeF",
        finalizeFunction = Some("finalizeF"),
        lang = "lang",
      ),
      """{"$accumulator": {"init": "initF", "initArgs": ["initArg"], "accumulate": "accF", "accumulateArgs": ["accArg"], "merge": "mergeF", "finalize": "finalizeF", "lang": "lang"}}""",
    ),
    testAccumulator(
      "bottom",
      accumulators.bottom(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
      ),
      """{"$bottom": {"sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testAccumulator(
      "bottomN",
      accumulators.bottomN(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
        n = expressions.const(10),
      ),
      """{"$bottom": {"n": 10, "sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testAccumulator(
      "top",
      accumulators.top(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
      ),
      """{"$top": {"sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testAccumulator(
      "topN",
      accumulators.topN(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
        n = expressions.const(10),
      ),
      """{"$topN": {"n": 10, "sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testAccumulator("count", accumulators.count(), """{"$count": {}}"""),
    suite("raw")(
      testAccumulator("bson", accumulators.raw(BsonDocument("a" -> 1)), """{"a": 1}"""),
      testAccumulator("json", accumulators.raw("""{"a": 1}"""), """{"a": 1}"""),
    ),
  )

}
