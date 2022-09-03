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
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.updates.Update

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

  /** A model describing the removal of all documents matching the query filter.
    *
    * @tparam TResult the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *                 other write models
    */
  type DeleteManyModel[TResult] = com.mongodb.client.model.DeleteManyModel[TResult]

  /** A model describing the removal of all documents matching the query filter.
    */
  object DeleteManyModel {
    def apply[T](filter: Filter): DeleteManyModel[T] =
      new com.mongodb.client.model.DeleteManyModel[T](filter.toBson)
    def apply[T](filter: Filter, options: DeleteOptions): DeleteManyModel[T] =
      new com.mongodb.client.model.DeleteManyModel[T](filter.toBson, options.toJava)
  }

  /** A model describing the removal of at most one document matching the query filter.
    *
    * @tparam TResult the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with
    *                 the other write models
    */
  type DeleteOneModel[TResult] = com.mongodb.client.model.DeleteOneModel[TResult]

  /** A model describing the removal of at most one document matching the query filter.
    */
  object DeleteOneModel {

    /** Construct a new instance.
      *
      * @param filter the query filter
      * @return the new DeleteOneModel
      */
    def apply(filter: Filter): DeleteOneModel[Nothing] =
      new com.mongodb.client.model.DeleteOneModel(filter.toBson)
  }

  /** A model describing an insert of a single document.
    *
    * @tparam TResult the type of document to insert. This can be of any type for which a `Codec` is registered
    */
  type InsertOneModel[TResult] = com.mongodb.client.model.InsertOneModel[TResult]

  /** A model describing an insert of a single document.
    */
  object InsertOneModel {

    /** Construct a new instance.
      *
      * @param document the document to insert
      * @tparam TResult the type of document to insert. This can be of any type for which a `Codec` is registered
      * @return the new InsertOneModel
      */
    def apply[TResult](document: TResult): InsertOneModel[TResult] =
      new com.mongodb.client.model.InsertOneModel[TResult](document)
  }

  /** A model describing the replacement of at most one document that matches the query filter.
    *
    * @tparam TResult the type of document to replace. This can be of any type for which a `Codec` is registered
    */
  type ReplaceOneModel[TResult] = com.mongodb.client.model.ReplaceOneModel[TResult]

  /** A model describing the replacement of at most one document that matches the query filter.
    */
  object ReplaceOneModel {

    /** Construct a new instance.
      *
      * @param filter    a document describing the query filter.
      * @param replacement the replacement document
      * @tparam TResult the type of document to insert. This can be of any type for which a `Codec` is registered
      * @return the new ReplaceOneModel
      */
    def apply[TResult](filter: Filter, replacement: TResult): ReplaceOneModel[TResult] =
      new com.mongodb.client.model.ReplaceOneModel[TResult](filter.toBson, replacement)

    /** Construct a new instance.
      *
      * @param filter    a document describing the query filter.
      * @param replacement the replacement document
      * @param replaceOptions the options to apply
      * @tparam TResult the type of document to insert. This can be of any type for which a `Codec` is registered
      * @return the new ReplaceOneModel
      */
    def apply[TResult](
      filter: Filter,
      replacement: TResult,
      replaceOptions: ReplaceOptions,
    ): ReplaceOneModel[TResult] =
      new com.mongodb.client.model.ReplaceOneModel[TResult](
        filter.toBson,
        replacement,
        replaceOptions.toJava,
      )
  }

  /** A model describing an update to all documents that matches the query filter. The update to apply must include only update
    * operators.
    *
    * @tparam TResult the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *                 other write models
    */
  type UpdateManyModel[TResult] = com.mongodb.client.model.UpdateManyModel[TResult]

  /** A model describing an update to all documents that matches the query filter. The update to apply must include only update
    * operators.
    */
  object UpdateManyModel {

    /** Construct a new instance.
      *
      * @param filter a document describing the query filter.
      * @param update a document describing the update. The update to apply must include only update operators.
      * @return the new UpdateManyModel
      */
    def apply(filter: Filter, update: Update): UpdateManyModel[Nothing] =
      new com.mongodb.client.model.UpdateManyModel(filter.toBson, update.toBson)

    /** Construct a new instance.
      *
      * @param filter a document describing the query filter.
      * @param update a document describing the update. The update to apply must include only update operators.
      * @param updateOptions the options to apply
      * @return the new UpdateManyModel
      */
    def apply(
      filter: Filter,
      update: Update,
      updateOptions: UpdateOptions,
    ): UpdateManyModel[Nothing] =
      new com.mongodb.client.model.UpdateManyModel(
        filter.toBson,
        update.toBson,
        updateOptions.toJava,
      )
  }

  /** A model describing an update to at most one document that matches the query filter. The update to apply must include only update
    * operators.
    *
    * @tparam TResult the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *                 other write models
    */
  type UpdateOneModel[TResult] = com.mongodb.client.model.UpdateOneModel[TResult]

  /** A model describing an update to at most one document that matches the query filter. The update to apply must include only update
    * operators.
    */
  object UpdateOneModel {

    /** Construct a new instance.
      *
      * @param filter a document describing the query filter.
      * @param update a document describing the update. The update to apply must include only update operators.
      * @return the new UpdateOneModel
      */
    def apply(filter: Filter, update: Update): UpdateOneModel[Nothing] =
      new com.mongodb.client.model.UpdateOneModel(filter.toBson, update.toBson)

    /** Construct a new instance.
      *
      * @param filter a document describing the query filter.
      * @param update a document describing the update. The update to apply must include only update operators.
      * @param updateOptions the options to apply
      * @return the new UpdateOneModel
      */
    def apply(
      filter: Filter,
      update: Update,
      updateOptions: UpdateOptions,
    ): UpdateOneModel[Nothing] =
      new com.mongodb.client.model.UpdateOneModel(
        filter.toBson,
        update.toBson,
        updateOptions.toJava,
      )
  }

  /** A base class for models that can be used in a bulk write operations.
    *
    * @tparam TResult the document type for storage
    */
  type WriteModel[TResult] = com.mongodb.client.model.WriteModel[TResult]
}
