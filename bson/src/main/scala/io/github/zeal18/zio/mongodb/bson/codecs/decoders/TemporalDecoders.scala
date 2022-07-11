package io.github.zeal18.zio.mongodb.bson.codecs.decoders

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import io.github.zeal18.zio.mongodb.bson.codecs.Decoder
import org.bson.codecs.jsr310.InstantCodec
import org.bson.codecs.jsr310.LocalDateCodec
import org.bson.codecs.jsr310.LocalDateTimeCodec
import org.bson.codecs.jsr310.LocalTimeCodec

trait TemporalDecoders {
  implicit lazy val instantDecoder: Decoder[Instant]     = Decoder[Instant](new InstantCodec())
  implicit lazy val localDateDecoder: Decoder[LocalDate] = Decoder[LocalDate](new LocalDateCodec())
  implicit lazy val localTimeDecoder: Decoder[LocalTime] = Decoder[LocalTime](new LocalTimeCodec())
  implicit lazy val localDateTimeDecoder: Decoder[LocalDateTime] =
    Decoder[LocalDateTime](new LocalDateTimeCodec())
}
