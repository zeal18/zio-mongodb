package io.github.zeal18.zio.mongodb.bson.codecs

import org.bson.BsonType
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.BsonSerializationException
import org.bson.BsonInvalidOperationException
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError

import scala.annotation.tailrec

/** Flatten version of the CaseClass codec,
  * it works with fields in a "current" context without creating/reading a document.
  *
  * For example, it's used for encoding coproducts when we create a document, write
  * the discriminator field then we need just to add all fields to the same document.
  */
class FlatCaseClassCodec[A](ctx: CaseClass[A]) extends Codec[A]:
  override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit =
    try
      ctx.fields.foreach { param =>
        val value      = param.deref(x)
        val childCodec = param.codec()
        writer.writeName(param.label)

        try childCodec.encode(writer, value, encoderCtx)
        catch
          case e: BsonError =>
            throw BsonError.ProductError(param.label, ctx.shortName, e) // scalafix:ok
      }
    catch
      case e: BsonSerializationException    => throw BsonError.SerializationError(e) // scalafix:ok
      case e: BsonInvalidOperationException => throw BsonError.SerializationError(e) // scalafix:ok

  override def decode(reader: BsonReader, decoderCtx: DecoderContext): A =
    @tailrec
    def step(values: Map[String, Any]): A =
      val typ = reader.readBsonType()
      typ match
        case BsonType.END_OF_DOCUMENT =>
          ctx.decode(values)
        case _ =>
          val name = reader.readName()
          ctx.fields.find(_.label == name) match
            case None =>
              // ignore additional fields
              reader.skipValue()
              step(values)
            case Some(field) =>
              val codec = field.codec()
              val maybeValue =
                try Some(field.label -> codec.decode(reader, decoderCtx))
                catch
                  case e: BsonError =>
                    throw BsonError.ProductError(field.label, ctx.shortName, e) // scalafix:ok

              step(values ++ maybeValue)
    end step

    try step(Map.empty)
    catch
      case e: BsonSerializationException    => throw BsonError.SerializationError(e) // scalafix:ok
      case e: BsonInvalidOperationException => throw BsonError.SerializationError(e) // scalafix:ok
end FlatCaseClassCodec
