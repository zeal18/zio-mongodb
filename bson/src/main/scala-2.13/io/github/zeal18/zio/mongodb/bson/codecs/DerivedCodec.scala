package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.bson.annotations.BsonIgnore
import io.github.zeal18.zio.mongodb.bson.annotations.BsonProperty
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import magnolia1.*
import org.bson.BsonInvalidOperationException
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

import scala.annotation.tailrec

private[codecs] trait DerivedCodec {
  type Typeclass[A] = Codec[A]

  implicit def derived[A]: Codec[A] = macro Magnolia.gen[A]

  def join[A](ctx: CaseClass[Typeclass, A]): Codec[A] =
    if (ctx.isObject) DerivedCodec.CaseObjectCodec(ctx)
    else if (ctx.isValueClass) DerivedCodec.ValueClassCodec(ctx)
    else DerivedCodec.CaseClassCodec(ctx)

  def split[A](ctx: SealedTrait[Typeclass, A]): Codec[A] = {
    ctx.subtypes.groupBy(_.typeName.short).filter(_._2.size > 1).toList match {
      case Nil            => ()
      case ambiguous :: _ =>
        throw BsonError.CodecError(
          ctx.typeName.full,
          BsonError.GeneralError(
            s"Ambiguous subtypes: ${ambiguous._2.map(_.typeName.full).mkString(", ")}",
          ),
        )
    }

    val isEnum = ctx.subtypes.forall(_.typeclass match {
      case _: DerivedCodec.CaseObjectCodec[?] => true
      case _                                  => false
    })
    if (isEnum) DerivedCodec.EnumCodec(ctx)
    else DerivedCodec.SealedTraitCodec(ctx)
  }
}

