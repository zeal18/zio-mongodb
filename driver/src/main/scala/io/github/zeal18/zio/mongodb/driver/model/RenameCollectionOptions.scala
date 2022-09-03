package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.RenameCollectionOptions as JRenameCollectionOptions

/** The options to apply when renaming a collection.
  *
  * @mongodb.driver.manual reference/command/renameCollection renameCollection
  */
final case class RenameCollectionOptions(dropTarget: Boolean) {
  private[driver] def toJava: JRenameCollectionOptions =
    new JRenameCollectionOptions().dropTarget(dropTarget)
}
