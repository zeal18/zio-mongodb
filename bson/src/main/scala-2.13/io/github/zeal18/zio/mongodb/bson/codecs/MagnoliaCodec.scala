package io.github.zeal18.zio.mongodb.bson.codecs

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.Try

import io.github.zeal18.zio.mongodb.bson.annotations.BsonIgnore
import io.github.zeal18.zio.mongodb.bson.annotations.BsonProperty
import magnolia1.*
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

private[codecs] trait MagnoliaCodec {
  type Typeclass[A] = Codec[A]

  implicit def derive[A]: Codec[A] = macro Magnolia.gen[A]

  def join[A: ClassTag](ctx: CaseClass[Typeclass, A]): Codec[A] =
    if (ctx.isObject) MagnoliaCodec.CaseObjectCodec(ctx)
    else if (ctx.isValueClass) MagnoliaCodec.ValueClassCodec(ctx)
    else MagnoliaCodec.CaseClassCodec(ctx)

  def split[A: ClassTag](ctx: SealedTrait[Typeclass, A]): Codec[A] = {
    ctx.subtypes.groupBy(_.typeName.short).filter(_._2.size > 1).toList match {
      case Nil => ()
      case ambiguous :: _ =>
        throw new BsonSerializationException(
          s"Ambiguous subtypes for sealed trait ${ctx.typeName.short}: ${ambiguous._2.map(_.typeName.full).mkString(", ")}",
        ) // scalafix:ok
    }

    val isEnum = ctx.subtypes.forall(_.typeclass match {
      case _: MagnoliaCodec.CaseObjectCodec[?] => true
      case _                                   => false
    })
    if (isEnum) MagnoliaCodec.EnumCodec(ctx)
    else MagnoliaCodec.SealedTraitCodec(ctx)
  }
}

object MagnoliaCodec {
  private[codecs] case class CaseClassCodec[A: ClassTag](ctx: CaseClass[Codec, A])
      extends Codec[A] {
    override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit = {
      writer.writeStartDocument()
      inlined.encode(writer, x, encoderCtx)
      writer.writeEndDocument()
    }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = {
      reader.readStartDocument()
      val result = inlined.decode(reader, decoderCtx)
      reader.readEndDocument()
      result
    }

    val inlined: InlinedCaseClassCodec[A] = InlinedCaseClassCodec(ctx)
  }

  private[codecs] case class InlinedCaseClassCodec[A: ClassTag](ctx: CaseClass[Codec, A])
      extends Codec[A] {

    private def getLabel[S](p: Param[Codec, S]): String =
      p.annotations.collectFirst { case BsonProperty(label) => label }.getOrElse(p.label)

    override def encode(writer: BsonWriter, x: A, encoderCtx: EncoderContext): Unit =
      ctx.parameters
        .filterNot(_.annotations.exists {
          case BsonIgnore() => true
          case _            => false
        })
        .foreach { param =>
          val value      = param.dereference(x)
          val childCodec = param.typeclass
          writer.writeName(getLabel(param))
          childCodec.encode(writer, value, encoderCtx)
        }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = {
      @tailrec
      def step(values: Map[String, Any], errors: List[String]): Either[List[String], A] = {
        val typ = reader.readBsonType()
        typ match {
          case BsonType.END_OF_DOCUMENT =>
            ctx
              .constructEither(param =>
                values
                  .get(param.label)
                  .orElse(param.default)
                  .toRight(s"missing field ${param.label}"),
              )
              .left
              .map(_ ::: errors)
          case _ =>
            val name = reader.readName()
            ctx.parameters.find(getLabel(_) == name) match {
              case None =>
                // ignore additional fields
                reader.skipValue()
                step(values, errors)
              case Some(param) =>
                val ignored = param.annotations.exists {
                  case BsonIgnore() => true
                  case _            => false
                }

                if (ignored && param.default.isEmpty)
                  throw new BsonSerializationException(
                    s"Field '$name' is ignored but doesn't have a default value",
                  ) // scalafix:ok
                else if (ignored) {
                  reader.skipValue()
                  step(values, errors)
                } else {
                  val codec = param.typeclass
                  val (maybeValue, additionalErrors) =
                    Try(codec.decode(reader, decoderCtx))
                      .fold(e => (None, List(e.getMessage)), v => (Some(param.label -> v), Nil))
                  step(values ++ maybeValue, additionalErrors ::: errors)
                }
            }
        }
      }

      step(Map.empty, Nil).fold(
        e =>
          throw new BsonSerializationException(
            s"Error decoding ${ctx.typeName.full}: ${e.mkString("[", ", ", "]")}.",
          ), // scalafix:ok
        identity,
      )
    }
  }