object DerivedCodec {
  private[codecs] case class CaseClassCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit = try {
      writer.writeStartDocument()
      inlined.encode(writer, x, encoderCtx)
      writer.writeEndDocument()
    } catch {
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          ctx.typeName.full,
          BsonError.SerializationError(e),
        )
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          ctx.typeName.full,
          BsonError.SerializationError(e),
        )
      case e: BsonError =>
        throw BsonError.CodecError(ctx.typeName.full, e)
    }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = try {
      reader.readStartDocument()
      val result = inlined.decode(reader, decoderCtx)
      reader.readEndDocument()
      result
    } catch {
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          ctx.typeName.full,
          BsonError.SerializationError(e),
        )
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          ctx.typeName.full,
          BsonError.SerializationError(e),
        )
      case e: BsonError =>
        throw BsonError.CodecError(ctx.typeName.full, e)
    }

    val inlined: InlinedCaseClassCodec[A] = InlinedCaseClassCodec(ctx)
  }

  private[codecs] case class InlinedCaseClassCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {

    private def getLabel[S](p: Param[Codec, S]): String =
      p.annotations
        .collectFirst {
          case BsonId()            => "_id"
          case BsonProperty(label) => label
        }
        .getOrElse(p.label)

    override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit =
      try
        ctx.parameters
          .filterNot(_.annotations.exists {
            case BsonIgnore() => true
            case _            => false
          })
          .foreach { param =>
            val value      = param.dereference(x)
            val childCodec = param.typeclass
            writer.writeName(getLabel(param))
            try
              childCodec.encode(writer, value, encoderCtx)
            catch {
              case e: BsonError =>
                throw BsonError.ProductError(param.label, param.typeName.short, e)
            }
          }
      catch {
        case e: BsonSerializationException    => throw BsonError.SerializationError(e)
        case e: BsonInvalidOperationException =>
          throw BsonError.SerializationError(e)
      }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = {
      @tailrec
      def step(values: Map[String, Any]): A = {
        val typ = reader.readBsonType()
        typ match {
          case BsonType.END_OF_DOCUMENT =>
            ctx
              .constructEither(param => values.get(param.label).orElse(param.default).toRight(param.label))
              .fold(
                e => throw BsonError.GeneralError(s"Missing fields: ${e.mkString(", ")}"),
                identity,
              )
          case _ =>
            val name = reader.readName()
            ctx.parameters.find(getLabel(_) == name) match {
              case None =>
                // ignore additional fields
                reader.skipValue()
                step(values)
              case Some(param) =>
                val ignored = param.annotations.exists {
                  case BsonIgnore() => true
                  case _            => false
                }

                if (ignored && param.default.isEmpty)
                  throw BsonError.ProductError(
                    name,
                    param.typeName.short,
                    BsonError.GeneralError(
                      s"Field '$name' is ignored but doesn't have a default value",
                    ),
                  )
                else if (ignored) {
                  reader.skipValue()
                  step(values)
                } else {
                  val codec      = param.typeclass
                  val maybeValue =
                    try
                      Some(param.label -> codec.decode(reader, decoderCtx))
                    catch {
                      case e: BsonError =>
                        throw BsonError.ProductError(
                          param.label,
                          param.typeName.short,
                          e,
                        )
                    }

                  step(values ++ maybeValue)
                }
            }
        }
      }

      try step(Map.empty)
      catch {
        case e: BsonSerializationException    => throw BsonError.SerializationError(e)
        case e: BsonInvalidOperationException =>
          throw BsonError.SerializationError(e)
      }
    }
  }

  private[codecs] case class CaseObjectCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      try writer.writeString(ctx.typeName.short)
      catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
      }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      try {
        val name = reader.readString()
        if (name == ctx.typeName.short) ctx.construct(_ => ())
        else
          throw BsonError.GeneralError(
            s"Expected '${ctx.typeName.short}'', got '$name'.",
          )
      } catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }
  }

  private[codecs] case class ValueClassCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      try {
        val param      = ctx.parameters.head
        val childValue = param.dereference(value)

        param.typeclass.encode(writer, childValue, encoderContext)
      } catch {
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      try {
        val param = ctx.parameters.head
        val codec = param.typeclass
        val value = codec.decode(reader, decoderContext)

        ctx.construct(_ => value)
      } catch {
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }
  }

  private[codecs] case class EnumCodec[A](ctx: SealedTrait[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      try
        ctx.split(value)(subtype => subtype.typeclass.encode(writer, value.asInstanceOf[subtype.SType], encoderContext))
      catch {
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      try {
        val shortName        = reader.readString
        val maybeObjectCodec =
          for {
            st          <- ctx.subtypes.find(_.typeName.short == shortName)
            objectCodec <- st.typeclass match {
              case oc: CaseObjectCodec[?] =>
                Some(oc.asInstanceOf[CaseObjectCodec[A]])
              case _ => None
            }
          } yield objectCodec

        maybeObjectCodec.fold(
          throw BsonError.CoproductError(
            shortName,
            BsonError.GeneralError("unsupported discriminator value"),
          ),
        )(_.ctx.construct(_ => ()))
      } catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }
  }

  private[codecs] case class SealedTraitCodec[A](ctx: SealedTrait[Codec, A]) extends Codec[A] {

    private val TypeTag = "_t"

    override def encode(writer: BsonWriter, value: A, encoderCtx: EncoderContext): Unit =
      try
        ctx.split(value) { subtype =>
          try {
            writer.writeStartDocument()
            subtype.typeclass match {
              case codec: CaseObjectCodec[?] =>
                writer.writeName(TypeTag)
                codec.encode(writer, value.asInstanceOf[subtype.SType], encoderCtx)
              case codec: CaseClassCodec[?] =>
                writer.writeString(TypeTag, subtype.typeName.short)
                codec.inlined.encode(
                  writer,
                  value.asInstanceOf[subtype.SType],
                  encoderCtx,
                )
              case codec =>
                writer.writeString(TypeTag, subtype.typeName.short)
                codec.encode(writer, value.asInstanceOf[subtype.SType], encoderCtx)
            }
            writer.writeEndDocument()
          } catch {
            case e: BsonSerializationException =>
              throw BsonError.CoproductError(
                subtype.typeName.short,
                BsonError.SerializationError(e),
              )
            case e: BsonInvalidOperationException =>
              throw BsonError.CoproductError(
                subtype.typeName.short,
                BsonError.SerializationError(e),
              )
            case e: BsonError =>
              throw BsonError.CoproductError(subtype.typeName.short, e)
          }
        }
      catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A =
      try {
        reader.readStartDocument()
        val typeTag = reader.readString(TypeTag)
        val result  = ctx.subtypes
          .collectFirst {
            case st if st.typeName.short == typeTag =>
              val codec = st.typeclass
              try
                codec match {
                  case CaseObjectCodec(objectCtx) =>
                    objectCtx.construct(_ => ())
                  case codec: CaseClassCodec[?] =>
                    codec.inlined.decode(reader, decoderCtx)
                  case _ =>
                    codec.decode(reader, decoderCtx)
                }
              catch {
                case e: BsonError =>
                  throw BsonError.CoproductError(st.typeName.short, e)
              }
          }
          .getOrElse(
            throw BsonError.CoproductError(
              typeTag,
              BsonError.GeneralError("unsupported discriminator value"),
            ),
          )
        reader.readEndDocument()

        result
      } catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeName.full,
            BsonError.SerializationError(e),
          )
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeName.full, e)
      }
  }
}
