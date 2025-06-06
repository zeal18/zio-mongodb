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

import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import org.bson.BsonReader
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.BsonDocumentCodec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry

/** Companion helper for immutable Document instances.
  */
private[mongodb] object ImmutableDocumentCodec {
  def apply(): ImmutableDocumentCodec                        = ImmutableDocumentCodec(None)
  def apply(registry: CodecRegistry): ImmutableDocumentCodec = ImmutableDocumentCodec(
    Some(registry),
  )
}

/** A Codec for immutable Document instances.
  *
  * As the underlying driver expects documents to be mutable the driver has direct access to the Documents underlying
  * mutable `BsonDocument` instance and therefore will mutate the document when adding an `_id`
  */
private[mongodb] case class ImmutableDocumentCodec(registry: Option[CodecRegistry]) extends CollectibleCodec[Document] {

  lazy val underlying: BsonDocumentCodec =
    registry.map(new BsonDocumentCodec(_)).getOrElse(new BsonDocumentCodec)

  override def generateIdIfAbsentFromDocument(document: Document): Document =
    if (!underlying.documentHasId(document.underlying)) {
      Document(underlying.generateIdIfAbsentFromDocument(document.toBsonDocument.clone))
    } else {
      document
    }

  override def documentHasId(document: Document): Boolean =
    underlying.documentHasId(document.underlying)

  override def getDocumentId(document: Document): BsonValue =
    underlying.getDocumentId(document.underlying)

  override def encode(writer: BsonWriter, value: Document, encoderContext: EncoderContext): Unit =
    underlying.encode(writer, value.underlying, encoderContext)

  override def getEncoderClass: Class[Document] = classOf[Document]

  override def decode(reader: BsonReader, decoderContext: DecoderContext): Document =
    Document(underlying.decode(reader, decoderContext))
}
