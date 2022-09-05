package io.github.zeal18.zio.mongodb.driver.filters

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonString
import io.github.zeal18.zio.mongodb.driver.filters
import org.bson.BsonType
import zio.test.ZIOSpecDefault
import zio.test.assertTrue

object FiltersSpec extends ZIOSpecDefault {
  private def testFilter(title: String, filter: filters.Filter, expected: String) =
    test(title) {
      assertTrue(filter.toBson.toBsonDocument.toString == expected)
    }

  override def spec = suite("FiltersSpec")(
    suite("equal")(
      testFilter("eq _id", filters.eq(42), """{"_id": 42}"""),
      testFilter("eq field", filters.eq("field", 42), """{"field": 42}"""),
      testFilter("equal _id", filters.equal(42), """{"_id": 42}"""),
      testFilter("equal field", filters.equal("field", 42), """{"field": 42}"""),
    ),
    suite("notEqual")(
      testFilter("ne", filters.ne("field", 42), """{"field": {"$ne": 42}}"""),
      testFilter("notEqual", filters.notEqual("field", 42), """{"field": {"$ne": 42}}"""),
    ),
    suite("gt/gte")(
      testFilter("gt", filters.gt("field-gt", 43), """{"field-gt": {"$gt": 43}}"""),
      testFilter("gte", filters.gte("field-gte", 44), """{"field-gte": {"$gte": 44}}"""),
    ),
    suite("lt/lte")(
      testFilter("lt", filters.lt("field-lt", 45), """{"field-lt": {"$lt": 45}}"""),
      testFilter("lte", filters.lte("field-lte", 46), """{"field-lte": {"$lte": 46}}"""),
    ),
    suite("in")(
      testFilter(
        "in seq",
        filters.in("field-seq", Seq(47, 48)),
        """{"field-seq": {"$in": [47, 48]}}""",
      ),
      testFilter(
        "in set",
        filters.in("field-set", Set(49, 50)),
        """{"field-set": {"$in": [49, 50]}}""",
      ),
    ),
    suite("nin")(
      testFilter(
        "nin seq",
        filters.nin("field-seq", Seq(47, 48)),
        """{"field-seq": {"$nin": [47, 48]}}""",
      ),
      testFilter(
        "nin set",
        filters.nin("field-set", Set(49, 50)),
        """{"field-set": {"$nin": [49, 50]}}""",
      ),
    ),
    suite("and")(
      testFilter(
        "and set",
        filters.and(
          Set[Filter](
            filters.eq("field-and-1", 51),
            filters.eq("field-and-2", 52),
          ),
        ),
        """{"$and": [{"field-and-1": 51}, {"field-and-2": 52}]}""",
      ),
      testFilter(
        "and varargs",
        filters.and(filters.eq("field-and-1", 51), filters.eq("field-and-2", 52)),
        """{"$and": [{"field-and-1": 51}, {"field-and-2": 52}]}""",
      ),
    ),
    suite("or")(
      testFilter(
        "or set",
        filters.or(
          Set[Filter](
            filters.eq("field-or-1", 51),
            filters.eq("field-or-2", 52),
          ),
        ),
        """{"$or": [{"field-or-1": 51}, {"field-or-2": 52}]}""",
      ),
      testFilter(
        "or varargs",
        filters.or(filters.eq("field-or-1", 51), filters.eq("field-or-2", 52)),
        """{"$or": [{"field-or-1": 51}, {"field-or-2": 52}]}""",
      ),
    ),
    suite("nor")(
      testFilter(
        "nor set",
        filters.nor(
          Set[Filter](
            filters.eq("field-nor-1", 51),
            filters.eq("field-nor-2", 52),
          ),
        ),
        """{"$nor": [{"field-nor-1": 51}, {"field-nor-2": 52}]}""",
      ),
      testFilter(
        "nor varargs",
        filters.nor(filters.eq("field-nor-1", 51), filters.eq("field-nor-2", 52)),
        """{"$nor": [{"field-nor-1": 51}, {"field-nor-2": 52}]}""",
      ),
    ),
    testFilter(
      "not",
      filters.not(filters.eq("field-not", 53)),
      """{"field-not": {"$not": {"$eq": 53}}}""",
    ),
    suite("exists")(
      testFilter(
        "exists",
        filters.exists("field-exists"),
        """{"field-exists": {"$exists": true}}""",
      ),
      testFilter(
        "exists true",
        filters.exists("field-exists", true),
        """{"field-exists": {"$exists": true}}""",
      ),
      testFilter(
        "exists false",
        filters.exists("field-exists", false),
        """{"field-exists": {"$exists": false}}""",
      ),
    ),
    suite("type")(
      testFilter(
        "type",
        filters.`type`("field-type", BsonType.DECIMAL128),
        """{"field-type": {"$type": 19}}""",
      ),
    ),
    testFilter(
      "mod",
      filters.mod("field-mod", 54, 55),
      """{"field-mod": {"$mod": [54, 55]}}""",
    ),
    suite("regex")(
      testFilter(
        "regex",
        filters.regex("field-regex", "pattern"),
        """{"field-regex": {"$regularExpression": {"pattern": "pattern", "options": ""}}}""",
      ),
      testFilter(
        "regex flags",
        filters.regex("field-regex", "pattern", "flags"),
        """{"field-regex": {"$regularExpression": {"pattern": "pattern", "options": "afgls"}}}""",
      ),
    ),
    suite("text")(
      testFilter(
        "text",
        filters.text("query"),
        """{"$text": {"$search": "query"}}""",
      ),
      testFilter(
        "text sensitive",
        filters.text("query", "language", true, true),
        """{"$text": {"$search": "query", "$language": "language", "$caseSensitive": true, "$diacriticSensitive": true}}""",
      ),
      testFilter(
        "text insensitive",
        filters.text("query", "language", false, false),
        """{"$text": {"$search": "query", "$language": "language", "$caseSensitive": false, "$diacriticSensitive": false}}""",
      ),
    ),
    testFilter(
      "where",
      filters.where("expression"),
      """{"$where": "expression"}""",
    ),
    testFilter("expr", filters.expr("expression"), """{"$expr": "expression"}"""),
    suite("all")(
      testFilter(
        "all set",
        filters.all("field-all", Set(60, 61)),
        """{"field-all": {"$all": [60, 61]}}""",
      ),
      testFilter(
        "all seq",
        filters.all("field-all", Seq(62, 63)),
        """{"field-all": {"$all": [62, 63]}}""",
      ),
    ),
    testFilter(
      "elemMatch",
      filters.elemMatch("field-elemMatch", filters.eq("elem-field", 64)),
      """{"field-elemMatch": {"$elemMatch": {"elem-field": 64}}}""",
    ),
    testFilter("size", filters.size("field-size", 65), """{"field-size": {"$size": 65}}"""),
    suite("bits")(
      testFilter(
        "bitsAllSet",
        filters.bitsAllSet("field-bitsAllSet", 66),
        """{"field-bitsAllSet": {"$bitsAllSet": 66}}""",
      ),
      testFilter(
        "bitsAllClear",
        filters.bitsAllClear("field-bitsAllClear", 67),
        """{"field-bitsAllClear": {"$bitsAllClear": 67}}""",
      ),
      testFilter(
        "bitsAnySet",
        filters.bitsAnySet("field-bitsAnySet", 68),
        """{"field-bitsAnySet": {"$bitsAnySet": 68}}""",
      ),
      testFilter(
        "bitsAnyClear",
        filters.bitsAnyClear("field-bitsAnyClear", 67),
        """{"field-bitsAnyClear": {"$bitsAnyClear": 67}}""",
      ),
    ),
    testFilter(
      "jsonSchema",
      filters.jsonSchema(BsonDocument("dummy" -> BsonString("schema"))),
      """{"$jsonSchema": {"dummy": "schema"}}""",
    ),
  )
}
