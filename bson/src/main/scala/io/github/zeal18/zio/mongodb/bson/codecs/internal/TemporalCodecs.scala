package io.github.zeal18.zio.mongodb.bson.codecs.internal

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.jsr310.LocalTimeCodec

private[codecs] trait TemporalCodecs {
  implicit lazy val instant: Codec[Instant]     = Codec[Instant](new InstantCodec())
  implicit lazy val localDate: Codec[LocalDate] = Codec[LocalDate](new LocalDateCodec())
  implicit lazy val localTime: Codec[LocalTime] = Codec[LocalTime](new LocalTimeCodec())
  implicit lazy val localDateTime: Codec[LocalDateTime] =
    Codec[LocalDateTime](new LocalDateTimeCodec())
}
