package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import com.mongodb.client.model.BsonField
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter

sealed trait Accumulator { self =>
  def toBsonField: BsonField = {
    def simpleExpression(operator: String, expression: Expression): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeName(operator)
      expression.encode(writer)
      writer.writeEndDocument()

      writer.getDocument()
    }

    def accumulatorOperator(
      operator: String,
      fieldName: String,
      expression: Expression,
    ): BsonField = new BsonField(fieldName, simpleExpression(operator, expression))

    self match {
      case Accumulator.Sum(name, expression) =>
        accumulatorOperator("$sum", name, expression)
      case Accumulator.Avg(name, expression) =>
        accumulatorOperator("$avg", name, expression)
      case Accumulator.First(name, expression) =>
        accumulatorOperator("$first", name, expression)
      case Accumulator.Last(name, expression) =>
        accumulatorOperator("$last", name, expression)
      case Accumulator.Max(name, expression) =>
        accumulatorOperator("$max", name, expression)
      case Accumulator.Min(name, expression) =>
        accumulatorOperator("$min", name, expression)
      case Accumulator.Push(name, expression) =>
        accumulatorOperator("$push", name, expression)
      case Accumulator.AddToSet(name, expression) =>
        accumulatorOperator("$addToSet", name, expression)
      case Accumulator.MergeObjects(name, expression) =>
        accumulatorOperator("$mergeObjects", name, expression)
      case Accumulator.StdDevPop(name, expression) =>
        accumulatorOperator("$stdDevPop", name, expression)
      case Accumulator.StdDevSamp(name, expression) =>
        accumulatorOperator("$stdDevSamp", name, expression)
      case Accumulator.Function(
            fieldName,
            initFunction,
            initArgs,
            accumulateFunction,
            accumulateArgs,
            mergeFunction,
            finalizeFunction,
            lang,
          ) =>
        val doc = {
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

        new BsonField(fieldName, doc)
    }
  }
}

object Accumulator {
  final case class Sum(name: String, expression: Expression)          extends Accumulator
  final case class Avg(name: String, expression: Expression)          extends Accumulator
  final case class First(name: String, expression: Expression)        extends Accumulator
  final case class Last(name: String, expression: Expression)         extends Accumulator
  final case class Max(name: String, expression: Expression)          extends Accumulator
  final case class Min(name: String, expression: Expression)          extends Accumulator
  final case class Push(name: String, expression: Expression)         extends Accumulator
  final case class AddToSet(name: String, expression: Expression)     extends Accumulator
  final case class MergeObjects(name: String, expression: Expression) extends Accumulator
  final case class StdDevPop(name: String, expression: Expression)    extends Accumulator
  final case class StdDevSamp(name: String, expression: Expression)   extends Accumulator
  final case class Function(
    fieldName: String,
    initFunction: String,
    initArgs: Option[Expression],
    accumulateFunction: String,
    accumulateArgs: Option[Expression],
    mergeFunction: String,
    finalizeFunction: Option[String],
    lang: String,
  ) extends Accumulator
}