  private[codecs] case class CaseObjectCodec[A: ClassTag](ctx: CaseClass[Codec, A])
      extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      writer.writeString(ctx.typeName.short)

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A = {
      val name = reader.readString()
      if (name == ctx.typeName.short) ctx.construct(_ => ())
      else
        throw new BsonSerializationException(
          s"Expected ${ctx.typeName.short}, got $name.",
        ) // scalafix:ok
    }
  }

  private[codecs] case class ValueClassCodec[A: ClassTag](ctx: CaseClass[Codec, A])
      extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit = {
      val param      = ctx.parameters.head
      val childValue = param.dereference(value)

      param.typeclass.encode(writer, childValue, encoderContext)
    }

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A = {
      val param = ctx.parameters.head
      val codec = param.typeclass
      val value = codec.decode(reader, decoderContext)

      ctx.construct(_ => value)
    }
  }

  private[codecs] case class EnumCodec[A: ClassTag](ctx: SealedTrait[Codec, A]) extends Codec[A] {
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit =
      ctx.split(value)(subtype =>
        subtype.typeclass
          .encode(writer, value.asInstanceOf[subtype.SType], encoderContext), // scalafix:ok
      )

    override def decode(reader: BsonReader, decoderContext: DecoderContext): A = {
      val shortName = reader.readString
      val maybeObjectCodec =
        for {
          st <- ctx.subtypes.find(_.typeName.short == shortName)
          objectCodec <- st.typeclass match {
            case oc: CaseObjectCodec[?] => Some(oc.asInstanceOf[CaseObjectCodec[A]]) // scalafix:ok
            case _                      => None
          }
        } yield objectCodec

      maybeObjectCodec.fold(
        throw new BsonSerializationException(
          s"could not find object codec for $shortName",
        ), // scalafix:ok
      )(_.ctx.construct(_ => ()))
    }

  }

  private[codecs] case class SealedTraitCodec[A: ClassTag](ctx: SealedTrait[Codec, A])
      extends Codec[A] {

    private val TypeTag = "_t"

    override def encode(writer: BsonWriter, value: A, encoderCtx: EncoderContext): Unit =
      ctx.split(value) { subtype =>
        writer.writeStartDocument()
        subtype.typeclass match {
          case codec: CaseObjectCodec[?] =>
            writer.writeName(TypeTag)
            codec.encode(writer, value.asInstanceOf[subtype.SType], encoderCtx) // scalafix:ok
          case codec: CaseClassCodec[?] =>
            writer.writeString(TypeTag, subtype.typeName.short)
            codec.inlined.encode(
              writer,
              value.asInstanceOf[subtype.SType],
              encoderCtx,
            ) // scalafix:ok
          case codec =>
            writer.writeString(TypeTag, subtype.typeName.short)
            codec.encode(writer, value.asInstanceOf[subtype.SType], encoderCtx) // scalafix:ok
        }
        writer.writeEndDocument()
      }

    override def decode(reader: BsonReader, decoderCtx: DecoderContext): A = {
      reader.readStartDocument()
      val typeTag = reader.readString(TypeTag)
      val result = ctx.subtypes
        .collectFirst {
          case st if st.typeName.short == typeTag =>
            val codec = st.typeclass
            codec match {
              case CaseObjectCodec(objectCtx) =>
                objectCtx.construct(_ => ())
              case codec: CaseClassCodec[?] =>
                codec.inlined.decode(reader, decoderCtx)
              case _ =>
                codec.decode(reader, decoderCtx)
            }
        }
        .getOrElse(
          throw new BsonSerializationException(
            s"no suitable child codec found for type tag $typeTag.",
          ), // scalafix:ok
        )
      reader.readEndDocument()

      result
    }
  }
}
