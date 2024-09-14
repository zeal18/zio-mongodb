package io.github.zeal18.zio.mongodb.driver.aggregates.windows

import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.configuration.CodecRegistry

import scala.annotation.nowarn

@nowarn("msg=possible missing interpolator")
sealed trait Window extends Bson { self =>
  override def toBsonDocument[A <: Object](
    documentClass: Class[A],
    codecRegistry: CodecRegistry,
  ): BsonDocument = {
    def simpleExpression(operator: String, expression: Expression): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeName(operator)
      expression.encode(writer)
      writer.writeEndDocument()

      writer.getDocument()
    }

    self match {
      case Window.Raw(bson) =>
        val writer = new BsonDocumentWriter(new BsonDocument())
        writer.pipe(new BsonDocumentReader(bson.toBsonDocument()))
        writer.getDocument()
      case Window.Sum(expression) =>
        simpleExpression("$sum", expression)
      case Window.Avg(expression) =>
        simpleExpression("$avg", expression)
      case Window.First(expression) =>
        simpleExpression("$first", expression)
      case Window.FirstN(input, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$firstN")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("n")
        n.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Last(expression) =>
        simpleExpression("$last", expression)
      case Window.LastN(input, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$lastN")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("n")
        n.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Max(expression) =>
        simpleExpression("$max", expression)
      case Window.MaxN(input, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$maxN")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("n")
        n.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Min(expression) =>
        simpleExpression("$min", expression)
      case Window.MinN(input, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$minN")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeName("n")
        n.encode(writer)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Push(expression) =>
        simpleExpression("$push", expression)
      case Window.AddToSet(expression) =>
        simpleExpression("$addToSet", expression)
      case Window.StdDevPop(expression) =>
        simpleExpression("$stdDevPop", expression)
      case Window.StdDevSamp(expression) =>
        simpleExpression("$stdDevSamp", expression)
      case Window.Bottom(sortBy, output) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$bottom")

        writer.writeStartDocument()
        writer.writeName("sortBy")
        writer.pipe(new BsonDocumentReader(sortBy.toBsonDocument()))
        writer.writeName("output")
        output.encode(writer)
        writer.writeEndDocument()

        writer.writeEndDocument()

        writer.getDocument()
      case Window.BottomN(sortBy, output, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$bottom")

        writer.writeStartDocument()
        writer.writeName("n")
        n.encode(writer)
        writer.writeName("sortBy")
        writer.pipe(new BsonDocumentReader(sortBy.toBsonDocument()))
        writer.writeName("output")
        output.encode(writer)
        writer.writeEndDocument()

        writer.writeEndDocument()

        writer.getDocument()
      case Window.Top(sortBy, output) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$top")

        writer.writeStartDocument()
        writer.writeName("sortBy")
        writer.pipe(new BsonDocumentReader(sortBy.toBsonDocument()))
        writer.writeName("output")
        output.encode(writer)
        writer.writeEndDocument()

        writer.writeEndDocument()

        writer.getDocument()
      case Window.TopN(sortBy, output, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$topN")

        writer.writeStartDocument()
        writer.writeName("n")
        n.encode(writer)
        writer.writeName("sortBy")
        writer.pipe(new BsonDocumentReader(sortBy.toBsonDocument()))
        writer.writeName("output")
        output.encode(writer)
        writer.writeEndDocument()

        writer.writeEndDocument()

        writer.getDocument()
      case Window.Count =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$count")
        writer.writeStartDocument()
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.CovariancePop(expr1, expr2) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$covariancePop")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.CovarianceSamp(expr1, expr2) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$covarianceSamp")
        writer.writeStartArray()
        expr1.encode(writer)
        expr2.encode(writer)
        writer.writeEndArray()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.DenseRank =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$denseRank")
        writer.writeStartDocument()
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Derivative(input, unit) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$derivative")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        unit.foreach(u => writer.writeString("unit", u.toString))
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.DocumentNumber =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$documentNumber")
        writer.writeStartDocument()
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.ExpMovingAvgN(input, n) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$expMovingAvg")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeInt32("N", n)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.ExpMovingAvgAlpha(input, alpha) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$expMovingAvg")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        writer.writeDouble("alpha", alpha)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Integral(input, unit) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$integral")
        writer.writeStartDocument()
        writer.writeName("input")
        input.encode(writer)
        unit.foreach(u => writer.writeString("unit", u.toString))
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.LinearFill(expr) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$linearFill")
        expr.encode(writer)
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Locf(expr) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$locf")
        expr.encode(writer)
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Rank =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$rank")
        writer.writeStartDocument()
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
      case Window.Shift(output, by, default) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$shift")
        writer.writeStartDocument()
        writer.writeName("output")
        output.encode(writer)
        writer.writeInt32("by", by)
        default.foreach { d =>
          writer.writeName("default")
          d.encode(writer)
        }
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
    }
  }
}

object Window {
  final case class Sum(expression: Expression)                                      extends Window
  final case class Avg(expression: Expression)                                      extends Window
  final case class First(expression: Expression)                                    extends Window
  final case class FirstN(input: Expression, n: Expression)                         extends Window
  final case class Last(expression: Expression)                                     extends Window
  final case class LastN(input: Expression, n: Expression)                          extends Window
  final case class Max(expression: Expression)                                      extends Window
  final case class MaxN(input: Expression, n: Expression)                           extends Window
  final case class Min(expression: Expression)                                      extends Window
  final case class MinN(input: Expression, n: Expression)                           extends Window
  final case class Push(expression: Expression)                                     extends Window
  final case class AddToSet(expression: Expression)                                 extends Window
  final case class StdDevPop(expression: Expression)                                extends Window
  final case class StdDevSamp(expression: Expression)                               extends Window
  final case class Bottom(sortBy: Sort, output: Expression)                         extends Window
  final case class BottomN(sortBy: Sort, output: Expression, n: Expression)         extends Window
  final case class Top(sortBy: Sort, output: Expression)                            extends Window
  final case class TopN(sortBy: Sort, output: Expression, n: Expression)            extends Window
  object Count                                                                      extends Window
  final case class CovariancePop(expression1: Expression, expression2: Expression)  extends Window
  final case class CovarianceSamp(expression1: Expression, expression2: Expression) extends Window
  object DenseRank                                                                  extends Window
  final case class Derivative(input: Expression, unit: Option[TimeUnit])            extends Window
  object DocumentNumber                                                             extends Window
  final case class ExpMovingAvgN(input: Expression, n: Int)                         extends Window
  final case class ExpMovingAvgAlpha(input: Expression, alpha: Double)              extends Window
  final case class Integral(input: Expression, unit: Option[TimeUnit])              extends Window
  final case class LinearFill(expression: Expression)                               extends Window
  final case class Locf(expression: Expression)                                     extends Window
  object Rank                                                                       extends Window
  final case class Shift(output: Expression, by: Int, default: Option[Expression])  extends Window
  final case class Raw(bson: Bson)                                                  extends Window

  sealed trait TimeUnit {
    override def toString(): String = this match {
      case TimeUnit.Millisecond => "millisecond"
      case TimeUnit.Second      => "second"
      case TimeUnit.Minute      => "minute"
      case TimeUnit.Hour        => "hour"
      case TimeUnit.Day         => "day"
      case TimeUnit.Week        => "week"
    }
  }
  object TimeUnit {
    case object Millisecond extends TimeUnit
    case object Second      extends TimeUnit
    case object Minute      extends TimeUnit
    case object Hour        extends TimeUnit
    case object Day         extends TimeUnit
    case object Week        extends TimeUnit
  }
}
