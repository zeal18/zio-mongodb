package io.github.zeal18.zio.mongodb.driver.aggregates

import scala.annotation.nowarn

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.driver.aggregates
import io.github.zeal18.zio.mongodb.driver.aggregates.UnwindOptions
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
import io.github.zeal18.zio.mongodb.driver.filters
import io.github.zeal18.zio.mongodb.driver.projections
import zio.test.*

object AggregatesSpec extends DefaultRunnableSpec {
  private def testAggregate(
    name: String,
    aggregate: aggregates.Aggregation,
    expected: String,
  ) =
    test(name) {
      assertTrue(aggregate.toBsonDocument().toString == expected)
    }

  @nowarn("msg=possible missing interpolator")
  override def spec: ZSpec[Environment, Failure] = suite("AggregatesSpec")(
    testAggregate("count", aggregates.count(), """{"$count": "count"}"""),
    testAggregate("count with field", aggregates.count("field"), """{"$count": "field"}"""),
    testAggregate(
      "match",
      aggregates.`match`(filters.eq(1.4)),
      """{"$match": {"_id": 1.4}}""",
    ),
    testAggregate("filter", aggregates.filter(filters.eq("a", 8)), """{"$match": {"a": 8}}"""),
    testAggregate(
      "facet",
      aggregates
        .facet(Facet("a", aggregates.count()), Facet("b", aggregates.filter(filters.eq(42)))),
      """{"$facet": {"a": [{"$count": "count"}], "b": [{"$match": {"_id": 42}}]}}""",
    ),
    testAggregate("limit", aggregates.limit(43), """{"$limit": 43}"""),
    testAggregate(
      "group",
      aggregates.group("a", accumulators.sum("b", 1)),
      """{"$group": {"_id": "a", "b": {"$sum": 1}}}""",
    ),
    testAggregate(
      "lookup",
      aggregates.lookup("a", "b", "c", "d"),
      """{"$lookup": {"from": "a", "localField": "b", "foreignField": "c", "as": "d"}}""",
    ),
    testAggregate(
      "lookup join",
      aggregates.lookup("a", Seq(Variable("var", "value")), Seq(aggregates.limit(2)), "d"),
      """{"$lookup": {"from": "a", "let": {"var": "value"}, "pipeline": [{"$limit": 2}], "as": "d"}}""",
    ),
    testAggregate(
      "project",
      aggregates.project(projections.include("a")),
      """{"$project": {"a": 1}}""",
    ),
    testAggregate("unwind", aggregates.unwind("a"), """{"$unwind": {"path": "a"}}"""),
    testAggregate(
      "unwind with options",
      aggregates.unwind(
        "a",
        UnwindOptions(Some(true), Some("b")),
      ),
      """{"$unwind": {"path": "a", "preserveNullAndEmptyArrays": true, "includeArrayIndex": "b"}}""",
    ),
    suite("raw")(
      testAggregate(
        "bson",
        aggregates.raw(BsonDocument("$match" -> BsonDocument("c" -> 42))),
        """{"$match": {"c": 42}}""",
      ),
      testAggregate(
        "json",
        aggregates.raw("""{"$match": {"c": 42}}"""),
        """{"$match": {"c": 42}}""",
      ),
    ),
  )
}
