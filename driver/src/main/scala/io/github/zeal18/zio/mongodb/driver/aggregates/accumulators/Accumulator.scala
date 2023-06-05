package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.configuration.CodecRegistry

sealed trait Accumulator extends Bson { self =>
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
      case Accumulator.Sum(expression) =>
        simpleExpression("$sum", expression)
      case Accumulator.Avg(expression) =>
        simpleExpression("$avg", expression)
      case Accumulator.First(expression) =>
        simpleExpression("$first", expression)
      case Accumulator.Last(expression) =>
        simpleExpression("$last", expression)
      case Accumulator.Max(expression) =>
        simpleExpression("$max", expression)
      case Accumulator.Min(expression) =>
        simpleExpression("$min", expression)
      case Accumulator.Push(expression) =>
        simpleExpression("$push", expression)
      case Accumulator.AddToSet(expression) =>
        simpleExpression("$addToSet", expression)
      case Accumulator.MergeObjects(expression) =>
        simpleExpression("$mergeObjects", expression)
      case Accumulator.StdDevPop(expression) =>
        simpleExpression("$stdDevPop", expression)
      case Accumulator.StdDevSamp(expression) =>
        simpleExpression("$stdDevSamp", expression)
      case Accumulator.Function(
            initFunction,
            initArgs,
            accumulateFunction,
            accumulateArgs,
            mergeFunction,
            finalizeFunction,
            lang,
          ) =>
        val writer = new BsonDocumentWriter(new BsonDocument())

        writer.writeStartDocument()
        writer.writeName("$accumulator")
        writer.writeStartDocument()
        writer.writeString("init", initFunction)
        initArgs.foreach { args =>
          writer.writeName("initArgs")
          args.encode(writer)
        }
        writer.writeString("accumulate", accumulateFunction)
        accumulateArgs.foreach { args =>
          writer.writeName("accumulateArgs")
          args.encode(writer)
        }
        writer.writeString("merge", mergeFunction)
        finalizeFunction.foreach { func =>
          writer.writeString("finalize", func)
        }
        writer.writeString("lang", lang)
        writer.writeEndDocument()
        writer.writeEndDocument()

        writer.getDocument()
    }
  }
}

object Accumulator {
  final case class Sum(expression: Expression)          extends Accumulator
  final case class Avg(expression: Expression)          extends Accumulator
  final case class First(expression: Expression)        extends Accumulator
  final case class Last(expression: Expression)         extends Accumulator
  final case class Max(expression: Expression)          extends Accumulator
  final case class Min(expression: Expression)          extends Accumulator
  final case class Push(expression: Expression)         extends Accumulator
  final case class AddToSet(expression: Expression)     extends Accumulator
  final case class MergeObjects(expression: Expression) extends Accumulator
  final case class StdDevPop(expression: Expression)    extends Accumulator
  final case class StdDevSamp(expression: Expression)   extends Accumulator
  final case class Function(
    initFunction: String,
    initArgs: Option[Expression],
    accumulateFunction: String,
    accumulateArgs: Option[Expression],
    mergeFunction: String,
    finalizeFunction: Option[String],
    lang: String,
  ) extends Accumulator
}
