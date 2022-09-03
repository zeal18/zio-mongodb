package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.Collation as JCollation

/** The options regarding collation support in MongoDB 3.4+
  *
  * @mongodb.driver.manual reference/method/db.createCollection/ Create Collection
  * @mongodb.driver.manual reference/command/createIndexes Index options
  * @mongodb.server.release 3.4
  */
final case class Collation(
  locale: Option[String] = None,
  caseLevel: Option[Boolean] = None,
  caseFirst: Option[CollationCaseFirst] = None,
  strength: Option[CollationStrength] = None,
  numericOrdering: Option[Boolean] = None,
  alternate: Option[CollationAlternate] = None,
  maxVariable: Option[CollationMaxVariable] = None,
  normalization: Option[Boolean] = None,
  backwards: Option[Boolean] = None,
) {

  /** Sets the locale
    *
    * @param locale the locale
    * @see <a href="http://userguide.icu-project.org/locale">ICU User Guide - Locale</a>
    */
  def withLocale(locale: String): Collation = copy(locale = Some(locale))

  /** Sets the case level value
    *
    * <p>Turns on case sensitivity</p>
    * @param caseLevel the case level value
    */
  def withCaseLevel(caseLevel: Boolean): Collation = copy(caseLevel = Some(caseLevel))

  /** Sets the collation case first value
    *
    * <p>Determines if Uppercase or lowercase values should come first</p>
    * @param caseFirst the collation case first value
    */
  def withCaseFirst(caseFirst: CollationCaseFirst): Collation = copy(caseFirst = Some(caseFirst))

  /** Sets the collation strength
    *
    * @param strength the strength
    */
  def withStrength(strength: CollationStrength): Collation = copy(strength = Some(strength))

  /** Sets the numeric ordering
    *
    * @param numericOrdering if true will order numbers based on numerical order and not collation order
    */
  def withNumericOrdering(numericOrdering: Boolean): Collation =
    copy(numericOrdering = Some(numericOrdering))

  /** Sets the alternate
    *
    * <p>Controls whether spaces and punctuation are considered base characters</p>
    *
    * @param alternate the alternate
    */
  def withAlternate(alternate: CollationAlternate): Collation = copy(alternate = Some(alternate))

  /** Sets the maxVariable
    *
    * @param maxVariable the maxVariable
    */
  def withMaxVariable(maxVariable: CollationMaxVariable): Collation =
    copy(maxVariable = Some(maxVariable))

  /** Sets the normalization value
    *
    * <p>If true, normalizes text into Unicode NFD.</p>
    * @param normalization the normalization value
    */
  def withNormalization(normalization: Boolean): Collation =
    copy(normalization = Some(normalization))

  /** Sets the backwards value
    *
    * <p>Causes secondary differences to be considered in reverse order, as it is done in the French language</p>
    *
    * @param backwards the backwards value
    */
  def withBackwards(backwards: Boolean): Collation = copy(backwards = Some(backwards))

  private[driver] def toJava: JCollation = {
    val builder = JCollation.builder()

    locale.foreach(builder.locale)
    caseLevel.foreach(builder.caseLevel(_))
    caseFirst.foreach(cf => builder.collationCaseFirst(cf.toJava))
    strength.foreach(s => builder.collationStrength(s.toJava))
    numericOrdering.foreach(builder.numericOrdering(_))
    alternate.foreach(a => builder.collationAlternate(a.toJava))
    maxVariable.foreach(v => builder.collationMaxVariable(v.toJava))
    normalization.foreach(builder.normalization(_))
    backwards.foreach(builder.backwards(_))

    builder.build()
  }
}
