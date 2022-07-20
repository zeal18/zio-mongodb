package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
import zio.test.*

object AccumulatorsSpec extends DefaultRunnableSpec {
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

  override def spec: ZSpec[Environment, Failure] = suite("AccumulatorsSpec")(
    testAccumulator("sum", accumulators.sum("a", 1), "a", """{"$sum": 1}"""),
    testAccumulator("avg", accumulators.avg("a", 2), "a", """{"$avg": 2}"""),
    testAccumulator("first", accumulators.first("a", 3), "a", """{"$first": 3}"""),
    testAccumulator("last", accumulators.last("a", 4), "a", """{"$last": 4}"""),
    testAccumulator("max", accumulators.max("a", 5), "a", """{"$max": 5}"""),
    testAccumulator("min", accumulators.min("a", 6), "a", """{"$min": 6}"""),
    testAccumulator("push", accumulators.push("a", 7), "a", """{"$push": 7}"""),
    testAccumulator("addToSet", accumulators.addToSet("a", 8), "a", """{"$addToSet": 8}"""),
    testAccumulator(
      "mergeObjects",
      accumulators.mergeObjects("a", 9),
      "a",
      """{"$mergeObjects": 9}""",
    ),
    testAccumulator("stdDevPop", accumulators.stdDevPop("a", 1), "a", """{"$stdDevPop": 1}"""),
    testAccumulator("stdDevSamp", accumulators.stdDevSamp("a", 2), "a", """{"$stdDevSamp": 2}"""),
    testAccumulator(
      "accumulator",
      accumulators.accumulator(
        fieldName = "a",
        initFunction = "initF",
        initArgs = Seq("initArg"),
        accumulateFunction = "accF",
        accumulateArgs = Seq("accArg"),
        mergeFunction = "mergeF",
        finalizeFunction = Some("finalizeF"),
        lang = "lang",
      ),
      "a",
      """{"$accumulator": {"init": "initF", "initArgs": ["initArg"], "accumulate": "accF", "accumulateArgs": ["accArg"], "merge": "mergeF", "lang": "lang", "finalize": "finalizeF"}}""",
    ),
  )

}
