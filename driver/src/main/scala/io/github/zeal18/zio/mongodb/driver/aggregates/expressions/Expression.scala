package io.github.zeal18.zio.mongodb.driver.aggregates.expressions

import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.BsonWriter
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.conversions.Bson

import scala.annotation.nowarn

/** Expressions can include field paths, literals, system variables, expression objects and expression operators.
  * Expressions can be nested.
  *
  * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#expressions Expressions]]
  */
@nowarn("msg=possible missing interpolator")
sealed trait Expression { self =>
  def encode(writer: BsonWriter): Unit =
    self match {
      case Expression.FieldPath(path) => writer.writeString(path)
      case l: Expression.Literal[?] =>
        writer.writeStartDocument()
        writer.writeName("$literal")
        l.encoder.encode(writer, l.value, EncoderContext.builder().build())
        writer.writeEndDocument()

      case Expression.ExpressionObject(fields) =>
        writer.writeStartDocument()
        fields.foreach { case (name, value) =>
          writer.writeName(name)
          value.encode(writer)
        }
        writer.writeEndDocument()
      case Expression.Raw(bson) =>
        writer.pipe(new BsonDocumentReader(bson.toBsonDocument()))
      case Expression.Operator.Abs(expr) =>
        writer.writeStartDocument()
        writer.writeName("$abs")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Add(expr) =>
        writer.writeStartDocument()
        writer.writeName("$add")
        writer.writeStartArray()
        expr.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Ceil(expr) =>
        writer.writeStartDocument()
        writer.writeName("$ceil")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Divide(divident, devisor) =>
        writer.writeStartDocument()
        writer.writeName("$divide")
        writer.writeStartArray()
        divident.encode(writer)
        devisor.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Exp(expr) =>
        writer.writeStartDocument()
        writer.writeName("$exp")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Floor(expr) =>
        writer.writeStartDocument()
        writer.writeName("$floor")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Ln(expr) =>
        writer.writeStartDocument()
        writer.writeName("$ln")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Log(number, base) =>
        writer.writeStartDocument()
        writer.writeName("$log")
        writer.writeStartArray()
        number.encode(writer)
        base.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Log10(expr) =>
        writer.writeStartDocument()
        writer.writeName("$log10")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Mod(divident, devisor) =>
        writer.writeStartDocument()
        writer.writeName("$mod")
        writer.writeStartArray()
        divident.encode(writer)
        devisor.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Multiply(expr) =>
        writer.writeStartDocument()
        writer.writeName("$multiply")
        writer.writeStartArray()
        expr.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Pow(number, exponent) =>
        writer.writeStartDocument()
        writer.writeName("$pow")
        writer.writeStartArray()
        number.encode(writer)
        exponent.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Round(number, place) =>
        writer.writeStartDocument()
        writer.writeName("$round")
        if (place.isEmpty) {
          number.encode(writer)
        } else {
          writer.writeStartArray()
          number.encode(writer)
          place.foreach(_.encode(writer))
          writer.writeEndArray()
        }
        writer.writeEndDocument()
      case Expression.Operator.Sqrt(expr) =>
        writer.writeStartDocument()
        writer.writeName("$sqrt")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Subtract(minuend, subtrahend) =>
        writer.writeStartDocument()
        writer.writeName("$subtract")
        writer.writeStartArray()
        minuend.encode(writer)
        subtrahend.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Trunc(number, place) =>
        writer.writeStartDocument()
        writer.writeName("$trunc")
        if (place.isEmpty) {
          number.encode(writer)
        } else {
          writer.writeStartArray()
          number.encode(writer)
          place.foreach(_.encode(writer))
          writer.writeEndArray()
        }
        writer.writeEndDocument()
      case Expression.Operator.ArrayElemAt(array, index) =>
        writer.writeStartDocument()
        writer.writeName("$arrayElemAt")
        writer.writeStartArray()
        array.encode(writer)
        index.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.ArrayToObject(array) =>
        writer.writeStartDocument()
        writer.writeName("$arrayToObject")
        array.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ConcatArrays(arrays) =>
        writer.writeStartDocument()
        writer.writeName("$concatArrays")
        writer.writeStartArray()
        arrays.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Filter(input, cond, as, limit) =>
        writer.writeStartDocument()
        writer.writeName("$filter")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("cond")
        cond.encode(writer)
        as.foreach { as =>
          writer.writeName("as")
          writer.writeString(as)
        }
        limit.foreach { limit =>
          writer.writeName("limit")
          limit.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.First(array) =>
        writer.writeStartDocument()
        writer.writeName("$first")
        array.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.FirstN(array, n) =>
        writer.writeStartDocument()
        writer.writeName("$firstN")
        writer.writeStartDocument()
        writer.writeName("n")
        n.encode(writer)
        writer.writeName("input")
        array.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.In(array, value) =>
        writer.writeStartDocument()
        writer.writeName("$in")
        writer.writeStartArray()
        value.encode(writer)
        array.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.IndexOfArray(array, value, start, e) =>
        writer.writeStartDocument()
        writer.writeName("$indexOfArray")
        writer.writeStartArray()
        array.encode(writer)
        value.encode(writer)
        start.foreach(_.encode(writer))
        e.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.IsArray(expr) =>
        writer.writeStartDocument()
        writer.writeName("$isArray")
        writer.writeStartArray()
        expr.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Last(array) =>
        writer.writeStartDocument()
        writer.writeName("$last")
        array.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.LastN(array, n) =>
        writer.writeStartDocument()
        writer.writeName("$lastN")
        writer.writeStartDocument()
        writer.writeName("n")
        n.encode(writer)
        writer.writeName("input")
        array.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.Map(input, in, as) =>
        writer.writeStartDocument()
        writer.writeName("$map")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        as.foreach { as =>
          writer.writeName("as")
          writer.writeString(as)
        }
        writer.writeName("in")
        in.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.MaxN(array, n) =>
        writer.writeStartDocument()
        writer.writeName("$maxN")
        writer.writeStartDocument()
        writer.writeName("n")
        n.encode(writer)
        writer.writeName("input")
        array.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.MinN(array, n) =>
        writer.writeStartDocument()
        writer.writeName("$minN")
        writer.writeStartDocument()
        writer.writeName("n")
        n.encode(writer)
        writer.writeName("input")
        array.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ObjectToArray(obj) =>
        writer.writeStartDocument()
        writer.writeName("$objectToArray")
        obj.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Range(start, end, step) =>
        writer.writeStartDocument()
        writer.writeName("$range")
        writer.writeStartArray()
        start.encode(writer)
        end.encode(writer)
        step.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Reduce(input, initialValue, in) =>
        writer.writeStartDocument()
        writer.writeName("$reduce")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("initialValue")
        initialValue.encode(writer)
        writer.writeName("in")
        in.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ReverseArray(array) =>
        writer.writeStartDocument()
        writer.writeName("$reverseArray")
        array.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Size(array) =>
        writer.writeStartDocument()
        writer.writeName("$size")
        array.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Slice(array, position, n) =>
        writer.writeStartDocument()
        writer.writeName("$slice")
        writer.writeStartArray()
        array.encode(writer)
        position.foreach(_.encode(writer))
        n.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SortArrayByField(array, s) =>
        writer.writeStartDocument()
        writer.writeName("$sortArray")
        writer.writeStartDocument()
        writer.writeName("input")
        array.encode(writer)
        writer.writeName("sortBy")
        writer.pipe(new BsonDocumentReader(s.toBsonDocument()))
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.SortArrayByValue(array, asc) =>
        writer.writeStartDocument()
        writer.writeName("$sortArray")
        writer.writeStartDocument()
        writer.writeName("input")
        array.encode(writer)
        writer.writeName("sortBy")
        writer.writeInt32(if (asc) 1 else -1)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ZipShortest(inputs) =>
        writer.writeStartDocument()
        writer.writeName("$zip")
        writer.writeStartDocument()
        writer.writeName("inputs")
        writer.writeStartArray()
        inputs.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeName("useLongestLength")
        writer.writeBoolean(false)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ZipLongest(inputs, defaults) =>
        writer.writeStartDocument()
        writer.writeName("$zip")
        writer.writeStartDocument()
        writer.writeName("inputs")
        writer.writeStartArray()
        inputs.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeName("useLongestLength")
        writer.writeBoolean(true)
        writer.writeName("defaults")
        defaults.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.And(exprs) =>
        writer.writeStartDocument()
        writer.writeName("$and")
        writer.writeStartArray()
        exprs.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Not(expr) =>
        writer.writeStartDocument()
        writer.writeName("$not")
        writer.writeStartArray()
        expr.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Or(exprs) =>
        writer.writeStartDocument()
        writer.writeName("$or")
        writer.writeStartArray()
        exprs.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Cmp(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$cmp")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Eq(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$eq")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Gt(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$gt")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Gte(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$gte")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Lt(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$lt")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Lte(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$lte")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Ne(expr1, expr2) =>
        writer.writeStartDocument()
        writer.writeName("$ne")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Cond(predicate, ifTrue, ifFalse) =>
        writer.writeStartDocument()
        writer.writeName("$cond")
        writer.writeStartDocument()
        writer.writeName("if")
        predicate.encode(writer)
        writer.writeName("then")
        ifTrue.encode(writer)
        writer.writeName("else")
        ifFalse.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.IfNull(exprs, replacement) =>
        writer.writeStartDocument()
        writer.writeName("$ifNull")
        writer.writeStartArray()
        exprs.foreach(_.encode(writer))
        replacement.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Switch(branches, default) =>
        writer.writeStartDocument()
        writer.writeName("$switch")
        writer.writeStartDocument()
        writer.writeName("branches")
        writer.writeStartArray()
        branches.foreach { case (caseExpr, thenExpr) =>
          writer.writeStartDocument()
          writer.writeName("case")
          caseExpr.encode(writer)
          writer.writeName("then")
          thenExpr.encode(writer)
          writer.writeEndDocument()
        }
        writer.writeEndArray()
        default.foreach { defaultExpr =>
          writer.writeName("default")
          defaultExpr.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      // case Expression.Operator.Accumulator(init, initArgs, accumulate, accumulateArgs, merge, finalise) =>
      //   writer.writeStartDocument()
      //   writer.writeName("$accumulator")
      //   writer.writeStartDocument()
      //   writer.writeName("init")
      //   writer.writeString(init)
      //   initArgs.foreach { args =>
      //     writer.writeName("initArgs")
      //     args.encode(writer)
      //   }
      //   writer.writeName("accumulate")
      //   writer.writeString(accumulate)
      //   accumulateArgs.foreach { args =>
      //     writer.writeName("accumulateArgs")
      //     args.encode(writer)
      //   }
      //   writer.writeName("merge")
      //   writer.writeString(merge)
      //   finalise.foreach(writer.writeString("finalize", _))
      //   writer.writeString("lang", "js")
      //   writer.writeEndDocument()
      //   writer.writeEndDocument()
      case Expression.Operator.Function(body, args) =>
        writer.writeStartDocument()
        writer.writeName("$function")
        writer.writeStartDocument()
        writer.writeString("body", body)
        writer.writeName("args")
        args.fold {
          writer.writeStartArray()
          writer.writeEndArray()
        }(_.encode(writer))
        writer.writeString("lang", "js")
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.BinarySize(expr) =>
        writer.writeStartDocument()
        writer.writeName("$binarySize")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.BsonSize(expr) =>
        writer.writeStartDocument()
        writer.writeName("$bsonSize")
        expr.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.DateAdd(startDate, unit, amount, timezone) =>
        writer.writeStartDocument()
        writer.writeName("$dateAdd")
        writer.writeStartDocument()
        writer.writeName("startDate")
        startDate.encode(writer)
        writer.writeName("unit")
        unit.encode(writer)
        writer.writeName("amount")
        amount.encode(writer)
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateDiff(startDate, endDate, unit, timezone, startOfWeek) =>
        writer.writeStartDocument()
        writer.writeName("$dateDiff")
        writer.writeStartDocument()
        writer.writeName("startDate")
        startDate.encode(writer)
        writer.writeName("endDate")
        endDate.encode(writer)
        writer.writeName("unit")
        unit.encode(writer)
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        startOfWeek.foreach { sow =>
          writer.writeName("startOfWeek")
          sow.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateFromParts(
            year,
            month,
            day,
            hour,
            minute,
            second,
            millisecond,
            timezone,
          ) =>
        writer.writeStartDocument()
        writer.writeName("$dateFromParts")
        writer.writeStartDocument()
        writer.writeName("year")
        year.encode(writer)
        month.foreach { m =>
          writer.writeName("month")
          m.encode(writer)
        }
        day.foreach { d =>
          writer.writeName("day")
          d.encode(writer)
        }
        hour.foreach { h =>
          writer.writeName("hour")
          h.encode(writer)
        }
        minute.foreach { m =>
          writer.writeName("minute")
          m.encode(writer)
        }
        second.foreach { s =>
          writer.writeName("second")
          s.encode(writer)
        }
        millisecond.foreach { ms =>
          writer.writeName("millisecond")
          ms.encode(writer)
        }
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateFromPartsISO(
            isoWeekYear,
            isoWeek,
            isoDayOfWeek,
            hour,
            minute,
            second,
            millisecoond,
            timezone,
          ) =>
        writer.writeStartDocument()
        writer.writeName("$dateFromParts")
        writer.writeStartDocument()
        writer.writeName("isoWeekYear")
        isoWeekYear.encode(writer)
        isoWeek.foreach { iw =>
          writer.writeName("isoWeek")
          iw.encode(writer)
        }
        isoDayOfWeek.foreach { idow =>
          writer.writeName("isoDayOfWeek")
          idow.encode(writer)
        }
        hour.foreach { h =>
          writer.writeName("hour")
          h.encode(writer)
        }
        minute.foreach { m =>
          writer.writeName("minute")
          m.encode(writer)
        }
        second.foreach { s =>
          writer.writeName("second")
          s.encode(writer)
        }
        millisecoond.foreach { ms =>
          writer.writeName("millisecond")
          ms.encode(writer)
        }
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateFromString(dateString, format, timezone, onError, onNull) =>
        writer.writeStartDocument()
        writer.writeName("$dateFromString")
        writer.writeStartDocument()
        writer.writeName("dateString")
        dateString.encode(writer)
        format.foreach { f =>
          writer.writeName("format")
          f.encode(writer)
        }
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        onError.foreach { oe =>
          writer.writeName("onError")
          oe.encode(writer)
        }
        onNull.foreach { on =>
          writer.writeName("onNull")
          on.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateSubtract(startDate, unit, amount, timezone) =>
        writer.writeStartDocument()
        writer.writeName("$dateSubtract")
        writer.writeStartDocument()
        writer.writeName("startDate")
        startDate.encode(writer)
        writer.writeName("unit")
        unit.encode(writer)
        writer.writeName("amount")
        amount.encode(writer)
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()

      case Expression.Operator.DateToParts(date, timezone, iso8601) =>
        writer.writeStartDocument()
        writer.writeName("$dateToParts")
        writer.writeStartDocument()
        writer.writeName("date")
        date.encode(writer)
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        iso8601.foreach { i =>
          writer.writeName("iso8601")
          writer.writeBoolean(i)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateToString(date, format, timezone, onNull) =>
        writer.writeStartDocument()
        writer.writeName("$dateToString")
        writer.writeStartDocument()
        writer.writeName("date")
        date.encode(writer)
        writer.writeString("format", format)
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        onNull.foreach { on =>
          writer.writeName("onNull")
          on.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DateTrunc(date, unit, binSize, timezone, startOfWeek) =>
        writer.writeStartDocument()
        writer.writeName("$dateTrunc")
        writer.writeStartDocument()
        writer.writeName("date")
        date.encode(writer)
        writer.writeName("unit")
        unit.encode(writer)
        binSize.foreach { bs =>
          writer.writeName("binSize")
          bs.encode(writer)
        }
        timezone.foreach { tz =>
          writer.writeName("timezone")
          tz.encode(writer)
        }
        startOfWeek.foreach { sow =>
          writer.writeName("startOfWeek")
          sow.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.DayOfMonth(date) =>
        writer.writeStartDocument()
        writer.writeName("$dayOfMonth")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.DayOfWeek(date) =>
        writer.writeStartDocument()
        writer.writeName("$dayOfWeek")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.DayOfYear(date) =>
        writer.writeStartDocument()
        writer.writeName("$dayOfYear")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Hour(date) =>
        writer.writeStartDocument()
        writer.writeName("$hour")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.IsoDayOfWeek(date) =>
        writer.writeStartDocument()
        writer.writeName("$isoDayOfWeek")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.IsoWeek(date) =>
        writer.writeStartDocument()
        writer.writeName("$isoWeek")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.IsoWeekYear(date) =>
        writer.writeStartDocument()
        writer.writeName("$isoWeekYear")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Millisecond(date) =>
        writer.writeStartDocument()
        writer.writeName("$millisecond")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Minute(date) =>
        writer.writeStartDocument()
        writer.writeName("$minute")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Month(date) =>
        writer.writeStartDocument()
        writer.writeName("$month")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Second(date) =>
        writer.writeStartDocument()
        writer.writeName("$second")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToDate(value) =>
        writer.writeStartDocument()
        writer.writeName("$toDate")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Week(date) =>
        writer.writeStartDocument()
        writer.writeName("$week")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Year(date) =>
        writer.writeStartDocument()
        writer.writeName("$year")
        date.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.GetField(field, input) =>
        writer.writeStartDocument()
        writer.writeName("$getField")
        writer.writeStartDocument()
        writer.writeName("field")
        field.encode(writer)
        input.foreach { i =>
          writer.writeName("input")
          i.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.Rand =>
        writer.writeStartDocument()
        writer.writeName("$rand")
        writer.writeStartDocument()
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.SampleRate(input) =>
        writer.writeStartDocument()
        writer.writeName("$sampleRate")
        input.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.MergeObjects(values) =>
        writer.writeStartDocument()
        writer.writeName("$mergeObjects")
        writer.writeStartArray()
        values.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SetField(field, input, value) =>
        writer.writeStartDocument()
        writer.writeName("$setField")
        writer.writeStartDocument()
        writer.writeName("field")
        field.encode(writer)
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("value")
        value.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.AllElementsTrue(value) =>
        writer.writeStartDocument()
        writer.writeName("$allElementsTrue")
        writer.writeStartArray()
        value.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.AnyElementTrue(value) =>
        writer.writeStartDocument()
        writer.writeName("$anyElementTrue")
        writer.writeStartArray()
        value.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SetDifference(left, right) =>
        writer.writeStartDocument()
        writer.writeName("$setDifference")
        writer.writeStartArray()
        left.encode(writer)
        right.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SetEquals(arrays) =>
        writer.writeStartDocument()
        writer.writeName("$setEquals")
        writer.writeStartArray()
        arrays.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SetIntersection(arrays) =>
        writer.writeStartDocument()
        writer.writeName("$setIntersection")
        writer.writeStartArray()
        arrays.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SetIsSubset(left, right) =>
        writer.writeStartDocument()
        writer.writeName("$setIsSubset")
        writer.writeStartArray()
        left.encode(writer)
        right.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SetUnion(arrays) =>
        writer.writeStartDocument()
        writer.writeName("$setUnion")
        writer.writeStartArray()
        arrays.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Concat(strings) =>
        writer.writeStartDocument()
        writer.writeName("$concat")
        writer.writeStartArray()
        strings.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.IndexOfBytes(string, substring, start, end) =>
        writer.writeStartDocument()
        writer.writeName("$indexOfBytes")
        writer.writeStartArray()
        string.encode(writer)
        substring.encode(writer)
        start.foreach(_.encode(writer))
        end.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.IndexOfCP(string, substring, start, end) =>
        writer.writeStartDocument()
        writer.writeName("$indexOfCP")
        writer.writeStartArray()
        string.encode(writer)
        substring.encode(writer)
        start.foreach(_.encode(writer))
        end.foreach(_.encode(writer))
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.LTrim(string, chars) =>
        writer.writeStartDocument()
        writer.writeName("$ltrim")
        writer.writeStartDocument()
        writer.writeName("input")
        string.encode(writer)
        chars.foreach { c =>
          writer.writeName("chars")
          c.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.RegexFind(input, regex, options) =>
        writer.writeStartDocument()
        writer.writeName("$regexFind")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("regex")
        regex.encode(writer)
        options.foreach { o =>
          writer.writeName("options")
          o.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.RegexFindAll(input, regex, options) =>
        writer.writeStartDocument()
        writer.writeName("$regexFindAll")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("regex")
        regex.encode(writer)
        options.foreach { o =>
          writer.writeName("options")
          o.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.RegexMatch(input, regex, options) =>
        writer.writeStartDocument()
        writer.writeName("$regexMatch")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("regex")
        regex.encode(writer)
        options.foreach { o =>
          writer.writeName("options")
          o.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ReplaceOne(string, substring, replacement) =>
        writer.writeStartDocument()
        writer.writeName("$replaceOne")
        writer.writeStartDocument()
        writer.writeName("input")
        string.encode(writer)
        writer.writeName("find")
        substring.encode(writer)
        writer.writeName("replacement")
        replacement.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ReplaceAll(string, substring, replacement) =>
        writer.writeStartDocument()
        writer.writeName("$replaceAll")
        writer.writeStartDocument()
        writer.writeName("input")
        string.encode(writer)
        writer.writeName("find")
        substring.encode(writer)
        writer.writeName("replacement")
        replacement.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.RTrim(string, chars) =>
        writer.writeStartDocument()
        writer.writeName("$rtrim")
        writer.writeStartDocument()
        writer.writeName("input")
        string.encode(writer)
        chars.foreach { c =>
          writer.writeName("chars")
          c.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.Split(string, delimiter) =>
        writer.writeStartDocument()
        writer.writeName("$split")
        writer.writeStartArray()
        string.encode(writer)
        delimiter.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.StrLenBytes(string) =>
        writer.writeStartDocument()
        writer.writeName("$strLenBytes")
        string.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.StrLenCP(string) =>
        writer.writeStartDocument()
        writer.writeName("$strLenCP")
        string.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.StrCaseCmp(string1, string2) =>
        writer.writeStartDocument()
        writer.writeName("$strcasecmp")
        writer.writeStartArray()
        string1.encode(writer)
        string2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SubstrBytes(string, start, length) =>
        writer.writeStartDocument()
        writer.writeName("$substrBytes")
        writer.writeStartArray()
        string.encode(writer)
        start.encode(writer)
        length.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.SubstrCP(string, start, length) =>
        writer.writeStartDocument()
        writer.writeName("$substrCP")
        writer.writeStartArray()
        string.encode(writer)
        start.encode(writer)
        length.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.ToLower(string) =>
        writer.writeStartDocument()
        writer.writeName("$toLower")
        string.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToString(value) =>
        writer.writeStartDocument()
        writer.writeName("$toString")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Trim(string, chars) =>
        writer.writeStartDocument()
        writer.writeName("$trim")
        writer.writeStartDocument()
        writer.writeName("input")
        string.encode(writer)
        chars.foreach { c =>
          writer.writeName("chars")
          c.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.ToUpper(string) =>
        writer.writeStartDocument()
        writer.writeName("$toUpper")
        string.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Meta(keyword) =>
        writer.writeStartDocument()
        writer.writeName("$meta")
        keyword.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Sin(value) =>
        writer.writeStartDocument()
        writer.writeName("$sin")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Cos(value) =>
        writer.writeStartDocument()
        writer.writeName("$cos")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Tan(value) =>
        writer.writeStartDocument()
        writer.writeName("$tan")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Asin(value) =>
        writer.writeStartDocument()
        writer.writeName("$asin")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Acos(value) =>
        writer.writeStartDocument()
        writer.writeName("$acos")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Atan(value) =>
        writer.writeStartDocument()
        writer.writeName("$atan")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Atan2(x, y) =>
        writer.writeStartDocument()
        writer.writeName("$atan2")
        writer.writeStartArray()
        x.encode(writer)
        y.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()
      case Expression.Operator.Asinh(value) =>
        writer.writeStartDocument()
        writer.writeName("$asinh")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Acosh(value) =>
        writer.writeStartDocument()
        writer.writeName("$acosh")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Atanh(value) =>
        writer.writeStartDocument()
        writer.writeName("$atanh")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Sinh(value) =>
        writer.writeStartDocument()
        writer.writeName("$sinh")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Cosh(value) =>
        writer.writeStartDocument()
        writer.writeName("$cosh")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Tanh(value) =>
        writer.writeStartDocument()
        writer.writeName("$tanh")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.DegreesToRadians(value) =>
        writer.writeStartDocument()
        writer.writeName("$degreesToRadians")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.RadiansToDegrees(value) =>
        writer.writeStartDocument()
        writer.writeName("$radiansToDegrees")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Convert(input, to, orError, onNull) =>
        writer.writeStartDocument()
        writer.writeName("$convert")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("to")
        to.encode(writer)
        orError.foreach { error =>
          writer.writeName("onError")
          error.encode(writer)
        }
        onNull.foreach { nullValue =>
          writer.writeName("onNull")
          nullValue.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()
      case Expression.Operator.IsNumber(value) =>
        writer.writeStartDocument()
        writer.writeName("$isNumber")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToBool(value) =>
        writer.writeStartDocument()
        writer.writeName("$toBool")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToDecimal(value) =>
        writer.writeStartDocument()
        writer.writeName("$toDecimal")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToDouble(value) =>
        writer.writeStartDocument()
        writer.writeName("$toDouble")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToInt(value) =>
        writer.writeStartDocument()
        writer.writeName("$toInt")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToLong(value) =>
        writer.writeStartDocument()
        writer.writeName("$toLong")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.ToObjectId(value) =>
        writer.writeStartDocument()
        writer.writeName("$toObjectId")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Type(value) =>
        writer.writeStartDocument()
        writer.writeName("$type")
        value.encode(writer)
        writer.writeEndDocument()
      case Expression.Operator.Let(vars, in) =>
        writer.writeStartDocument()
        writer.writeName("$let")
        writer.writeStartDocument()
        writer.writeName("vars")
        writer.writeStartDocument()
        vars.foreach { case (k, v) =>
          writer.writeName(k)
          v.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeName("in")
        in.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()
      case v: Expression.Variable.Constant[?] =>
        v.encoder.encode(writer, v.value, EncoderContext.builder().build())
      case Expression.Variable.Now         => writer.writeString("$$NOW")
      case Expression.Variable.ClusterTime => writer.writeString("$$CLUSTER_TIME")
      case Expression.Variable.Root        => writer.writeString("$$ROOT")
      case Expression.Variable.Current     => writer.writeString("$$CURRENT")
      case Expression.Variable.Remove      => writer.writeString("$$REMOVE")
      case Expression.Variable.Descent     => writer.writeString("$$DESCEND")
      case Expression.Variable.Prune       => writer.writeString("$$PRUNE")
      case Expression.Variable.Keep        => writer.writeString("$$KEEP")
    }
}

object Expression {
  final case class FieldPath(path: String) extends Expression

  sealed trait Variable extends Expression

  case class Literal[A](val value: A, val encoder: Encoder[A]) extends Bson with Expression {
    override def toBsonDocument[TDocument <: Object](
      documentClass: Class[TDocument],
      codecRegistry: CodecRegistry,
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())
      encode(writer)
      writer.getDocument()
    }
  }

  case class ExpressionObject(fields: Map[String, Expression]) extends Bson with Expression {
    override def toBsonDocument[TDocument <: Object](
      documentClass: Class[TDocument],
      codecRegistry: CodecRegistry,
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())
      encode(writer)
      writer.getDocument()
    }
  }

  sealed trait Operator extends Bson with Expression {
    override def toBsonDocument[TDocument <: Object](
      documentClass: Class[TDocument],
      codecRegistry: CodecRegistry,
    ): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())
      encode(writer)
      writer.getDocument()
    }
  }

  case class Raw(bson: Bson) extends Bson with Expression {
    override def toBsonDocument[TDocument <: Object](
      documentClass: Class[TDocument],
      codecRegistry: CodecRegistry,
    ): BsonDocument = bson.toBsonDocument(documentClass, codecRegistry)
  }

  object Variable {
    sealed trait System extends Variable

    case object Now         extends System
    case object ClusterTime extends System
    case object Root        extends System
    case object Current     extends System
    case object Remove      extends System
    case object Descent     extends System
    case object Prune       extends System
    case object Keep        extends System

    case class Constant[A](val value: A, val encoder: Encoder[A]) extends Variable
  }

  object Operator {
    sealed trait Arithmetic   extends Operator
    sealed trait Array        extends Operator
    sealed trait Boolean      extends Operator
    sealed trait Comparison   extends Operator
    sealed trait Conditional  extends Operator
    sealed trait Custom       extends Operator
    sealed trait DataSize     extends Operator
    sealed trait Date         extends Operator
    sealed trait Misc         extends Operator
    sealed trait Object       extends Operator
    sealed trait Set          extends Operator
    sealed trait String       extends Operator
    sealed trait Text         extends Operator
    sealed trait Trigonometry extends Operator
    sealed trait TypeOperator extends Operator
    sealed trait Variable     extends Operator

    final case class Abs(number: Expression)                               extends Arithmetic
    final case class Add(expressions: Seq[Expression])                     extends Arithmetic
    final case class Ceil(number: Expression)                              extends Arithmetic
    final case class Divide(dividend: Expression, devisor: Expression)     extends Arithmetic
    final case class Exp(exponent: Expression)                             extends Arithmetic
    final case class Floor(number: Expression)                             extends Arithmetic
    final case class Ln(number: Expression)                                extends Arithmetic
    final case class Log(number: Expression, base: Expression)             extends Arithmetic
    final case class Log10(number: Expression)                             extends Arithmetic
    final case class Mod(dividend: Expression, divisor: Expression)        extends Arithmetic
    final case class Multiply(numbers: Seq[Expression])                    extends Arithmetic
    final case class Pow(number: Expression, exponent: Expression)         extends Arithmetic
    final case class Round(number: Expression, place: Option[Expression])  extends Arithmetic
    final case class Sqrt(number: Expression)                              extends Arithmetic
    final case class Subtract(minuend: Expression, subtrahend: Expression) extends Arithmetic
    final case class Trunc(number: Expression, place: Option[Expression])  extends Arithmetic

    final case class ArrayElemAt(array: Expression, idx: Expression) extends Array
    final case class ArrayToObject(array: Expression)                extends Array
    final case class ConcatArrays(arrays: Seq[Expression])           extends Array
    final case class Filter(
      input: Expression,
      cond: Expression,
      as: Option[scala.Predef.String] = None,
      limit: Option[Expression] = None,
    ) extends Array
    final case class First(array: Expression)                 extends Array
    final case class FirstN(array: Expression, n: Expression) extends Array
    final case class In(array: Expression, value: Expression) extends Array
    final case class IndexOfArray(
      array: Expression,
      value: Expression,
      start: Option[Expression] = None,
      end: Option[Expression] = None,
    ) extends Array
    final case class IsArray(expr: Expression)                                                      extends Array
    final case class Last(array: Expression)                                                        extends Array
    final case class LastN(array: Expression, n: Expression)                                        extends Array
    final case class Map(input: Expression, in: Expression, as: Option[scala.Predef.String] = None) extends Array
    final case class MaxN(array: Expression, n: Expression)                                         extends Array
    final case class MinN(array: Expression, n: Expression)                                         extends Array
    final case class ObjectToArray(obj: Expression) extends Array with Object
    final case class Range(start: Expression, end: Expression, step: Option[Expression] = None) extends Array
    final case class Reduce(input: Expression, initialValue: Expression, in: Expression)        extends Array
    final case class ReverseArray(array: Expression)                                            extends Array
    final case class Size(array: Expression)                                                    extends Array
    final case class Slice(array: Expression, position: Option[Expression], n: Expression)      extends Array
    final case class SortArrayByField(array: Expression, sort: Sort)                            extends Array
    final case class SortArrayByValue(array: Expression, asc: scala.Boolean)                    extends Array
    final case class ZipShortest(inputs: Seq[Expression])                                       extends Array
    final case class ZipLongest(inputs: Seq[Expression], defaults: Expression)                  extends Array

    final case class And(expressions: Seq[Expression]) extends Boolean
    final case class Not(expression: Expression)       extends Boolean
    final case class Or(expressions: Seq[Expression])  extends Boolean

    final case class Cmp(expression1: Expression, expression2: Expression) extends Comparison
    final case class Eq(expression1: Expression, expression2: Expression)  extends Comparison
    final case class Gt(expression1: Expression, expression2: Expression)  extends Comparison
    final case class Gte(expression1: Expression, expression2: Expression) extends Comparison
    final case class Lt(expression1: Expression, expression2: Expression)  extends Comparison
    final case class Lte(expression1: Expression, expression2: Expression) extends Comparison
    final case class Ne(expression1: Expression, expression2: Expression)  extends Comparison

    final case class Cond(predicate: Expression, ifTrue: Expression, ifFalse: Expression)         extends Conditional
    final case class IfNull(expressions: Seq[Expression], replacement: Expression)                extends Conditional
    final case class Switch(branches: Seq[(Expression, Expression)], default: Option[Expression]) extends Conditional

    final case class Function(body: scala.Predef.String, args: Option[Expression] = None) extends Custom

    final case class BinarySize(expression: Expression) extends DataSize
    final case class BsonSize(expression: Expression)   extends DataSize

    final case class DateAdd(
      startDate: Expression,
      unit: Expression,
      amount: Expression,
      timezone: Option[Expression],
    ) extends Date
    final case class DateDiff(
      startDate: Expression,
      endDate: Expression,
      unit: Expression,
      timezone: Option[Expression] = None,
      startOfWeek: Option[Expression] = None,
    ) extends Date
    final case class DateFromParts(
      year: Expression,
      month: Option[Expression],
      day: Option[Expression],
      hour: Option[Expression],
      minute: Option[Expression],
      second: Option[Expression],
      millisecond: Option[Expression],
      timezone: Option[Expression],
    ) extends Date
    final case class DateFromPartsISO(
      isoWeekYear: Expression,
      isoWeek: Option[Expression],
      isoDayOfWeek: Option[Expression],
      hour: Option[Expression],
      minute: Option[Expression],
      second: Option[Expression],
      millisecond: Option[Expression],
      timezone: Option[Expression],
    ) extends Date
    final case class DateFromString(
      dateString: Expression,
      format: Option[Expression] = None,
      timezone: Option[Expression] = None,
      onError: Option[Expression] = None,
      onNull: Option[Expression] = None,
    ) extends Date
        with String
    final case class DateSubtract(
      startDate: Expression,
      unit: Expression,
      amount: Expression,
      timezone: Option[Expression] = None,
    ) extends Date
    final case class DateToParts(
      date: Expression,
      timezone: Option[Expression] = None,
      iso8601: Option[scala.Boolean] = None,
    ) extends Date
    final case class DateToString(
      date: Expression,
      format: scala.Predef.String,
      timezone: Option[Expression] = None,
      onNull: Option[Expression] = None,
    ) extends Date
        with String
    final case class DateTrunc(
      date: Expression,
      unit: Expression,
      binSize: Option[Expression] = None,
      timezone: Option[Expression] = None,
      startOfWeek: Option[Expression] = None,
    ) extends Date
    final case class DayOfMonth(date: Expression)   extends Date
    final case class DayOfWeek(date: Expression)    extends Date
    final case class DayOfYear(date: Expression)    extends Date
    final case class Hour(date: Expression)         extends Date
    final case class IsoDayOfWeek(date: Expression) extends Date
    final case class IsoWeek(date: Expression)      extends Date
    final case class IsoWeekYear(date: Expression)  extends Date
    final case class Millisecond(date: Expression)  extends Date
    final case class Minute(date: Expression)       extends Date
    final case class Month(date: Expression)        extends Date
    final case class Second(date: Expression)       extends Date
    final case class ToDate(value: Expression)      extends Date with TypeOperator
    final case class Week(date: Expression)         extends Date
    final case class Year(date: Expression)         extends Date

    final case class GetField(field: Expression, input: Option[Expression] = None) extends Misc
    object Rand                                                                    extends Misc
    final case class SampleRate(rate: Expression)                                  extends Misc

    final case class MergeObjects(expressions: Seq[Expression])                        extends Object
    final case class SetField(field: Expression, input: Expression, value: Expression) extends Object

    final case class AllElementsTrue(array: Expression)                   extends Set
    final case class AnyElementTrue(array: Expression)                    extends Set
    final case class SetDifference(first: Expression, second: Expression) extends Set
    final case class SetEquals(arrays: Seq[Expression])                   extends Set
    final case class SetIntersection(arrays: Seq[Expression])             extends Set
    final case class SetIsSubset(first: Expression, second: Expression)   extends Set
    final case class SetUnion(arrays: Seq[Expression])                    extends Set

    final case class Concat(strings: Seq[Expression]) extends String
    final case class IndexOfBytes(
      string: Expression,
      substring: Expression,
      start: Option[Expression] = None,
      end: Option[Expression] = None,
    ) extends String
    final case class IndexOfCP(
      string: Expression,
      substring: Expression,
      start: Option[Expression] = None,
      end: Option[Expression] = None,
    ) extends String
    final case class LTrim(string: Expression, chars: Option[Expression] = None) extends String
    final case class RegexFind(
      input: Expression,
      regex: Expression,
      options: Option[Expression] = None,
    ) extends String
    final case class RegexFindAll(
      input: Expression,
      regex: Expression,
      options: Option[Expression] = None,
    ) extends String
    final case class RegexMatch(
      input: Expression,
      regex: Expression,
      options: Option[Expression] = None,
    ) extends String
    final case class ReplaceOne(
      input: Expression,
      find: Expression,
      replacement: Expression,
    ) extends String
    final case class ReplaceAll(
      input: Expression,
      find: Expression,
      replacement: Expression,
    ) extends String
    final case class RTrim(string: Expression, chars: Option[Expression] = None)           extends String
    final case class Split(string: Expression, delimiter: Expression)                      extends String
    final case class StrLenBytes(string: Expression)                                       extends String
    final case class StrLenCP(string: Expression)                                          extends String
    final case class StrCaseCmp(string1: Expression, string2: Expression)                  extends String
    final case class SubstrBytes(string: Expression, start: Expression, count: Expression) extends String
    final case class SubstrCP(string: Expression, start: Expression, count: Expression)    extends String
    final case class ToLower(string: Expression)                                           extends String
    final case class ToString(value: Expression)                                extends String with TypeOperator
    final case class Trim(string: Expression, chars: Option[Expression] = None) extends String
    final case class ToUpper(string: Expression)                                extends String
    final case class Meta(keyword: Expression)                                  extends Text

    final case class Sin(value: Expression)              extends Trigonometry
    final case class Cos(value: Expression)              extends Trigonometry
    final case class Tan(value: Expression)              extends Trigonometry
    final case class Asin(value: Expression)             extends Trigonometry
    final case class Acos(value: Expression)             extends Trigonometry
    final case class Atan(value: Expression)             extends Trigonometry
    final case class Atan2(y: Expression, x: Expression) extends Trigonometry
    final case class Asinh(value: Expression)            extends Trigonometry
    final case class Acosh(value: Expression)            extends Trigonometry
    final case class Atanh(value: Expression)            extends Trigonometry
    final case class Sinh(value: Expression)             extends Trigonometry
    final case class Cosh(value: Expression)             extends Trigonometry
    final case class Tanh(value: Expression)             extends Trigonometry
    final case class DegreesToRadians(value: Expression) extends Trigonometry
    final case class RadiansToDegrees(value: Expression) extends Trigonometry

    final case class Convert(
      input: Expression,
      to: Expression,
      onError: Option[Expression] = None,
      onNull: Option[Expression] = None,
    ) extends TypeOperator
    final case class IsNumber(value: Expression)   extends TypeOperator
    final case class ToBool(value: Expression)     extends TypeOperator
    final case class ToDecimal(value: Expression)  extends TypeOperator
    final case class ToDouble(value: Expression)   extends TypeOperator
    final case class ToInt(value: Expression)      extends TypeOperator
    final case class ToLong(value: Expression)     extends TypeOperator
    final case class ToObjectId(value: Expression) extends TypeOperator
    final case class Type(value: Expression)       extends TypeOperator

    final case class Let(
      vars: scala.collection.immutable.Map[scala.Predef.String, Expression],
      in: Expression,
    ) extends Variable
  }
}
