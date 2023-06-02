package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator.*
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression

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
  def sum(fieldName: String, expression: Expression): Sum =
    Sum(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing the average of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg/ \$avg]]
    */
  def avg(fieldName: String, expression: Expression): Avg =
    Avg(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing the value of the given expression when applied to the first member of
    * the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/first/ \$first]]
    */
  def first(fieldName: String, expression: Expression): First =
    First(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing the value of the given expression when applied to the last member of
    * the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/last/ \$last]]
    */
  def last(fieldName: String, expression: Expression): Last =
    Last(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing the maximum of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/max/ \$max]]
    */
  def max(fieldName: String, expression: Expression): Max =
    Max(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing the minimum of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/min/ \$min]]
    */
  def min(fieldName: String, expression: Expression): Min =
    Min(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing an array of all values that results from applying an expression to each
    * document in a group of documents that share the same group by key.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/push/ \$push]]
    */
  def push(fieldName: String, expression: Expression): Push =
    Push(fieldName, expression)

  /** Gets a field name for a `\$group` operation representing all unique values that results from applying the given expression to each
    * document in a group of documents that share the same group by key.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/addToSet/ \$addToSet]]
    */
  def addToSet(fieldName: String, expression: Expression): AddToSet =
    AddToSet(fieldName, expression)

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
  def mergeObjects(fieldName: String, expression: Expression): MergeObjects =
    MergeObjects(fieldName, expression)

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
  def stdDevPop(fieldName: String, expression: Expression): StdDevPop =
    StdDevPop(fieldName, expression)

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
  def stdDevSamp(fieldName: String, expression: Expression): StdDevSamp =
    StdDevSamp(fieldName, expression)

  /** Creates an `\$accumulator` pipeline stage
    *
    * @param fieldName            the field name
    * @param initFunction         a function used to initialize the state
    * @param initArgs             init function’s arguments
    * @param accumulateFunction   a function used to accumulate documents
    * @param accumulateArgs       additional accumulate function’s arguments. The first argument to the
    *                             function is ‘state’.
    * @param mergeFunction        a function used to merge two internal states, e.g. accumulated on different shards or
    *                             threads. It returns the resulting state of the accumulator.
    * @param finalizeFunction     a function used to finalize the state and return the result
    * @param lang                 a language specifier
    * @return the `\$accumulator` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/accumulator/ \$accumulator]]
    * @note Requires MongoDB 4.4 or greater
    */
  def accumulator(
    fieldName: String,
    initFunction: String,
    initArgs: Option[Expression],
    accumulateFunction: String,
    accumulateArgs: Option[Expression],
    mergeFunction: String,
    finalizeFunction: Option[String],
    lang: String = "js",
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
