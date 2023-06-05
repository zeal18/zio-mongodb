package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import zio.test.ZIOSpecDefault
import zio.test.*

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
    testAccumulator("last", accumulators.last(expressions.const(4)), """{"$last": 4}"""),
    testAccumulator("max", accumulators.max(expressions.const(5)), """{"$max": 5}"""),
    testAccumulator("min", accumulators.min(expressions.const(6)), """{"$min": 6}"""),
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
  )

}
