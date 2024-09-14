package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.ZIOSpecDefault

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

object TemporalCodecsSpec extends ZIOSpecDefault {
  override def spec = suite("TemporalCodecsSpec")(
    suite("Instant")(
      testCodecRoundtrip[Instant](
        "epoch",
        Instant.EPOCH,
        """{"$date": "1970-01-01T00:00:00Z"}""",
      ),
      testCodecRoundtrip[Instant](
        "epoch 1657524265825",
        Instant.ofEpochMilli(1657524265825L),
        """{"$date": "2022-07-11T07:24:25.825Z"}""",
      ),
      testCodecRoundtrip[Instant](
        "max millis",
        Instant.ofEpochMilli(Long.MaxValue),
        """{"$date": {"$numberLong": "9223372036854775807"}}""",
      ),
      testCodecRoundtrip[Instant](
        "min millis",
        Instant.ofEpochMilli(Long.MinValue),
        """{"$date": {"$numberLong": "-9223372036854775808"}}""",
      ),
    ),
    suite("LocalDate")(
      testCodecRoundtrip[LocalDate](
        "epoch",
        LocalDate.of(1970, 1, 1),
        """{"$date": "1970-01-01T00:00:00Z"}""",
      ),
      testCodecRoundtrip[LocalDate](
        "epoch 1657524265825",
        LocalDate.parse("2022-07-11"),
        """{"$date": "2022-07-11T00:00:00Z"}""",
      ),
      testCodecRoundtrip[LocalDate](
        "max",
        LocalDate.parse("+292278994-08-17"),
        """{"$date": {"$numberLong": "9223372036828800000"}}""",
      ),
      testCodecRoundtrip[LocalDate](
        "min",
        LocalDate.parse("-292275055-05-17"),
        """{"$date": {"$numberLong": "-9223372036828800000"}}""",
      ),
    ),
    suite("LocalTime")(
      testCodecRoundtrip[LocalTime](
        "epoch",
        LocalTime.MIDNIGHT,
        """{"$date": "1970-01-01T00:00:00Z"}""",
      ),
      testCodecRoundtrip[LocalTime](
        "1 second",
        LocalTime.ofSecondOfDay(1),
        """{"$date": "1970-01-01T00:00:01Z"}""",
      ),
      testCodecRoundtrip[LocalTime](
        "epoch 1657524265825",
        LocalTime.parse("07:24:25.825"),
        """{"$date": "1970-01-01T07:24:25.825Z"}""",
      ),
      testCodecRoundtrip[LocalTime](
        "max",
        LocalTime.parse("23:59:59.999"),
        """{"$date": "1970-01-01T23:59:59.999Z"}""",
      ),
      testCodecRoundtrip[LocalTime](
        "min",
        LocalTime.MIN,
        """{"$date": "1970-01-01T00:00:00Z"}""",
      ),
    ),
    suite("LocalDateTime")(
      testCodecRoundtrip[LocalDateTime](
        "epoch",
        LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
        """{"$date": "1970-01-01T00:00:00Z"}""",
      ),
      testCodecRoundtrip[LocalDateTime](
        "epoch 1657524265825",
        LocalDateTime.ofInstant(Instant.ofEpochMilli(1657524265825L), ZoneOffset.UTC),
        """{"$date": "2022-07-11T07:24:25.825Z"}""",
      ),
      testCodecRoundtrip[LocalDateTime](
        "max",
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.MaxValue), ZoneOffset.UTC),
        """{"$date": {"$numberLong": "9223372036854775807"}}""",
      ),
      testCodecRoundtrip[LocalDateTime](
        "min",
        LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.MinValue), ZoneOffset.UTC),
        """{"$date": {"$numberLong": "-9223372036854775808"}}""",
      ),
    ),
  )
}
