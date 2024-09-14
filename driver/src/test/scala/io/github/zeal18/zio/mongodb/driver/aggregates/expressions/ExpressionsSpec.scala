package io.github.zeal18.zio.mongodb.driver.aggregates.expressions

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.*
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression.*
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonDocumentWriter
import zio.test.ZIOSpecDefault
import zio.test.assertTrue

import scala.annotation.nowarn

@nowarn("msg=possible missing interpolator")
object ExpressionsSpec extends ZIOSpecDefault {
  private def testExpr(title: String, expr: Expression, expected: String) =
    test(title) {
      val writer = new BsonDocumentWriter(new BsonDocument())
      writer.writeStartDocument()
      writer.writeName("_t")
      expr.encode(writer)
      writer.writeEndDocument()

      assertTrue(writer.getDocument.toString == expected)
    }
  private def testBson(title: String, expr: Bson, expected: String) =
    test(title) {
      assertTrue(expr.toBsonDocument().toString == expected)
    }

  override def spec = suite("ExpressionsSpec")(
    testExpr("field path", fieldPath("$a.b"), """{"_t": "$a.b"}"""),
    suite("literal")(
      testBson("string", literal("a.b"), """{"$literal": "a.b"}"""),
      testBson("int", literal(34), """{"$literal": 34}"""),
      testBson("double", literal(34.5), """{"$literal": 34.5}"""),
    ),
    testBson(
      "expression object",
      obj(
        "a" -> literal(1),
        "b" -> const(true),
      ),
      """{"a": {"$literal": 1}, "b": true}""",
    ),
    suite("raw")(
      testBson("bson", raw(BsonDocument("a" -> 1)), """{"a": 1}"""),
      testBson("json", raw("""{"a": 1}"""), """{"a": 1}"""),
    ),
    suite("operator")(
      suite("arithmetic")(
        testBson("abs", abs(const(-1)), """{"$abs": -1}"""),
        testBson("add", add(const(1), const(2)), """{"$add": [1, 2]}"""),
        testBson("ceil", ceil(const(1.5)), """{"$ceil": 1.5}"""),
        testBson("divide", divide(const(1), const(2)), """{"$divide": [1, 2]}"""),
        testBson("exp", exp(const(1)), """{"$exp": 1}"""),
        testBson("floor", floor(const(1.5)), """{"$floor": 1.5}"""),
        testBson("ln", ln(const(1)), """{"$ln": 1}"""),
        testBson("log", log(const(1), const(4)), """{"$log": [1, 4]}"""),
        testBson("log10", log10(const(1)), """{"$log10": 1}"""),
        testBson("mod", mod(const(1), const(2)), """{"$mod": [1, 2]}"""),
        testBson("multiply", multiply(const(1), const(2)), """{"$multiply": [1, 2]}"""),
        testBson("pow", pow(const(1), const(2)), """{"$pow": [1, 2]}"""),
        suite("round")(
          testBson("without place", round(const(1.5)), """{"$round": 1.5}"""),
          testBson("with place", round(const(1.5), Some(const(1))), """{"$round": [1.5, 1]}"""),
        ),
        testBson("sqrt", sqrt(const(1)), """{"$sqrt": 1}"""),
        testBson("subtract", subtract(const(1), const(2)), """{"$subtract": [1, 2]}"""),
        suite("trunc")(
          testBson("without place", trunc(const(1.5)), """{"$trunc": 1.5}"""),
          testBson("with place", trunc(const(1.5), Some(const(1))), """{"$trunc": [1.5, 1]}"""),
        ),
      ),
      suite("array")(
        testBson(
          "arrayElemAt",
          arrayElemAt(const(Seq(1, 2, 3)), const(1)),
          """{"$arrayElemAt": [[1, 2, 3], 1]}""",
        ),
        testBson(
          "arrayToObject",
          arrayToObject(fieldPath("$a")),
          """{"$arrayToObject": "$a"}""",
        ),
        testBson(
          "concatArrays",
          concatArrays(const(Seq(1, 2, 3)), const(Seq(4, 5, 6)), fieldPath("$a")),
          """{"$concatArrays": [[1, 2, 3], [4, 5, 6], "$a"]}""",
        ),
        testBson(
          "filter",
          filter(
            input = const(Seq(1, 2, 3)),
            cond = in(const(Seq(1, 2)), fieldPath("$$var")),
            as = Some("var"),
            limit = Some(const(10)),
          ),
          """{"$filter": {"input": [1, 2, 3], "cond": {"$in": ["$$var", [1, 2]]}, "as": "var", "limit": 10}}""",
        ),
        testBson("first", first(const(Seq(1, 2, 3))), """{"$first": [1, 2, 3]}"""),
        testBson(
          "firstN",
          firstN(const(Seq(1, 2, 3)), const(2)),
          """{"$firstN": {"n": 2, "input": [1, 2, 3]}}""",
        ),
        testBson(
          "in",
          in(const(Seq(1, 2)), fieldPath("a")),
          """{"$in": ["a", [1, 2]]}""",
        ),
        testBson(
          "indexOfArray",
          indexOfArray(
            array = const(Seq(1, 2, 3)),
            value = const(4),
            start = Some(const(5)),
            end = Some(const(6)),
          ),
          """{"$indexOfArray": [[1, 2, 3], 4, 5, 6]}""",
        ),
        testBson("isArray", isArray(fieldPath("$a")), """{"$isArray": ["$a"]}"""),
        testBson("last", last(fieldPath("$b")), """{"$last": "$b"}"""),
        testBson(
          "lastN",
          lastN(fieldPath("$b"), const(2)),
          """{"$lastN": {"n": 2, "input": "$b"}}""",
        ),
        testBson(
          "map",
          map(
            input = const(Seq(1, 2, 3)),
            as = Some("var"),
            in = last(fieldPath("$$var")),
          ),
          """{"$map": {"input": [1, 2, 3], "as": "var", "in": {"$last": "$$var"}}}""",
        ),
        testBson(
          "MaxN",
          maxN(fieldPath("$a"), const(2)),
          """{"$maxN": {"n": 2, "input": "$a"}}""",
        ),
        testBson(
          "MinN",
          minN(fieldPath("$a"), const(2)),
          """{"$minN": {"n": 2, "input": "$a"}}""",
        ),
        testBson(
          "objectToArray",
          objectToArray(fieldPath("$a")),
          """{"$objectToArray": "$a"}""",
        ),
        testBson(
          "range",
          range(
            start = const(1),
            end = const(2),
            step = Some(const(3)),
          ),
          """{"$range": [1, 2, 3]}""",
        ),
        testBson(
          "reduce",
          reduce(
            input = const(Seq(1, 2, 3)),
            initialValue = const(0),
            in = fieldPath("$$value"),
          ),
          """{"$reduce": {"input": [1, 2, 3], "initialValue": 0, "in": "$$value"}}""",
        ),
        testBson(
          "reverseArray",
          reverseArray(fieldPath("$a")),
          """{"$reverseArray": "$a"}""",
        ),
        testBson(
          "size",
          size(fieldPath("$a")),
          """{"$size": "$a"}""",
        ),
        testBson(
          "slice",
          slice(
            array = const(Seq(1, 2, 3)),
            position = Some(const(4)),
            n = const(5),
          ),
          """{"$slice": [[1, 2, 3], 4, 5]}""",
        ),
        testBson(
          "sortArrayByField",
          sortArray(fieldPath("$a"), Sort.Asc("b")),
          """{"$sortArray": {"input": "$a", "sortBy": {"b": 1}}}""",
        ),
        testBson(
          "sortArrayByValue",
          sortArray(fieldPath("$a"), asc = false),
          """{"$sortArray": {"input": "$a", "sortBy": -1}}""",
        ),
        testBson(
          "zipShortest",
          zip(Seq(fieldPath("$a"), fieldPath("$b"))),
          """{"$zip": {"inputs": ["$a", "$b"], "useLongestLength": false}}""",
        ),
        testBson(
          "zipLongest",
          zip(Seq(fieldPath("$a"), fieldPath("$b")), fieldPath("$c")),
          """{"$zip": {"inputs": ["$a", "$b"], "useLongestLength": true, "defaults": "$c"}}""",
        ),
      ),
      suite("boolean")(
        testBson(
          "and",
          and(const(true), const(false)),
          """{"$and": [true, false]}""",
        ),
        testBson(
          "not",
          not(const(true)),
          """{"$not": [true]}""",
        ),
        testBson(
          "or",
          or(const(true), const(false)),
          """{"$or": [true, false]}""",
        ),
      ),
      suite("comparison")(
        testBson(
          "cmp",
          cmp(fieldPath("$a"), fieldPath("$b")),
          """{"$cmp": ["$a", "$b"]}""",
        ),
        testBson(
          "eq",
          expressions.eq(fieldPath("$a"), fieldPath("$b")),
          """{"$eq": ["$a", "$b"]}""",
        ),
        testBson(
          "gt",
          gt(fieldPath("$a"), fieldPath("$b")),
          """{"$gt": ["$a", "$b"]}""",
        ),
        testBson(
          "gte",
          gte(fieldPath("$a"), fieldPath("$b")),
          """{"$gte": ["$a", "$b"]}""",
        ),
        testBson(
          "lt",
          lt(fieldPath("$a"), fieldPath("$b")),
          """{"$lt": ["$a", "$b"]}""",
        ),
        testBson(
          "lte",
          lte(fieldPath("$a"), fieldPath("$b")),
          """{"$lte": ["$a", "$b"]}""",
        ),
        testBson(
          "ne",
          expressions.ne(fieldPath("$a"), fieldPath("$b")),
          """{"$ne": ["$a", "$b"]}""",
        ),
      ),
      suite("conditional")(
        testBson(
          "cond",
          cond(
            const(true),
            const(1),
            const(2),
          ),
          """{"$cond": {"if": true, "then": 1, "else": 2}}""",
        ),
        testBson(
          "ifNull",
          ifNull(const(1), fieldPath("$a"), fieldPath("$b")),
          """{"$ifNull": ["$a", "$b", 1]}""",
        ),
        testBson(
          "switch",
          switch(
            branches = Seq(
              const(1) -> const("a"),
              const(2) -> const("b"),
            ),
            default = Some(const("c")),
          ),
          """{"$switch": {"branches": [{"case": 1, "then": "a"}, {"case": 2, "then": "b"}], "default": "c"}}""",
        ),
      ),
      suite("custom")(
        testBson(
          "function",
          function(body = "a", args = Some(const(1))),
          """{"$function": {"body": "a", "args": 1, "lang": "js"}}""",
        ),
      ),
      suite("data size")(
        testBson("binarySize", binarySize(fieldPath("$a")), """{"$binarySize": "$a"}"""),
        testBson("bsonSize", bsonSize(fieldPath("$a")), """{"$bsonSize": "$a"}"""),
      ),
      suite("date")(
        testBson(
          "dateAdd",
          dateAdd(
            startDate = fieldPath("$a"),
            unit = const("hour"),
            amount = const(3),
            timezone = Some(
              const("Europe/London"),
            ),
          ),
          """{"$dateAdd": {"startDate": "$a", "unit": "hour", "amount": 3, "timezone": "Europe/London"}}""",
        ),
        testBson(
          "dateDiff",
          dateDiff(
            startDate = fieldPath("$a"),
            endDate = fieldPath("$b"),
            unit = const("hour"),
            timezone = Some(const("Europe/London")),
            startOfWeek = Some(const("monday")),
          ),
          """{"$dateDiff": {"startDate": "$a", "endDate": "$b", "unit": "hour", "timezone": "Europe/London", "startOfWeek": "monday"}}""",
        ),
        testBson(
          "dateFromParts",
          dateFromParts(
            year = const(2042),
            month = Some(const(3)),
            day = Some(const(14)),
            hour = Some(const(12)),
            minute = Some(const(8)),
            second = Some(const(56)),
            millisecond = Some(const(123)),
            timezone = Some(const("Europe/London")),
          ),
          """{"$dateFromParts": {"year": 2042, "month": 3, "day": 14, "hour": 12, "minute": 8, "second": 56, "millisecond": 123, "timezone": "Europe/London"}}""",
        ),
        testBson(
          "dateFromParts ISO",
          dateFromPartsISO(
            isoWeekYear = const(2042),
            isoWeek = Some(const(3)),
            isoDayOfWeek = Some(const(1)),
            hour = Some(const(12)),
            minute = Some(const(8)),
            second = Some(const(56)),
            millisecond = Some(const(123)),
            timezone = Some(const("Europe/London")),
          ),
          """{"$dateFromParts": {"isoWeekYear": 2042, "isoWeek": 3, "isoDayOfWeek": 1, "hour": 12, "minute": 8, "second": 56, "millisecond": 123, "timezone": "Europe/London"}}""",
        ),
        testBson(
          "dateFromString",
          dateFromString(
            dateString = const("2017-02-08"),
            format = Some(const("%Y-%m-%d")),
            timezone = Some(const("Europe/London")),
            onError = Some(const("error")),
            onNull = Some(const("null")),
          ),
          """{"$dateFromString": {"dateString": "2017-02-08", "format": "%Y-%m-%d", "timezone": "Europe/London", "onError": "error", "onNull": "null"}}""",
        ),
        testBson(
          "dateSubtract",
          dateSubtract(
            startDate = fieldPath("$a"),
            unit = fieldPath("$b"),
            amount = const(3),
            timezone = Some(const("Europe/London")),
          ),
          """{"$dateSubtract": {"startDate": "$a", "unit": "$b", "amount": 3, "timezone": "Europe/London"}}""",
        ),
        testBson(
          "dateToParts",
          dateToParts(
            date = fieldPath("$a"),
            timezone = Some(const("Europe/London")),
            iso8601 = Some(true),
          ),
          """{"$dateToParts": {"date": "$a", "timezone": "Europe/London", "iso8601": true}}""",
        ),
        testBson(
          "dateToString",
          dateToString(
            date = fieldPath("$a"),
            format = "%Y-%m-%d",
            timezone = Some(const("Europe/London")),
            onNull = Some(Variable.Now),
          ),
          """{"$dateToString": {"date": "$a", "format": "%Y-%m-%d", "timezone": "Europe/London", "onNull": "$$NOW"}}""",
        ),
        testBson(
          "dateTrunc",
          dateTrunc(
            date = fieldPath("$a"),
            unit = const("hour"),
            binSize = Some(const(3)),
            timezone = Some(const("Europe/London")),
            startOfWeek = Some(const("monday")),
          ),
          """{"$dateTrunc": {"date": "$a", "unit": "hour", "binSize": 3, "timezone": "Europe/London", "startOfWeek": "monday"}}""",
        ),
        testBson("dayOfMonth", dayOfMonth(fieldPath("$a")), """{"$dayOfMonth": "$a"}"""),
        testBson("dayOfWeek", dayOfWeek(fieldPath("$a")), """{"$dayOfWeek": "$a"}"""),
        testBson("dayOfYear", dayOfYear(fieldPath("$a")), """{"$dayOfYear": "$a"}"""),
        testBson("hour", hour(fieldPath("$a")), """{"$hour": "$a"}"""),
        testBson("isoDayOfWeek", isoDayOfWeek(fieldPath("$a")), """{"$isoDayOfWeek": "$a"}"""),
        testBson("isoWeek", isoWeek(fieldPath("$a")), """{"$isoWeek": "$a"}"""),
        testBson("isoWeekYear", isoWeekYear(fieldPath("$a")), """{"$isoWeekYear": "$a"}"""),
        testBson("millisecond", millisecond(fieldPath("$a")), """{"$millisecond": "$a"}"""),
        testBson("minute", minute(fieldPath("$a")), """{"$minute": "$a"}"""),
        testBson("month", month(fieldPath("$a")), """{"$month": "$a"}"""),
        testBson("second", second(fieldPath("$a")), """{"$second": "$a"}"""),
        testBson("toDate", toDate(fieldPath("$a")), """{"$toDate": "$a"}"""),
        testBson("week", week(fieldPath("$a")), """{"$week": "$a"}"""),
        testBson("year", year(fieldPath("$a")), """{"$year": "$a"}"""),
      ),
      suite("miscellaneous")(
        testBson(
          "getField",
          getField(fieldPath("$a"), Some(fieldPath("$b"))),
          """{"$getField": {"field": "$a", "input": "$b"}}""",
        ),
        testBson("rand", rand, """{"$rand": {}}"""),
        testBson("sampleRate", sampleRate(const(0.5)), """{"$sampleRate": 0.5}"""),
      ),
      suite("object")(
        testBson(
          "mergeObjects",
          mergeObjects(
            fieldPath("$a"),
            fieldPath("$b"),
            fieldPath("$c"),
          ),
          """{"$mergeObjects": ["$a", "$b", "$c"]}""",
        ),
        testBson(
          "setField",
          setField(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$setField": {"field": "$a", "input": "$b", "value": "$c"}}""",
        ),
      ),
      suite("set")(
        testBson(
          "allElementsTrue",
          allElementsTrue(fieldPath("$a")),
          """{"$allElementsTrue": ["$a"]}""",
        ),
        testBson(
          "anyElementTrue",
          anyElementTrue(fieldPath("$a")),
          """{"$anyElementTrue": ["$a"]}""",
        ),
        testBson(
          "setDifference",
          setDifference(fieldPath("$a"), fieldPath("$b")),
          """{"$setDifference": ["$a", "$b"]}""",
        ),
        testBson(
          "setEquals",
          setEquals(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$setEquals": ["$a", "$b", "$c"]}""",
        ),
        testBson(
          "setIntersection",
          setIntersection(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$setIntersection": ["$a", "$b", "$c"]}""",
        ),
        testBson(
          "setIsSubset",
          setIsSubset(fieldPath("$a"), fieldPath("$b")),
          """{"$setIsSubset": ["$a", "$b"]}""",
        ),
        testBson(
          "setUnion",
          setUnion(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$setUnion": ["$a", "$b", "$c"]}""",
        ),
      ),
      suite("string")(
        testBson(
          "concat",
          concat(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$concat": ["$a", "$b", "$c"]}""",
        ),
        testBson(
          "indexOfBytes",
          indexOfBytes(
            fieldPath("$a"),
            fieldPath("$b"),
            Some(const(1)),
            Some(const(2)),
          ),
          """{"$indexOfBytes": ["$a", "$b", 1, 2]}""",
        ),
        testBson(
          "indexOfCP",
          indexOfCP(
            fieldPath("$a"),
            fieldPath("$b"),
            Some(const(1)),
            Some(const(2)),
          ),
          """{"$indexOfCP": ["$a", "$b", 1, 2]}""",
        ),
        testBson(
          "ltrim",
          lTrim(fieldPath("$a"), Some(fieldPath("$b"))),
          """{"$ltrim": {"input": "$a", "chars": "$b"}}""",
        ),
        testBson(
          "regexFind",
          regexFind(fieldPath("$a"), fieldPath("$b"), Some(const(1))),
          """{"$regexFind": {"input": "$a", "regex": "$b", "options": 1}}""",
        ),
        testBson(
          "regexFindAll",
          regexFindAll(fieldPath("$a"), fieldPath("$b"), Some(const(1))),
          """{"$regexFindAll": {"input": "$a", "regex": "$b", "options": 1}}""",
        ),
        testBson(
          "regexMatch",
          regexMatch(fieldPath("$a"), fieldPath("$b"), Some(const(1))),
          """{"$regexMatch": {"input": "$a", "regex": "$b", "options": 1}}""",
        ),
        testBson(
          "replaceOne",
          replaceOne(
            fieldPath("$a"),
            fieldPath("$b"),
            fieldPath("$c"),
          ),
          """{"$replaceOne": {"input": "$a", "find": "$b", "replacement": "$c"}}""",
        ),
        testBson(
          "replaceAll",
          replaceAll(
            fieldPath("$a"),
            fieldPath("$b"),
            fieldPath("$c"),
          ),
          """{"$replaceAll": {"input": "$a", "find": "$b", "replacement": "$c"}}""",
        ),
        testBson(
          "rtrim",
          rTrim(fieldPath("$a"), Some(fieldPath("$b"))),
          """{"$rtrim": {"input": "$a", "chars": "$b"}}""",
        ),
        testBson(
          "split",
          split(fieldPath("$a"), fieldPath("$b")),
          """{"$split": ["$a", "$b"]}""",
        ),
        testBson(
          "strLenBytes",
          strLenBytes(fieldPath("$a")),
          """{"$strLenBytes": "$a"}""",
        ),
        testBson(
          "strLenCP",
          strLenCP(fieldPath("$a")),
          """{"$strLenCP": "$a"}""",
        ),
        testBson(
          "strcasecmp",
          strCaseCmp(fieldPath("$a"), fieldPath("$b")),
          """{"$strcasecmp": ["$a", "$b"]}""",
        ),
        testBson(
          "substrBytes",
          substrBytes(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$substrBytes": ["$a", "$b", "$c"]}""",
        ),
        testBson(
          "substrCP",
          substrCP(fieldPath("$a"), fieldPath("$b"), fieldPath("$c")),
          """{"$substrCP": ["$a", "$b", "$c"]}""",
        ),
        testBson(
          "toLower",
          toLower(fieldPath("$a")),
          """{"$toLower": "$a"}""",
        ),
        testBson("toString", expressions.toString(fieldPath("$a")), """{"$toString": "$a"}"""),
        testBson(
          "trim",
          trim(fieldPath("$a"), Some(fieldPath("$b"))),
          """{"$trim": {"input": "$a", "chars": "$b"}}""",
        ),
        testBson(
          "toUpper",
          toUpper(fieldPath("$a")),
          """{"$toUpper": "$a"}""",
        ),
      ),
      suite("text")(
        testBson(
          "meta",
          meta(fieldPath("$a")),
          """{"$meta": "$a"}""",
        ),
      ),
      suite("trigonometry")(
        testBson("sin", sin(fieldPath("$a")), """{"$sin": "$a"}"""),
        testBson("cos", cos(fieldPath("$a")), """{"$cos": "$a"}"""),
        testBson("tan", tan(fieldPath("$a")), """{"$tan": "$a"}"""),
        testBson("asin", asin(fieldPath("$a")), """{"$asin": "$a"}"""),
        testBson("acos", acos(fieldPath("$a")), """{"$acos": "$a"}"""),
        testBson("atan", atan(fieldPath("$a")), """{"$atan": "$a"}"""),
        testBson(
          "atan2",
          atan2(fieldPath("$a"), fieldPath("$b")),
          """{"$atan2": ["$a", "$b"]}""",
        ),
        testBson("asinh", asinh(fieldPath("$a")), """{"$asinh": "$a"}"""),
        testBson("acosh", acosh(fieldPath("$a")), """{"$acosh": "$a"}"""),
        testBson("atanh", atanh(fieldPath("$a")), """{"$atanh": "$a"}"""),
        testBson("sinh", sinh(fieldPath("$a")), """{"$sinh": "$a"}"""),
        testBson("cosh", cosh(fieldPath("$a")), """{"$cosh": "$a"}"""),
        testBson("tanh", tanh(fieldPath("$a")), """{"$tanh": "$a"}"""),
        testBson(
          "degreesToRadians",
          degreesToRadians(fieldPath("$a")),
          """{"$degreesToRadians": "$a"}""",
        ),
        testBson(
          "radiansToDegrees",
          radiansToDegrees(fieldPath("$a")),
          """{"$radiansToDegrees": "$a"}""",
        ),
      ),
      suite("type")(
        testBson(
          "convert",
          convert(
            input = fieldPath("$a"),
            to = const("int"),
            onError = Some(const("error")),
            onNull = Some(const("null")),
          ),
          """{"$convert": {"input": "$a", "to": "int", "onError": "error", "onNull": "null"}}""",
        ),
        testBson("isNumber", isNumber(fieldPath("$a")), """{"$isNumber": "$a"}"""),
        testBson("toBool", toBool(fieldPath("$a")), """{"$toBool": "$a"}"""),
        testBson("toDecimal", toDecimal(fieldPath("$a")), """{"$toDecimal": "$a"}"""),
        testBson("toDouble", toDouble(fieldPath("$a")), """{"$toDouble": "$a"}"""),
        testBson("toInt", toInt(fieldPath("$a")), """{"$toInt": "$a"}"""),
        testBson("toLong", toLong(fieldPath("$a")), """{"$toLong": "$a"}"""),
        testBson("toObjectId", toObjectId(fieldPath("$a")), """{"$toObjectId": "$a"}"""),
        testBson("type", `type`(fieldPath("$a")), """{"$type": "$a"}"""),
      ),
      testBson(
        "let",
        let(Map("a" -> fieldPath("$a")), const("a")),
        """{"$let": {"vars": {"a": "$a"}, "in": "a"}}""",
      ),
    ),
  )
}
