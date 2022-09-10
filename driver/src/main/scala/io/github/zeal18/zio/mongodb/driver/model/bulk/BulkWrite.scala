package io.github.zeal18.zio.mongodb.driver.model.bulk

import scala.jdk.CollectionConverters.*

import com.mongodb.client.model.DeleteManyModel
import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.InsertOneModel
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.WriteModel
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.model.DeleteOptions
import io.github.zeal18.zio.mongodb.driver.model.ReplaceOptions
import io.github.zeal18.zio.mongodb.driver.model.UpdateOptions
import io.github.zeal18.zio.mongodb.driver.updates.Update

sealed trait BulkWrite[A] {
  private[driver] def toJava: WriteModel[A] = this match {
    case BulkWrite.DeleteMany(filter, options) =>
      new DeleteManyModel[A](filter, options.toJava)
    case BulkWrite.DeleteOne(filter, options) =>
      new DeleteOneModel[A](filter, options.toJava)
    case BulkWrite.InsertOne(document) =>
      new InsertOneModel[A](document)
    case BulkWrite.ReplaceOne(filter, replacement, options) =>
      new ReplaceOneModel[A](filter, replacement, options.toJava)
    case BulkWrite.UpdateMany(filter, update, options) =>
      new UpdateManyModel[A](filter, update, options.toJava)
    case BulkWrite.UpdateManyPipeline(filter, pipeline, options) =>
      new UpdateManyModel[A](filter, pipeline.asJava, options.toJava)
    case BulkWrite.UpdateOne(filter, update, options) =>
      new UpdateOneModel[A](filter, update, options.toJava)
    case BulkWrite.UpdateOnePipeline(filter, pipeline, options) =>
      new UpdateOneModel[A](filter, pipeline.asJava, options.toJava)
  }
}

object BulkWrite {

  /** A model describing the removal of all documents matching the query filter.
    *
    * @param <T> the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *           other write models
    * @mongodb.driver.manual tutorial/remove-documents/ Remove
    */
  final case class DeleteMany[A](filter: Filter, options: DeleteOptions = DeleteOptions())
      extends BulkWrite[A]

  /** A model describing the removal of at most one document matching the query filter.
    *
    * @param <T> the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *           other write models
    * @mongodb.driver.manual tutorial/remove-documents/ Remove
    */
  final case class DeleteOne[A](filter: Filter, options: DeleteOptions = DeleteOptions())
      extends BulkWrite[A]

  /** A model describing an insert of a single document.
    *
    * @param <T> the type of document to insert. This can be of any type for which a {@code Codec} is registered
    * @mongodb.driver.manual tutorial/insert-documents/ Insert
    */
  final case class InsertOne[A](document: A) extends BulkWrite[A]

  /** A model describing the replacement of at most one document that matches the query filter.
    *
    * @param <T> the type of document to replace. This can be of any type for which a {@code Codec} is registered
    * @mongodb.driver.manual tutorial/modify-documents/#replace-the-document Replace
    */
  final case class ReplaceOne[A](
    filter: Filter,
    replacement: A,
    options: ReplaceOptions = ReplaceOptions(),
  ) extends BulkWrite[A]

  /** A model describing an update to all documents that matches the query filter. The update to apply must include only update
    * operators.
    *
    * @param <T> the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *           other write models
    * @mongodb.driver.manual tutorial/modify-documents/ Updates
    * @mongodb.driver.manual reference/operator/update/ Update Operators
    */
  final case class UpdateMany[A](
    filter: Filter,
    update: Update,
    options: UpdateOptions = UpdateOptions(),
  ) extends BulkWrite[A]

  /** A model describing an update to all documents that matches the query filter. The update to apply must include only update
    * operators.
    *
    * @param <T> the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the
    *           other write models
    * @mongodb.driver.manual tutorial/modify-documents/ Updates
    * @mongodb.driver.manual reference/operator/update/ Update Operators
    */
  final case class UpdateManyPipeline[A](
    filter: Filter,
    update: List[Update],
    options: UpdateOptions = UpdateOptions(),
  ) extends BulkWrite[A]

  /** A model describing an update to at most one document that matches the query filter. The update to apply must include only update
    * operators.
    *
    * @param <T> the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the other
    *            write models
    * @mongodb.driver.manual tutorial/modify-documents/ Updates
    * @mongodb.driver.manual reference/operator/update/ Update Operators
    */
  final case class UpdateOne[A](
    filter: Filter,
    update: Update,
    options: UpdateOptions = UpdateOptions(),
  ) extends BulkWrite[A]

  /** A model describing an update to at most one document that matches the query filter. The update to apply must include only update
    * operators.
    *
    * @param <T> the type of document to update.  In practice this doesn't actually apply to updates but is here for consistency with the other
    *            write models
    * @mongodb.driver.manual tutorial/modify-documents/ Updates
    * @mongodb.driver.manual reference/operator/update/ Update Operators
    */
  final case class UpdateOnePipeline[A](
    filter: Filter,
    update: List[Update],
    options: UpdateOptions = UpdateOptions(),
  ) extends BulkWrite[A]
}
