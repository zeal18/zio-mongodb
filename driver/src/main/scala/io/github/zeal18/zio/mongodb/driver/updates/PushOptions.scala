package io.github.zeal18.zio.mongodb.driver.updates

import io.github.zeal18.zio.mongodb.driver.sorts.Sort

/** The options to apply to a $push update operator.
  *
  * @mongodb.driver.manual reference/operator/update/push/ $push
  */
final case class PushOptions(
  position: Option[Int] = None,
  slice: Option[Int] = None,
  sort: Option[Either[Boolean, Sort]] = None,
) {

  /** Sets the position at which to add the pushed values in the array.
    *
    * @param position the position
    * @mongodb.driver.manual reference/operator/update/position/ $position
    */
  def withPosition(position: Int): PushOptions = copy(position = Some(position))

  /** Sets the limit on the number of array elements allowed.
    *
    * @param slice the limit
    * @mongodb.driver.manual reference/operator/update/slice/ $slice
    */
  def withSlice(slice: Int): PushOptions = copy(slice = Some(slice))

  /** Sets the sort direction for sorting array elements.
    *
    * @param sort either a boolean (true stays for ascending) for non-documents array or a [[Sort]] instance for documents array
    * @mongodb.driver.manual reference/operator/update/sort/ $sort
    */
  def withSort(sort: Either[Boolean, Sort]): PushOptions = copy(sort = Some(sort))
}
