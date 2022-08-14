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

package io.github.zeal18.zio.mongodb.driver

import com.mongodb.reactivestreams.client.MongoClients as JMongoClients
import com.mongodb.MongoClientSettings as JMongoClientSettings
import zio.Has
import zio.ZIO
import zio.ZLayer

/** A MongoClientSettings companion object
  */
object MongoClientSettings {

  /** Creates a the default builder
    * @return a MongoClientSettings builder
    */
  def builder(): Builder =
    JMongoClientSettings.builder().codecRegistry(MongoClient.DEFAULT_CODEC_REGISTRY)

  /** Creates a builder from an existing `MongoClientSettings`.
    *
    * @param settings the settings to create the builder from
    * @return a MongoClientSettings builder
    */
  def builder(settings: MongoClientSettings): Builder = {
    val builder = JMongoClientSettings.builder(settings)
    if (settings.getCodecRegistry == JMongoClients.getDefaultCodecRegistry) {
      builder.codecRegistry(MongoClient.DEFAULT_CODEC_REGISTRY)
    }
    builder
  }

  /** MongoClientSettings builder type
    */
  type Builder = JMongoClientSettings.Builder

  /** Create a default MongoClientSettings at localhost:27017
    */
  def localhost: ZLayer[Any, Throwable, Has[MongoClientSettings]] =
    fromUri("mongodb://localhost:27017")

  /** Create a MongoClientSettings instance from a connection string uri
    */
  def fromUri(uri: String): ZLayer[Any, Throwable, Has[MongoClientSettings]] =
    ZLayer.fromEffect(
      ZIO.effect(
        JMongoClientSettings
          .builder()
          .applyConnectionString(new ConnectionString(uri))
          .codecRegistry(MongoClient.DEFAULT_CODEC_REGISTRY)
          .build(),
      ),
    )
}
