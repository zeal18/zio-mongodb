package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.ValidationOptions as JValidationOptions
import org.bson.conversions.Bson

/** Validation options for documents being inserted or updated in a collection
  *
  * @mongodb.server.release 3.2
  * @mongodb.driver.manual reference/method/db.createCollection/ Create Collection
  */
final case class ValidationOptions(
  validator: Option[Bson] = None,
  validationLevel: Option[ValidationLevel] = None,
  validationAction: Option[ValidationAction] = None,
) {

  /** Sets the validation rules for all
    *
    * @param validator the validation rules
    */
  def withValidator(validator: Bson): ValidationOptions = copy(validator = Some(validator))

  /** Sets the validation level that determines how strictly MongoDB applies the validation rules to existing documents during an insert
    * or update.
    *
    * @param validationLevel the validation level
    */
  def withValidationLevel(validationLevel: ValidationLevel): ValidationOptions =
    copy(validationLevel = Some(validationLevel))

  /** Sets the {@link ValidationAction} that determines whether to error on invalid documents or just warn about the violations but allow
    * invalid documents.
    *
    * @param validationAction the validation action
    */
  def withValidationAction(validationAction: ValidationAction): ValidationOptions =
    copy(validationAction = Some(validationAction))

  private[driver] def toJava: JValidationOptions = {
    val javaValidationOptions = new JValidationOptions()

    validator.foreach(javaValidationOptions.validator)
    validationLevel.foreach(l => javaValidationOptions.validationLevel(l.toJava))
    validationAction.foreach(a => javaValidationOptions.validationAction(a.toJava))

    javaValidationOptions
  }
}
