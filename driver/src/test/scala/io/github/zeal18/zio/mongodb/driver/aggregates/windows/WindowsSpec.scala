package io.github.zeal18.zio.mongodb.driver.aggregates.windows

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import io.github.zeal18.zio.mongodb.driver.aggregates.windows
import io.github.zeal18.zio.mongodb.driver.sorts
import zio.test.*
import zio.test.ZIOSpecDefault

import scala.annotation.nowarn

@nowarn("msg=possible missing interpolator")
object WindowsSpec extends ZIOSpecDefault {
  private def testWindow(
    title: String,
    win: windows.Window,
    expectedValue: String,
  ) =
    test(title) {
      assertTrue(win.toBsonDocument().toString == expectedValue)
    }

  override def spec = suite("WindowsSpec")(
    testWindow("sum", windows.sum(expressions.const(1)), """{"$sum": 1}"""),
    testWindow("avg", windows.avg(expressions.const(2)), """{"$avg": 2}"""),
    testWindow("first", windows.first(expressions.const(3)), """{"$first": 3}"""),
    testWindow(
      "firstN",
      windows.firstN(expressions.const(3), expressions.fieldPath("$a")),
      """{"$firstN": {"input": 3, "n": "$a"}}""",
    ),
    testWindow("last", windows.last(expressions.const(4)), """{"$last": 4}"""),
    testWindow(
      "lastN",
      windows.lastN(expressions.const(4), expressions.fieldPath("$b")),
      """{"$lastN": {"input": 4, "n": "$b"}}""",
    ),
    testWindow("max", windows.max(expressions.const(5)), """{"$max": 5}"""),
    testWindow(
      "maxN",
      windows.maxN(expressions.const(5), expressions.fieldPath("$c")),
      """{"$maxN": {"input": 5, "n": "$c"}}""",
    ),
    testWindow("min", windows.min(expressions.const(6)), """{"$min": 6}"""),
    testWindow(
      "minN",
      windows.minN(expressions.const(6), expressions.fieldPath("$d")),
      """{"$minN": {"input": 6, "n": "$d"}}""",
    ),
    testWindow("push", windows.push(expressions.const(7)), """{"$push": 7}"""),
    testWindow(
      "addToSet",
      windows.addToSet(expressions.const(8)),
      """{"$addToSet": 8}""",
    ),
    testWindow(
      "stdDevPop",
      windows.stdDevPop(expressions.const(1)),
      """{"$stdDevPop": 1}""",
    ),
    testWindow(
      "stdDevSamp",
      windows.stdDevSamp(expressions.const(2)),
      """{"$stdDevSamp": 2}""",
    ),
    testWindow(
      "bottom",
      windows.bottom(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
      ),
      """{"$bottom": {"sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testWindow(
      "bottomN",
      windows.bottomN(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
        n = expressions.const(10),
      ),
      """{"$bottom": {"n": 10, "sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testWindow(
      "top",
      windows.top(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
      ),
      """{"$top": {"sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testWindow(
      "topN",
      windows.topN(
        sortBy = sorts.compound(sorts.desc("a"), sorts.asc("b")),
        output = expressions.const(Seq("$playerId", "$score")),
        n = expressions.const(10),
      ),
      """{"$topN": {"n": 10, "sortBy": {"a": -1, "b": 1}, "output": ["$playerId", "$score"]}}""",
    ),
    testWindow("count", windows.count(), """{"$count": {}}"""),
    testWindow(
      "covariancePop",
      windows.covariancePop(expressions.fieldPath("$a"), expressions.fieldPath("$b")),
      """{"$covariancePop": ["$a", "$b"]}""",
    ),
    testWindow(
      "covarianceSamp",
      windows.covarianceSamp(expressions.fieldPath("$a"), expressions.fieldPath("$b")),
      """{"$covarianceSamp": ["$a", "$b"]}""",
    ),
    testWindow("denceRank", windows.denseRank(), """{"$denseRank": {}}"""),
    testWindow(
      "derivative",
      windows.derivative(input = expressions.fieldPath("$a"), unit = Some(Window.TimeUnit.Hour)),
      """{"$derivative": {"input": "$a", "unit": "hour"}}""",
    ),
    testWindow("documentNumber", windows.documentNumber(), """{"$documentNumber": {}}"""),
    testWindow(
      "expMovingAvgN",
      windows.expMovingAvg(input = expressions.fieldPath("$a"), n = 10),
      """{"$expMovingAvg": {"input": "$a", "N": 10}}""",
    ),
    testWindow(
      "expMovingAvgAlpha",
      windows.expMovingAvg(input = expressions.fieldPath("$a"), alpha = 0.5),
      """{"$expMovingAvg": {"input": "$a", "alpha": 0.5}}""",
    ),
    testWindow(
      "integral",
      windows.integral(input = expressions.fieldPath("$a"), unit = Some(Window.TimeUnit.Hour)),
      """{"$integral": {"input": "$a", "unit": "hour"}}""",
    ),
    testWindow(
      "linearFill",
      windows.linearFill(expressions.fieldPath("$a")),
      """{"$linearFill": "$a"}""",
    ),
    testWindow("locf", windows.locf(expressions.fieldPath("$a")), """{"$locf": "$a"}"""),
    testWindow("rank", windows.rank(), """{"$rank": {}}"""),
    testWindow(
      "shift",
      windows.shift(
        output = expressions.fieldPath("$a"),
        by = 3,
        default = Some(expressions.const("b")),
      ),
      """{"$shift": {"output": "$a", "by": 3, "default": "b"}}""",
    ),
    suite("raw")(
      testWindow("bson", windows.raw(BsonDocument("a" -> 1)), """{"a": 1}"""),
      testWindow("json", windows.raw("""{"a": 1}"""), """{"a": 1}"""),
    ),
  )
}
