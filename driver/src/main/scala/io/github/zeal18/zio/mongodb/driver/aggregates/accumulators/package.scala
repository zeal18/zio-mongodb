package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator.*
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import io.github.zeal18.zio.mongodb.driver.sorts.Sort

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
  def sum(expression: Expression): Sum = Sum(expression)

  /** Gets a field name for a `\$group` operation representing the average of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg/ \$avg]]
    */
  def avg(expression: Expression): Avg = Avg(expression)

  /** Gets a field name for a `\$group` operation representing the value of the given expression when applied to the first member of
    * the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/first/ \$first]]
    */
  def first(expression: Expression): First = First(expression)

  /** Returns an aggregation of the first n elements within a group.
    *
    * @param input specifies the field(s) from the document to take the first n of
    * @param n the number of documents to return
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/ \$firstN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def firstN(input: Expression, n: Expression): FirstN = FirstN(input, n)

  /** Gets a field name for a `\$group` operation representing the value of the given expression when applied to the last member of
    * the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/last/ \$last]]
    */
  def last(expression: Expression): Last = Last(expression)

  /** Returns an aggregation of the last n elements within a group.
    *
    * @param input specifies the field(s) from the document to take the last n of
    * @param n the number of documents to return
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/ \$lastN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def lastN(input: Expression, n: Expression): LastN = LastN(input, n)

  /** Gets a field name for a `\$group` operation representing the maximum of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/max/ \$max]]
    */
  def max(expression: Expression): Max = Max(expression)

  /** Returns an aggregation of the maxmimum value n elements within a group.
    *
    * @param input specifies an expression that is the input to $maxN
    * @param n limits the number of results per group
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/maxN/ \$maxN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def maxN(input: Expression, n: Expression): MaxN = MaxN(input, n)

  /** Gets a field name for a `\$group` operation representing the minimum of the values of the given expression when applied to all
    * members of the group.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/min/ \$min]]
    */
  def min(expression: Expression): Min = Min(expression)

  /** Returns an aggregation of the minimum value n elements within a group.
    *
    * @param input specifies an expression that is the input to $minN
    * @param n limits the number of results per group
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/minN/ \$minN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def minN(input: Expression, n: Expression): MinN = MinN(input, n)

  /** Gets a field name for a `\$group` operation representing an array of all values that results from applying an expression to each
    * document in a group of documents that share the same group by key.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/push/ \$push]]
    */
  def push(expression: Expression): Push = Push(expression)

  /** Gets a field name for a `\$group` operation representing all unique values that results from applying the given expression to each
    * document in a group of documents that share the same group by key.
    *
    * @param fieldName the field name
    * @param expression the expression
    * @tparam A the expression type
    * @return the field
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/addToSet/ \$addToSet]]
    */
  def addToSet(expression: Expression): AddToSet = AddToSet(expression)

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
  def mergeObjects(expression: Expression): MergeObjects = MergeObjects(expression)

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
  def stdDevPop(expression: Expression): StdDevPop = StdDevPop(expression)

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
  def stdDevSamp(expression: Expression): StdDevSamp = StdDevSamp(expression)

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
    initFunction: String,
    initArgs: Option[Expression],
    accumulateFunction: String,
    accumulateArgs: Option[Expression],
    mergeFunction: String,
    finalizeFunction: Option[String],
    lang: String = "js",
  ): Function =
    Function(
      initFunction,
      initArgs,
      accumulateFunction,
      accumulateArgs,
      mergeFunction,
      finalizeFunction,
      lang,
    )

  /** Returns the bottom element within a group according to the specified sort order.
    *
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    * @return the bottom element
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bottom/ \$bottom]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def bottom(sortBy: Sort, output: Expression): Bottom = Bottom(sortBy, output)

  /** Returns an aggregation of the bottom n elements within a group, according to the specified sort order
    *
    * @param n limits the number of results per group and has to be a positive integral expression
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    * @return the bottom element
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bottomN/ \$bottomN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def bottomN(sortBy: Sort, output: Expression, n: Expression): BottomN = BottomN(sortBy, output, n)

  /** Returns the top element within a group according to the specified sort order.
    *
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    * @return the top element
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/top/ \$top]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def top(sortBy: Sort, output: Expression): Top = Top(sortBy, output)

  /** Returns an aggregation of the top n elements within a group, according to the specified sort order
    *
    * @param n limits the number of results per group and has to be a positive integral expression
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    * @return the top element
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/topN/ \$topN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def topN(sortBy: Sort, output: Expression, n: Expression): TopN = TopN(sortBy, output, n)

  /** Returns the number of documents in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/count-accumulator/ \$count]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def count(): Count.type = Count
}
