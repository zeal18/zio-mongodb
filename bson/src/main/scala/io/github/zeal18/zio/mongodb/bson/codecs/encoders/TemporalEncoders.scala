package io.github.zeal18.zio.mongodb.bson.codecs.encoders

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.jsr310.LocalTimeCodec

trait TemporalEncoders {
  implicit lazy val instantEncoder: Encoder[Instant]     = Encoder[Instant](new InstantCodec())
  implicit lazy val localDateEncoder: Encoder[LocalDate] = Encoder[LocalDate](new LocalDateCodec())
  implicit lazy val localTimeEncoder: Encoder[LocalTime] = Encoder[LocalTime](new LocalTimeCodec())
  implicit lazy val localDateTimeEncoder: Encoder[LocalDateTime] =
    Encoder[LocalDateTime](new LocalDateTimeCodec())
}
