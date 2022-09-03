package io.github.zeal18.zio.mongodb.driver.aggregates

/** The options for an unwind aggregation pipeline stage
  *
  * @mongodb.driver.manual reference/operator/aggregation/unwind/ $unwind
  * @mongodb.server.release 3.2
  */
final case class UnwindOptions(
  preserveNullAndEmptyArrays: Option[Boolean] = None,
  includeArrayIndex: Option[String] = None,
) {

  /** Sets true if the unwind stage should include documents that have null values or empty arrays
    *
    * @param preserveNullAndEmptyArrays flag depicting if the unwind stage should include documents that have null values or empty arrays
    */
  def withPreserveNullAndEmptyArrays(preserveNullAndEmptyArrays: Boolean): UnwindOptions =
    copy(preserveNullAndEmptyArrays = Some(preserveNullAndEmptyArrays))

  /** Sets the field to be used to store the array index of the unwound item
    *
    * @param arrayIndexFieldName the field to be used to store the array index of the unwound item
    */
  def withIncludeArrayIndex(includeArrayIndex: String): UnwindOptions =
    copy(includeArrayIndex = Some(includeArrayIndex))
}
