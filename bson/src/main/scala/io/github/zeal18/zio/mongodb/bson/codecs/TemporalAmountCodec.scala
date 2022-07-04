package io.github.zeal18.zio.mongodb.bson.codecs

import java.time.Duration
import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit

import scala.jdk.CollectionConverters.*

import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

class TemporalAmountCodec extends Codec[TemporalAmount] {

  override def encode(writer: BsonWriter, amount: TemporalAmount, ctx: EncoderContext): Unit = {
    writer.writeStartDocument()
    amount.getUnits().asScala.foreach {
      case unit: ChronoUnit =>
        writer.writeInt64(unit.name, amount.get(unit))
      case other =>
        throw new BsonSerializationException(
          s"Can't encode $other. Only ChronoUnit is supported.",
        ) // scalafix:ok
    }
    writer.writeEndDocument()
  }

  override def getEncoderClass(): Class[TemporalAmount] = classOf[TemporalAmount]

  override def decode(reader: BsonReader, ctx: DecoderContext): TemporalAmount = {
    reader.readStartDocument()
    val byUnit =
      List
        .unfold(reader.readBsonType()) {
          case BsonType.INT64 =>
            val name               = reader.readName()
            val unit: TemporalUnit = ChronoUnit.valueOf(name)
            val value              = reader.readInt64()
            Some(((unit, value), reader.readBsonType()))
          case BsonType.INT32 =>
            val name               = reader.readName()
            val unit: TemporalUnit = ChronoUnit.valueOf(name)
            val value              = reader.readInt32().toLong
            Some(((unit, value), reader.readBsonType()))
          case BsonType.END_OF_DOCUMENT =>
            reader.readEndDocument()
            None
          case other =>
            throw new BsonSerializationException(
              s"Can't decode $other. ChronoUnits must be encoded as Long.",
            ) // scalafix:ok
        }
        .toMap
    mapToTemporal(byUnit)
  }

  private def mapToTemporal(byUnit: Map[TemporalUnit, Long]) = {
    val DurationUnits =
      List(
        ChronoUnit.HOURS,
        ChronoUnit.MINUTES,
        ChronoUnit.SECONDS,
        ChronoUnit.MILLIS,
        ChronoUnit.NANOS,
      )
    val PeriodUnits =
      List(
        ChronoUnit.YEARS,
        ChronoUnit.MONTHS,
        ChronoUnit.DAYS,
      )
    if (byUnit.keys.toSeq.diff(DurationUnits).isEmpty)
      byUnit.foldLeft(Duration.ZERO) { case (temporal, (unit, amount)) =>
        temporal.plus(amount, unit)
      }
    else if (byUnit.keys.toSeq.diff(PeriodUnits).isEmpty) {
      val years  = byUnit.getOrElse(ChronoUnit.YEARS, 0L).toInt
      val months = byUnit.getOrElse(ChronoUnit.MONTHS, 0L).toInt
      val days   = byUnit.getOrElse(ChronoUnit.DAYS, 0L).toInt
      Period.of(years, months, days)
    } else
      throw new BsonSerializationException(
        s"Can't create TemporalAmount from $byUnit. Supported are java.time.Duration and java.time.Period.",
      ) // scalafix:ok

  }

}
