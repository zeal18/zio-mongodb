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

import io.github.zeal18.zio.mongodb.bson.conversions.Bson

/** The model package containing models and options that help describe `MongoCollection` operations
  */
package object model {

  /** A representation of a BSON document field whose value is another BSON document.
    */
  type BsonField = com.mongodb.client.model.BsonField

  /** A representation of a BSON document field whose value is another BSON document.
    */
  object BsonField {

    /** Construct a new instance.
      *
      * @param name the name of the field
      * @param value the value for the field
      * @return a new BsonField instance
      */
    def apply(name: String, value: Bson): BsonField =
      new com.mongodb.client.model.BsonField(name, value)
  }
}
