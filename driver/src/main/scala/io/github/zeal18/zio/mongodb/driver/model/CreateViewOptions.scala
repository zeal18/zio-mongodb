package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.CreateViewOptions as JCreateViewOptions

/** Options for creating a view
  *
  * @mongodb.server.release 3.4
  * @mongodb.driver.manual reference/command/create Create Command
  */
final case class CreateViewOptions(
  collation: Option[Collation] = None,
) {

  /** Sets the collation options
    *
    * @param collation the collation options to use
    */
  def withCollation(collation: Collation): CreateViewOptions = copy(collation = Some(collation))

  private[driver] def toJava: JCreateViewOptions = {
    val options = new JCreateViewOptions()

    collation.foreach(c => options.collation(c.toJava))

    options
  }
}
