package io.github.zeal18.zio.mongodb.driver.aggregates.accumulators

import scala.jdk.CollectionConverters.*

import com.mongodb.client.model.BsonField
import com.mongodb.client.model.Accumulators as JAccumulators
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import org.bson.BsonDocument
import org.bson.BsonDocumentWriter
import org.bson.codecs.EncoderContext

sealed trait Accumulator { self =>
  def toBsonField: BsonField = {
    def simpleExpression[A](fieldName: String, value: A, codec: Codec[A]): BsonDocument = {
      val writer = new BsonDocumentWriter(new BsonDocument())

      writer.writeStartDocument()
      writer.writeName(fieldName)
      codec.encode(writer, value, EncoderContext.builder().build())
      writer.writeEndDocument()

      writer.getDocument()
    }

    def accumulatorOperator[A](
      operator: String,
      fieldName: String,
      expression: A,
      codec: Codec[A],
    ): BsonField = new BsonField(fieldName, simpleExpression(operator, expression, codec))

    self match {
      case Accumulator.Sum(name, expression, codec) =>
        accumulatorOperator("$sum", name, expression, codec)
      case Accumulator.Avg(name, expression, codec) =>
        accumulatorOperator("$avg", name, expression, codec)
      case Accumulator.First(name, expression, codec) =>
        accumulatorOperator("$first", name, expression, codec)
      case Accumulator.Last(name, expression, codec) =>
        accumulatorOperator("$last", name, expression, codec)
      case Accumulator.Max(name, expression, codec) =>
        accumulatorOperator("$max", name, expression, codec)
      case Accumulator.Min(name, expression, codec) =>
        accumulatorOperator("$min", name, expression, codec)
      case Accumulator.Push(name, expression, codec) =>
        accumulatorOperator("$push", name, expression, codec)
      case Accumulator.AddToSet(name, expression, codec) =>
        accumulatorOperator("$addToSet", name, expression, codec)
      case Accumulator.MergeObjects(name, expression, codec) =>
        accumulatorOperator("$mergeObjects", name, expression, codec)
      case Accumulator.StdDevPop(name, expression, codec) =>
        accumulatorOperator("$stdDevPop", name, expression, codec)
      case Accumulator.StdDevSamp(name, expression, codec) =>
        accumulatorOperator("$stdDevSamp", name, expression, codec)
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
        JAccumulators.accumulator(
          fieldName,
          initFunction,
          initArgs.asJava,
          accumulateFunction,
          accumulateArgs.asJava,
          mergeFunction,
          finalizeFunction.orNull,
          lang,
        )
    }
  }
}

object Accumulator {
  final case class Sum[A](name: String, expression: A, codec: Codec[A])          extends Accumulator
  final case class Avg[A](name: String, expression: A, codec: Codec[A])          extends Accumulator
  final case class First[A](name: String, expression: A, codec: Codec[A])        extends Accumulator
  final case class Last[A](name: String, expression: A, codec: Codec[A])         extends Accumulator
  final case class Max[A](name: String, expression: A, codec: Codec[A])          extends Accumulator
  final case class Min[A](name: String, expression: A, codec: Codec[A])          extends Accumulator
  final case class Push[A](name: String, expression: A, codec: Codec[A])         extends Accumulator
  final case class AddToSet[A](name: String, expression: A, codec: Codec[A])     extends Accumulator
  final case class MergeObjects[A](name: String, expression: A, codec: Codec[A]) extends Accumulator
  final case class StdDevPop[A](name: String, expression: A, codec: Codec[A])    extends Accumulator
  final case class StdDevSamp[A](name: String, expression: A, codec: Codec[A])   extends Accumulator
  final case class Function(
    fieldName: String,
    initFunction: String,
    initArgs: Seq[String],
    accumulateFunction: String,
    accumulateArgs: Seq[String],
    mergeFunction: String,
    finalizeFunction: Option[String],
    lang: String,
  ) extends Accumulator
}
