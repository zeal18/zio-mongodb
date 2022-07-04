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

import io.github.zeal18.zio.mongodb.bson.collection.immutable
import io.github.zeal18.zio.mongodb.bson.collection.mutable
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

/** A [[http://api.mongodb.org/java/current/org/bson/codecs/configuration/CodecProvider.html CodecProvider]] for the Document
  * class and all the default Codec implementations on which it depends.
  */
case class DocumentCodecProvider() extends CodecProvider {

  val IMMUTABLE: Class[immutable.Document] = classOf[immutable.Document]
  val MUTABLE: Class[mutable.Document]     = classOf[mutable.Document]

  @SuppressWarnings(Array("unchecked"))
  def get[T](clazz: Class[T], registry: CodecRegistry): Codec[T] =
    clazz match {
      case IMMUTABLE => ImmutableDocumentCodec(registry).asInstanceOf[Codec[T]]
      case MUTABLE   => MutableDocumentCodec(registry).asInstanceOf[Codec[T]]
      case _         => null
    } // scalafix:ok
}
