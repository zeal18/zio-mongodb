package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator.*
import org.bson.codecs.Codec

package object accumulators {

  /** Gets a field name for a `\$group` operation representing the sum of the values of the given expression when applied to all members of
    * the group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sum/ \$sum]]
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    */
  def sum[A](fieldName: String, expression: A)(implicit codec: Codec[A]): Sum[A] =
    Sum(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the average of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg/ \$avg]]
    */
  def avg[A](fieldName: String, expression: A)(implicit codec: Codec[A]): Avg[A] =
    Avg(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the value of the given expression when applied to the first member of
    * the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/first/ \$first]]
    */
  def first[A](fieldName: String, expression: A)(implicit codec: Codec[A]): First[A] =
    First(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the value of the given expression when applied to the last member of
    * the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/last/ \$last]]
    */
  def last[A](fieldName: String, expression: A)(implicit codec: Codec[A]): Last[A] =
    Last(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the maximum of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/max/ \$max]]
    */
  def max[A](fieldName: String, expression: A)(implicit codec: Codec[A]): Max[A] =
    Max(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the minimum of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/min/ \$min]]
    */
  def min[A](fieldName: String, expression: A)(implicit codec: Codec[A]): Min[A] =
    Min(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing an array of all values that results from applying an expression to each
    * document in a group of documents that share the same group by key.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/push/ \$push]]
    */
  def push[A](fieldName: String, expression: A)(implicit codec: Codec[A]): Push[A] =
    Push(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing all unique values that results from applying the given expression to each
    * document in a group of documents that share the same group by key.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/addToSet/ \$addToSet]]
    */
  def addToSet[A](fieldName: String, expression: A)(implicit codec: Codec[A]): AddToSet[A] =
    AddToSet(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the result of merging the fields of the documents.
    * If documents to merge include the same field name, the field, in the resulting document, has the value from the last document
    * merged for the field.
    *
    * @param fieldName  the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/mergeObjects/ \$mergeObjects]]
    */
  def mergeObjects[A](fieldName: String, expression: A)(implicit codec: Codec[A]): MergeObjects[A] =
    MergeObjects(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the sample standard deviation of the values of the given expression
    * when applied to all members of the group.
    *
    * Use if the values encompass the entire population of data you want to represent and do not wish to generalize about
    * a larger population.
    *
    * @note Requires MongoDB 3.2 or greater
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/stdDevPop/ \$stdDevPop]]
    */
  def stdDevPop[A](fieldName: String, expression: A)(implicit codec: Codec[A]): StdDevPop[A] =
    StdDevPop(fieldName, expression, codec)

  /** Gets a field name for a `\$group` operation representing the sample standard deviation of the values of the given expression
    * when applied to all members of the group.
    *
    * Use if the values encompass a sample of a population of data from which to generalize about the population.
    *
    * @note Requires MongoDB 3.2 or greater
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/stdDevSamp/ \$stdDevSamp]]
    */
  def stdDevSamp[A](fieldName: String, expression: A)(implicit codec: Codec[A]): StdDevSamp[A] =
    StdDevSamp(fieldName, expression, codec)

  /** Creates an `\$accumulator` pipeline stage
    *
    * @param fieldName            the field name
    * @param initFunction         a function used to initialize the state
    * @param initArgs             init function’s arguments (may be null)
    * @param accumulateFunction   a function used to accumulate documents
    * @param accumulateArgs       additional accumulate function’s arguments (may be null). The first argument to the
    *                             function is ‘state’.
    * @param mergeFunction        a function used to merge two internal states, e.g. accumulated on different shards or
    *                             threads. It returns the resulting state of the accumulator.
    * @param finalizeFunction     a function used to finalize the state and return the result (may be null)
    * @param lang                 a language specifier
    * @return the `\$accumulator` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/accumulator/ \$accumulator]]
    * @note Requires MongoDB 4.4 or greater
    */
  def accumulator(
    fieldName: String,
    initFunction: String,
    initArgs: Seq[String],
    accumulateFunction: String,
    accumulateArgs: Seq[String],
    mergeFunction: String,
    finalizeFunction: Option[String],
    lang: String,
  ): Function =
    Function(
      fieldName,
      initFunction,
      initArgs,
      accumulateFunction,
      accumulateArgs,
      mergeFunction,
      finalizeFunction,
      lang,
    )
}
