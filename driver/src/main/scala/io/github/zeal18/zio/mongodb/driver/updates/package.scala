package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.updates.Update.*
import org.bson.BsonDocument
import org.bson.conversions.Bson

package object updates {

  /** Combine a list of updates into a single update.
    *
    * @param updates the list of updates
    * @return a combined update
    */
  def combine(updates: Update*): Combine = Combine(updates)

  /** Creates an update that sets the value of the field with the given name to the given value.
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/set/ \$set]]
    */
  def set[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): Set[A] =
    Set[A](fieldName, value, encoder)

  /** Creates an update that deletes the field with the given name.
    *
    * @param fieldName the non-null field name
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/unset/ \$unset]]
    */
  def unset(fieldName: String): Unset = Unset(fieldName)

  /** Creates an update that sets the value of the field with the given name to the given value, but only if the update is an upsert that
    * results in an insert of a document.
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/setOnInsert/ \$setOnInsert]]
    * @see UpdateOptions#upsert(boolean)
    */
  def setOnInsert[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): SetOnInsert[A] =
    SetOnInsert(fieldName, value, encoder)

  /** Creates an update that renames a field.
    *
    * @param fieldName    the non-null field name
    * @param newFieldName the non-null new field name
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/rename/ \$rename]]
    */
  def rename(fieldName: String, newFieldName: String): Rename =
    Rename(fieldName, newFieldName)

  /** Creates an update that increments the value of the field with the given name by the given value.
    *
    * @param fieldName the non-null field name
    * @param number     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/inc/ \$inc]]
    */
  def inc(fieldName: String, number: Int): Increment[Int] =
    Increment[Int](fieldName, number, Codec[Int])

  /** Creates an update that increments the value of the field with the given name by the given value.
    *
    * @param fieldName the non-null field name
    * @param number     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/inc/ \$inc]]
    */
  def inc(fieldName: String, number: Long): Increment[Long] =
    Increment[Long](fieldName, number, Codec[Long])

  /** Creates an update that increments the value of the field with the given name by the given value.
    *
    * @param fieldName the non-null field name
    * @param number     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/inc/ \$inc]]
    */
  def inc(fieldName: String, number: Double): Increment[Double] =
    Increment[Double](fieldName, number, Codec[Double])

  /** Creates an update that multiplies the value of the field with the given name by the given number.
    *
    * @param fieldName the non-null field name
    * @param number    the non-null number
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/mul/ \$mul]]
    */
  def mul(fieldName: String, number: Int): Multiply[Int] =
    Multiply[Int](fieldName, number, Codec[Int])

  /** Creates an update that multiplies the value of the field with the given name by the given number.
    *
    * @param fieldName the non-null field name
    * @param number    the non-null number
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/mul/ \$mul]]
    */
  def mul(fieldName: String, number: Long): Multiply[Long] =
    Multiply[Long](fieldName, number, Codec[Long])

  /** Creates an update that multiplies the value of the field with the given name by the given number.
    *
    * @param fieldName the non-null field name
    * @param number    the non-null number
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/mul/ \$mul]]
    */
  def mul(fieldName: String, number: Double): Multiply[Double] =
    Multiply[Double](fieldName, number, Codec[Double])

  /** Creates an update that sets the value of the field to the given value if the given value is less than the current value of the
    * field.
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/min/ \$min]]
    */
  def min[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): Min[A] =
    Min(fieldName, value, encoder)

  /** Creates an update that sets the value of the field to the given value if the given value is greater than the current value of the
    * field.
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/min/ \$min]]
    */
  def max[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): Max[A] =
    Max(fieldName, value, encoder)

  /** Creates an update that sets the value of the field to the current date as a BSON date.
    *
    * @param fieldName the non-null field name
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/currentDate/ \$currentDate]]
    * @see [[https://www.mongodb.com/docs/manual/reference/bson-types/#date Date]]
    */
  def currentDate(fieldName: String): CurrentDate = CurrentDate(fieldName)

  /** Creates an update that sets the value of the field to the current date as a BSON timestamp.
    *
    * @param fieldName the non-null field name
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/currentDate/ \$currentDate]]
    * @see [[https://www.mongodb.com/docs/manual/reference/bson-types/#document-bson-type-timestamp Timestamp]]
    */
  def currentTimestamp(fieldName: String): CurrentTimestamp = CurrentTimestamp(fieldName)

  /** Creates an update that adds the given value to the array value of the field with the given name, unless the value is
    * already present, in which case it does nothing
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/addToSet/ \$addToSet]]
    */
  def addToSet[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): AddToSet[A] =
    AddToSet(fieldName, value, encoder)

  /** Creates an update that adds each of the given values to the array value of the field with the given name, unless the value is
    * already present, in which case it does nothing
    *
    * @param fieldName the non-null field name
    * @param values     the values
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/addToSet/ \$addToSet]]
    */
  def addEachToSet[A](fieldName: String, values: Iterable[A])(implicit
    encoder: Encoder[A],
  ): AddEachToSet[A] =
    AddEachToSet(fieldName, values, encoder)

  /** Creates an update that adds the given value to the array value of the field with the given name.
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/push/ \$push]]
    */
  def push[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): Push[A] =
    Push(fieldName, value, encoder)

  /** Creates an update that adds each of the given values to the array value of the field with the given name.
    *
    * @param fieldName the non-null field name
    * @param values    the values
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/push/ \$push]]
    */
  def pushEach[A](fieldName: String, values: Iterable[A])(implicit
    encoder: Encoder[A],
  ): PushEach[A] =
    PushEach(fieldName, values, options = None, encoder)

  /** Creates an update that adds each of the given values to the array value of the field with the given name, applying the given
    * options for positioning the pushed values, and then slicing and/or sorting the array.
    *
    * @param fieldName the field name
    * @param values    the values
    * @param options   the push options
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/push/ \$push]]
    */
  def pushEach[A](fieldName: String, values: Iterable[A], options: PushOptions)(implicit
    encoder: Encoder[A],
  ): PushEach[A] =
    PushEach(fieldName, values, Some(options), encoder)

  /** Creates an update that removes all instances of the given value from the array value of the field with the given name.
    *
    * @param fieldName the non-null field name
    * @param value     the value
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/pull/ \$pull]]
    */
  def pull[A](fieldName: String, value: A)(implicit encoder: Encoder[A]): Pull[A] =
    Pull(fieldName, value, encoder)

  /** Creates an update that removes from an array all elements that match the given filter.
    *
    * @param filter the query filter
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/pull/ \$pull]]
    */
  def pullByFilter(filter: Filter): PullByFilter = PullByFilter(filter)

  /** Creates an update that removes all instances of the given values from the array value of the field with the given name.
    *
    * @param fieldName the non-null field name
    * @param values    the values
    * @tparam A   the value type
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/pull/ \$pull]]
    */
  def pullAll[A](fieldName: String, values: Iterable[A])(implicit encoder: Encoder[A]): PullAll[A] =
    PullAll(fieldName, values, encoder)

  /** Creates an update that pops the first element of an array that is the value of the field with the given name.
    *
    * @param fieldName the non-null field name
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/pop/ \$pop]]
    */
  def popFirst(fieldName: String): PopFirst = PopFirst(fieldName)

  /** Creates an update that pops the last element of an array that is the value of the field with the given name.
    *
    * @param fieldName the non-null field name
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/pop/ \$pop]]
    */
  def popLast(fieldName: String): PopLast = PopLast(fieldName)

  /** Creates an update that performs a bitwise and between the given integer value and the integral value of the field with the given
    * name.
    *
    * @param fieldName the field name
    * @param value     the value
    * @return the update
    */
  def bitwiseAnd(fieldName: String, value: Int): BitwiseAndInt = BitwiseAndInt(fieldName, value)

  /** Creates an update that performs a bitwise and between the given long value and the integral value of the field with the given name.
    *
    * @param fieldName the field name
    * @param value     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/bit/ \$bit]]
    */
  def bitwiseAnd(fieldName: String, value: Long): BitwiseAndLong = BitwiseAndLong(fieldName, value)

  /** Creates an update that performs a bitwise or between the given integer value and the integral value of the field with the given
    * name.
    *
    * @param fieldName the field name
    * @param value     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/bit/ \$bit]]
    */
  def bitwiseOr(fieldName: String, value: Int): BitwiseOrInt = BitwiseOrInt(fieldName, value)

  /** Creates an update that performs a bitwise or between the given long value and the integral value of the field with the given name.
    *
    * @param fieldName the field name
    * @param value     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/bit/ \$bit]]
    */
  def bitwiseOr(fieldName: String, value: Long): BitwiseOrLong = BitwiseOrLong(fieldName, value)

  /** Creates an update that performs a bitwise xor between the given integer value and the integral value of the field with the given
    * name.
    *
    * @param fieldName the field name
    * @param value     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/bit/ \$bit]]
    */
  def bitwiseXor(fieldName: String, value: Int): BitwiseXorInt = BitwiseXorInt(fieldName, value)

  /** Creates an update that performs a bitwise xor between the given long value and the integral value of the field with the given name.
    *
    * @param fieldName the field name
    * @param value     the value
    * @return the update
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/bit/ \$bit]]
    */
  def bitwiseXor(fieldName: String, value: Long): BitwiseXorLong = BitwiseXorLong(fieldName, value)

  /** Creates an update from a raw Bson.
    *
    * It is less type safe but useful when you want to use an update that is not yet supported by this library.
    *
    * @param update the raw Bson
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    */
  def raw(update: Bson): Raw = Raw(update)

  /** Creates an update from a raw extended Json.
    *
    * It is less type safe but useful when you want to use an update that is not yet supported by this library.
    *
    * @param json the raw extended Json
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/update/ Update Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): Raw = Raw(BsonDocument.parse(json))
}
