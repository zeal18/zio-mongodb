package io.github.zeal18.zio.mongodb.bson.codecs

import scala.annotation.tailrec

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
import magnolia1.CaseClass.Param

private[codecs] trait MagnoliaCodec extends AutoDerivation[Codec] {
  override def join[T](ctx: CaseClass[Codec, T]): Codec[T] =
    if ctx.isObject then MagnoliaCodec.CaseObjectCodec(ctx)
    else if ctx.isValueClass then MagnoliaCodec.ValueClassCodec(ctx)
    else MagnoliaCodec.CaseClassCodec(ctx)

  override def split[T](ctx: SealedTrait[Codec, T]): Codec[T] = {
    val flattenSubtypes = MagnoliaCodec
      .flattenSealedSubtypes(ctx)
      .toMap
      .keys
      .groupBy(identity)
      .filter(_._2.size > 1)
      .toList match {
      case Nil => ()
      case ambiguous :: _ =>
        throw BsonError.CodecError(
          ctx.typeInfo.full,
          BsonError.GeneralError(
            s"Ambiguous subtypes: ${ambiguous._1}",
          ),
        ) // scalafix:ok
    }

    val isEnum = ctx.subtypes.forall(_.typeclass match {
      case _: MagnoliaCodec.CaseObjectCodec[?] => true
      case _                                   => false
    })
    if isEnum then MagnoliaCodec.EnumCodec(ctx)
    else MagnoliaCodec.SealedTraitCodec(ctx)
  }
}

