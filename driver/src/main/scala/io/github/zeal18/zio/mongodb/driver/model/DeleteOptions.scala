package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.DeleteOptions as JDeleteOptions
import io.github.zeal18.zio.mongodb.driver.hints.Hint
import org.bson.BsonValue
import org.bson.conversions.Bson

/** The options to apply when deleting documents.
  *
  * @mongodb.driver.manual tutorial/remove-documents/ Remove documents
  * @mongodb.driver.manual reference/command/delete/ Delete Command
  */
final case class DeleteOptions(
  hint: Option[Hint] = None,
  collation: Option[Collation] = None,
  comment: Option[BsonValue] = None,
  variables: Option[Bson] = None,
) {

  /** Sets the hint to apply.
    *
    * @param hint a document describing the index which should be used for this operation.
    * @mongodb.server.release 4.4
    */
  def withHint(hint: Hint): DeleteOptions = copy(hint = Some(hint))

  /** Sets the collation options
    *
    * @param collation the collation options to use
    * @mongodb.server.release 3.4
    */
  def withCollation(collation: Collation): DeleteOptions = copy(collation = Some(collation))

  /** Sets the comment for this operation.
    *
    * @param comment the comment
    * @mongodb.server.release 4.4
    */
  def withComment(comment: BsonValue): DeleteOptions = copy(comment = Some(comment))

  /** Add top-level variables for the operation
    *
    * <p>Allows for improved command readability by separating the variables from the query text.
    *
    * @param variables for the operation or null
    * @mongodb.server.release 5.0
    */
  def withVariables(variables: Bson): DeleteOptions = copy(variables = Some(variables))

  private[driver] def toJava: JDeleteOptions = {
    val options = new JDeleteOptions()

    hint.map(_.toBson).foreach {
      case Left(string) => options.hintString(string)
      case Right(bson)  => options.hint(bson)
    }
    collation.foreach(c => options.collation(c.toJava))
    comment.foreach(options.comment)
    variables.foreach(options.let)

    options
  }
}
