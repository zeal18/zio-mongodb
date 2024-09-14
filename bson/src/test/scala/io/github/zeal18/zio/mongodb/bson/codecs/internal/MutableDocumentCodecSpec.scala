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

package io.github.zeal18.zio.mongodb.bson.codecs.internal

import io.github.zeal18.zio.mongodb.bson.codecs.internal.Registry.DEFAULT_CODEC_REGISTRY
import io.github.zeal18.zio.mongodb.bson.collection.mutable
import io.github.zeal18.zio.mongodb.bson.collection.mutable.Document
import org.bson.*
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.io.BasicOutputBuffer
import org.bson.io.ByteBufferBsonInput
import org.bson.types.ObjectId
import zio.test.*

import java.nio.ByteBuffer
import java.util.Date
import scala.jdk.CollectionConverters.*

object MutableDocumentCodecSpec extends ZIOSpecDefault {
  val registry: CodecRegistry = DEFAULT_CODEC_REGISTRY

  override def spec = suite("MutableDocumentCodecSpec")(
    test("should encode and decode all default types with readers and writers") {
      val original: mutable.Document = Document(
        "binary"       -> new BsonBinary("bson".toCharArray map (_.toByte)),
        "boolean"      -> new BsonBoolean(true),
        "dateTime"     -> new BsonDateTime(new Date().getTime),
        "double"       -> new BsonDouble(1.0),
        "int"          -> new BsonInt32(1),
        "long"         -> new BsonInt64(1L),
        "null"         -> new BsonNull(),
        "objectId"     -> new BsonObjectId(new ObjectId()),
        "regEx"        -> new BsonRegularExpression("^bson".r.regex),
        "string"       -> new BsonString("string"),
        "symbol"       -> new BsonSymbol(Symbol("bson").name),
        "bsonDocument" -> new BsonDocument("a", new BsonString("string")),
        "array"        -> new BsonArray(List(new BsonString("string"), new BsonBoolean(false)).asJava),
      )

      val writer: BsonBinaryWriter = new BsonBinaryWriter(new BasicOutputBuffer())
      MutableDocumentCodec(registry).encode(writer, original, EncoderContext.builder().build())

      val buffer: BasicOutputBuffer =
        writer.getBsonOutput().asInstanceOf[BasicOutputBuffer];
      val reader: BsonBinaryReader = new BsonBinaryReader(
        new ByteBufferBsonInput(
          new ByteBufNIO(ByteBuffer.wrap(buffer.toByteArray)),
        ),
      )

      val decodedDocument =
        MutableDocumentCodec().decode(reader, DecoderContext.builder().build())

      assertTrue(
        decodedDocument.isInstanceOf[mutable.Document],
      ) &&
      assertTrue(original == decodedDocument)
    },
    test("should respect encodeIdFirst property in encoder context") {
      val original: mutable.Document = Document(
        "a"   -> new BsonString("string"),
        "_id" -> new BsonInt32(1),
        "nested" -> Document(
          "a"   -> new BsonString("string"),
          "_id" -> new BsonInt32(1),
        ).toBsonDocument,
      )

      val writer: BsonBinaryWriter = new BsonBinaryWriter(new BasicOutputBuffer())
      MutableDocumentCodec(registry).encode(
        writer,
        original,
        EncoderContext.builder().isEncodingCollectibleDocument(true).build(),
      )

      val buffer: BasicOutputBuffer =
        writer.getBsonOutput().asInstanceOf[BasicOutputBuffer];
      val reader: BsonBinaryReader =
        new BsonBinaryReader(
          new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(buffer.toByteArray))),
        )

      val decodedDocument =
        MutableDocumentCodec().decode(reader, DecoderContext.builder().build())

      assertTrue(
        decodedDocument.isInstanceOf[mutable.Document],
      ) &&
      assertTrue(original == decodedDocument) &&
      assertTrue(
        decodedDocument.keys.toList == List(
          "_id",
          "a",
          "nested",
        ),
      ) &&
      assertTrue(
        Document(
          decodedDocument[BsonDocument]("nested"),
        ).keys.toList == List(
          "a",
          "_id",
        ),
      )
    },
    test("should encoder class should work as expected") {
      assertTrue(MutableDocumentCodec().getEncoderClass == classOf[mutable.Document])
    },
    test("should determine if document has an _id") {
      assertTrue(MutableDocumentCodec().documentHasId(Document()) == false) &&
      assertTrue(
        MutableDocumentCodec().documentHasId(Document("_id" -> new BsonInt32(1))) == true,
      )
    },
    test("should get the document_id") {
      assertTrue(
        MutableDocumentCodec().getDocumentId(Document()) == null,
      ) &&
      assertTrue(
        MutableDocumentCodec().getDocumentId(Document("_id" -> new BsonInt32(1))) == new BsonInt32(
          1,
        ),
      )
    },
    test("should not generate document id if present") {
      val document = Document("_id" -> new BsonInt32(1))
      val _        = MutableDocumentCodec().generateIdIfAbsentFromDocument(document)

      assertTrue(document("_id") == new BsonInt32(1))
    },
  )
}
