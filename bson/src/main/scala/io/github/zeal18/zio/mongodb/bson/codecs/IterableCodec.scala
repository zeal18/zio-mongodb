/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zeal18.zio.mongodb.bson.codecs

import java.util.UUID

import scala.collection.mutable

import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistry

/** IterableCodec companion object
  */
object IterableCodec {

  def apply(registry: CodecRegistry, bsonTypeClassMap: BsonTypeClassMap): IterableCodec =
    apply(registry, bsonTypeClassMap, None)

  def apply(
    registry: CodecRegistry,
    bsonTypeClassMap: BsonTypeClassMap,
    valueTransformer: Option[Transformer],
  ): IterableCodec =
    new IterableCodec(registry, bsonTypeClassMap, valueTransformer.getOrElse(DEFAULT_TRANSFORMER))

  private val DEFAULT_TRANSFORMER = new Transformer() {
    def transform(objectToTransform: Object): Object = objectToTransform
  }
}

/** Encodes and decodes `Iterable` objects.
  */
@SuppressWarnings(Array("rawtypes"))
case class IterableCodec(
  registry: CodecRegistry,
  bsonTypeClassMap: BsonTypeClassMap,
  valueTransformer: Transformer,
) extends Codec[Iterable[? <: Any]] {
  lazy val bsonTypeCodecMap = new BsonTypeCodecMap(bsonTypeClassMap, registry)

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Iterable[?] =
    readValue(reader, decoderContext).asInstanceOf[Iterable[?]] // scalafix:ok

  override def encode(
    writer: BsonWriter,
    value: Iterable[? <: Any],
    encoderContext: EncoderContext,
  ): Unit =
    writeValue(writer, encoderContext, value)

  override def getEncoderClass: Class[Iterable[?]] = classOf[Iterable[?]]

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  private def writeValue[T](writer: BsonWriter, encoderContext: EncoderContext, value: T): Unit =
    value match {
      case _ if value == null => writer.writeNull()
      case map: Map[?, ?] =>
        writeMap(writer, map.asInstanceOf[Map[String, Any]], encoderContext.getChildContext)
      case list: Iterable[?] =>
        writeIterable(writer, list, encoderContext.getChildContext)
      case _ =>
        val codec = registry.get(value.getClass).asInstanceOf[Encoder[T]]
        encoderContext.encodeWithChildContext(codec, writer, value)
    } // scalafix:ok

  private def writeMap(
    writer: BsonWriter,
    map: Map[String, Any],
    encoderContext: EncoderContext,
  ): Unit = {
    writer.writeStartDocument()
    map.foreach { kv =>
      writer.writeName(kv._1)
      writeValue(writer, encoderContext, kv._2)
    }
    writer.writeEndDocument()
  }

  private def writeIterable(
    writer: BsonWriter,
    list: Iterable[?],
    encoderContext: EncoderContext,
  ): Unit = {
    writer.writeStartArray()
    list.foreach(value => writeValue(writer, encoderContext, value))
    writer.writeEndArray()
  }

  private def readValue(reader: BsonReader, decoderContext: DecoderContext): Any =
    reader.getCurrentBsonType match {
      case BsonType.NULL =>
        reader.readNull()
        null // scalafix:ok
      case BsonType.ARRAY    => readList(reader, decoderContext)
      case BsonType.DOCUMENT => readMap(reader, decoderContext)
      case BsonType.BINARY
          if BsonBinarySubType.isUuid(reader.peekBinarySubType) && reader.peekBinarySize == 16 =>
        registry.get(classOf[UUID]).decode(reader, decoderContext)
      case bsonType: BsonType =>
        valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext))
    }

  private def readMap(reader: BsonReader, decoderContext: DecoderContext): Map[String, ?] = {
    val map = mutable.Map[String, Any]()
    reader.readStartDocument()
    while (reader.readBsonType ne BsonType.END_OF_DOCUMENT) // scalafix:ok
      map += (reader.readName -> readValue(reader, decoderContext))
    reader.readEndDocument()
    map.toMap
  }

  private def readList(reader: BsonReader, decoderContext: DecoderContext): List[?] = {
    reader.readStartArray()
    val list = mutable.ListBuffer[Any]()
    while (reader.readBsonType ne BsonType.END_OF_DOCUMENT) // scalafix:ok
      list.append(readValue(reader, decoderContext))
    reader.readEndArray()
    list.toList
  }
}