object MagnoliaCodec {
  private[codecs] case class CaseClassCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit = try {
      writer.writeStartDocument()
      inlined.encode(writer, x, encoderCtx)
      writer.writeEndDocument()
    } catch {
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          ctx.typeInfo.full,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          ctx.typeInfo.full,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonError =>
        throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
    }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = try {
      reader.readStartDocument()
      val result = inlined.decode(reader, decoderCtx)
      reader.readEndDocument()
      result
    } catch {
      case e: BsonSerializationException =>
        throw BsonError.CodecError(
          ctx.typeInfo.full,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonInvalidOperationException =>
        throw BsonError.CodecError(
          ctx.typeInfo.full,
          BsonError.SerializationError(e),
        ) // scalafix:ok
      case e: BsonError =>
        throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
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
        ctx.params
          .filterNot(_.annotations.exists {
            case BsonIgnore() => true
            case _            => false
          })
          .foreach { param =>
            val value      = param.deref(x)
            val childCodec = param.typeclass
            writer.writeName(getLabel(param))
            try
              childCodec.encode(writer, value, encoderCtx)
            catch {
              case e: BsonError =>
                throw BsonError.ProductError(param.label, ctx.typeInfo.short, e) // scalafix:ok
            }
          }
      catch {
        case e: BsonSerializationException => throw BsonError.SerializationError(e) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.SerializationError(e) // scalafix:ok
      }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = {
      @tailrec
      def step(values: Map[String, Any]): A = {
        val typ = reader.readBsonType()
        typ match {
          case BsonType.END_OF_DOCUMENT =>
            ctx
              .constructEither(param =>
                values.get(param.label).orElse(param.default).toRight(param.label),
              )
              .fold(
                e =>
                  throw BsonError
                    .GeneralError(s"Missing fields: ${e.mkString(", ")}"), // scalafix:ok
                identity,
              )
          case _ =>
            val name = reader.readName()
            ctx.params.find(getLabel(_) == name) match {
              case None =>
                // ignore additional fields
                reader.skipValue()
                step(values)
              case Some(param) =>
                val ignored = param.annotations.exists {
                  case BsonIgnore() => true
                  case _            => false
                }

                if ignored && param.default.isEmpty then
                  throw BsonError.ProductError(
                    name,
                    ctx.typeInfo.short,
                    BsonError.GeneralError(
                      s"Field '$name' is ignored but doesn't have a default value",
                    ),
                  ) // scalafix:ok
                else if ignored then {
                  reader.skipValue()
                  step(values)
                } else {
                  val codec = param.typeclass
                  val maybeValue =
                    try
                      Some(param.label -> codec.decode(reader, decoderCtx))
                    catch {
                      case e: BsonError =>
                        throw BsonError.ProductError(
                          param.label,
                          ctx.typeInfo.short,
                          e,
                        ) // scalafix:ok
                    }

                  step(values ++ maybeValue)
                }
            }
        }
      }

      try step(Map.empty)
      catch {
        case e: BsonSerializationException => throw BsonError.SerializationError(e) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.SerializationError(e) // scalafix:ok
      }
    }
  }

  private[codecs] case class CaseObjectCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      try writer.writeString(ctx.typeInfo.short)
      catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
      }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      try {
        val name = reader.readString()
        if name == ctx.typeInfo.short then ctx.construct(_ => ())
        else
          throw BsonError.GeneralError(
            s"Expected '${ctx.typeInfo.short}'', got '$name'.",
          ) // scalafix:ok
      } catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }
  }

  private[codecs] case class ValueClassCodec[A](ctx: CaseClass[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      try {
        val param      = ctx.params.head
        val childValue = param.deref(value)

        param.typeclass.encode(writer, childValue, encoderContext)
      } catch {
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      try {
        val param = ctx.params.head
        val codec = param.typeclass
        val value = codec.decode(reader, decoderContext)

        ctx.construct(_ => ())
      } catch {
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }
  }

  private[codecs] case class EnumCodec[A](ctx: SealedTrait[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      try
        ctx.choose(value)(subtype =>
          subtype.typeclass
            .asInstanceOf[Codec[A]]
            .encode(writer, value, encoderContext), // scalafix:ok
        )
      catch {
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A =
      try {
        val shortName = reader.readString
        val maybeObjectCodec =
          for {
            st <- ctx.subtypes.find(_.typeInfo.short == shortName)
            objectCodec <- st.typeclass match {
              case oc: CaseObjectCodec[?] =>
                Some(oc.asInstanceOf[CaseObjectCodec[A]]) // scalafix:ok
              case _ => None
            }
          } yield objectCodec

        maybeObjectCodec.fold(
          throw BsonError.CoproductError(
            shortName,
            BsonError.GeneralError("unsupported discriminator value"),
          ), // scalafix:ok
        )(_.ctx.construct(_ => ()))
      } catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }
  }

  private[codecs] case class SealedTraitCodec[A](ctx: SealedTrait[Codec, A]) extends Codec[A] {

    private val TypeTag = "_t"

    override def encode(writer: BsonWriter, value: A, encoderCtx: EncoderContext): Unit =
      try
        ctx.choose(value) { subtype =>
          try
            subtype.typeclass match {
              case codec: CaseObjectCodec[?] =>
                writer.writeStartDocument()
                writer.writeName(TypeTag)
                codec.asInstanceOf[Codec[A]].encode(writer, value, encoderCtx) // scalafix:ok
                writer.writeEndDocument()
              case codec: EnumCodec[?] =>
                writer.writeStartDocument()
                writer.writeName(TypeTag)
                codec.asInstanceOf[Codec[A]].encode(writer, value, encoderCtx) // scalafix:ok
                writer.writeEndDocument()
              case codec: CaseClassCodec[?] =>
                writer.writeStartDocument()
                writer.writeString(TypeTag, subtype.typeInfo.short)
                codec.inlined
                  .asInstanceOf[Codec[A]]
                  .encode(
                    writer,
                    value,
                    encoderCtx,
                  ) // scalafix:ok
                writer.writeEndDocument()
              case codec =>
                codec.asInstanceOf[Codec[A]].encode(writer, value, encoderCtx) // scalafix:ok
            }
          catch {
            case e: BsonSerializationException =>
              throw BsonError.CoproductError(
                subtype.typeInfo.short,
                BsonError.SerializationError(e),
              ) // scalafix:ok
            case e: BsonInvalidOperationException =>
              throw BsonError.CoproductError(
                subtype.typeInfo.short,
                BsonError.SerializationError(e),
              ) // scalafix:ok
            case e: BsonError =>
              throw BsonError.CoproductError(subtype.typeInfo.short, e) // scalafix:ok
          }
        }
      catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A =
      try {
        reader.readStartDocument()
        val typeTag         = reader.readString(TypeTag)
        val flattenSubtypes = flattenSealedSubtypes(ctx).toMap
        val codec           = flattenSubtypes.get(typeTag)
        val result = codec
          .map { codec =>
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
                throw BsonError.CoproductError(typeTag, e) // scalafix:ok
            }
          }
          .getOrElse(
            throw BsonError.CoproductError(
              typeTag,
              BsonError.GeneralError("unsupported discriminator value"),
            ), // scalafix:ok
          )
          .asInstanceOf[A] // scalafix:ok
        reader.readEndDocument()

        result
      } catch {
        case e: BsonSerializationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonInvalidOperationException =>
          throw BsonError.CodecError(
            ctx.typeInfo.full,
            BsonError.SerializationError(e),
          ) // scalafix:ok
        case e: BsonError =>
          throw BsonError.CodecError(ctx.typeInfo.full, e) // scalafix:ok
      }
  }

  private[codecs] def flattenSealedSubtypes[A](
    ctx: SealedTrait[Codec, A],
  ): List[(String, Codec[?])] =
    ctx.subtypes.toList.flatMap { subtype =>
      subtype.typeclass match {
        // case codec: CaseObjectCodec[?] => List(subtype.typeInfo.short -> codec)
        // case codec: CaseClassCodec[?] => List(subtype.typeInfo.short -> codec)
        case codec: EnumCodec[?] =>
          codec.ctx.subtypes.map { enumSubtype =>
            enumSubtype.typeInfo.short -> enumSubtype.typeclass
          }
        case codec: SealedTraitCodec[?] => flattenSealedSubtypes(codec.ctx)
        case codec                      => List(subtype.typeInfo.short -> codec)
      }
    }
}
