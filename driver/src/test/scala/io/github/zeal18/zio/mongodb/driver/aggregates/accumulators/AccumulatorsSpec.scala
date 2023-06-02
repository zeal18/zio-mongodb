package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import zio.test.ZIOSpecDefault
import zio.test.*

object AccumulatorsSpec extends ZIOSpecDefault {
  private def testAccumulator(
    title: String,
    acc: accumulators.Accumulator,
    expectedName: String,
    expectedValue: String,
  ) =
    test(title) {
      assertTrue(
        acc.toBsonField.getName == expectedName,
        acc.toBsonField.getValue.toBsonDocument.toString == expectedValue,
      )
    }

  override def spec = suite("AccumulatorsSpec")(
    testAccumulator("sum", accumulators.sum("a", expressions.const(1)), "a", """{"$sum": 1}"""),
    testAccumulator("avg", accumulators.avg("a", expressions.const(2)), "a", """{"$avg": 2}"""),
    testAccumulator(
      "first",
      accumulators.first("a", expressions.const(3)),
      "a",
      """{"$first": 3}""",
    ),
    testAccumulator("last", accumulators.last("a", expressions.const(4)), "a", """{"$last": 4}"""),
    testAccumulator("max", accumulators.max("a", expressions.const(5)), "a", """{"$max": 5}"""),
    testAccumulator("min", accumulators.min("a", expressions.const(6)), "a", """{"$min": 6}"""),
    testAccumulator("push", accumulators.push("a", expressions.const(7)), "a", """{"$push": 7}"""),
    testAccumulator(
      "addToSet",
      accumulators.addToSet("a", expressions.const(8)),
      "a",
      """{"$addToSet": 8}""",
    ),
    testAccumulator(
      "mergeObjects",
      accumulators.mergeObjects("a", expressions.const(9)),
      "a",
      """{"$mergeObjects": 9}""",
    ),
    testAccumulator(
      "stdDevPop",
      accumulators.stdDevPop("a", expressions.const(1)),
      "a",
      """{"$stdDevPop": 1}""",
    ),
    testAccumulator(
      "stdDevSamp",
      accumulators.stdDevSamp("a", expressions.const(2)),
      "a",
      """{"$stdDevSamp": 2}""",
    ),
    testAccumulator(
      "accumulator",
      accumulators.accumulator(
        fieldName = "a",
        initFunction = "initF",
        initArgs = Some(expressions.const(Seq("initArg"))),
        accumulateFunction = "accF",
        accumulateArgs = Some(expressions.const(Seq("accArg"))),
        mergeFunction = "mergeF",
        finalizeFunction = Some("finalizeF"),
        lang = "lang",
      ),
      "a",
      """{"$accumulator": {"init": "initF", "initArgs": ["initArg"], "accumulate": "accF", "accumulateArgs": ["accArg"], "merge": "mergeF", "finalize": "finalizeF", "lang": "lang"}}""",
    ),
  )

}
