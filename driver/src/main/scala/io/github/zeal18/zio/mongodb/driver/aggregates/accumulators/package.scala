package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator.*
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonDocument

package object accumulators {

  /** Returns a sum of numerical values. Ignores non-numeric values.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sum/ \$sum]]
    */
  def sum(expression: Expression): Sum = Sum(expression)

  /** Returns an average of numerical values. Ignores non-numeric values.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg/ \$avg]]
    */
  def avg(expression: Expression): Avg = Avg(expression)

  /** Returns an aggregation of the first n elements within a group. Only meaningful when documents are in a defined order.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/first/ \$first]]
    */
  def first(expression: Expression): First = First(expression)

  /** Returns an aggregation of the first n elements within a group. Only meaningful when documents are in a defined order.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/ \$firstN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def firstN(input: Expression, n: Expression): FirstN = FirstN(input, n)

  /** Returns a value from the last document for each group. Order is only defined if the documents are sorted.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/last/ \$last]]
    */
  def last(expression: Expression): Last = Last(expression)

  /** Returns an aggregation of the last n elements within a group. Only meaningful when documents are in a defined order.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/ \$lastN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def lastN(input: Expression, n: Expression): LastN = LastN(input, n)

  /** Returns the highest expression value for each group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/max/ \$max]]
    */
  def max(expression: Expression): Max = Max(expression)

  /** Returns an aggregation of the n maximum valued elements in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/maxN/ \$maxN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def maxN(input: Expression, n: Expression): MaxN = MaxN(input, n)

  /** Returns the lowest expression value for each group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/min/ \$min]]
    */
  def min(expression: Expression): Min = Min(expression)

  /** Returns an aggregation of the n minimum valued elements in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/minN/ \$minN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def minN(input: Expression, n: Expression): MinN = MinN(input, n)

  /** Returns an array of expression values for documents in each group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/push/ \$push]]
    */
  def push(expression: Expression): Push = Push(expression)

  /** Returns an array of unique expression values for each group. Order of the array elements is undefined.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/addToSet/ \$addToSet]]
    */
  def addToSet(expression: Expression): AddToSet = AddToSet(expression)

  /** Returns a document created by combining the input documents for each group.
    * If documents to merge include the same field name, the field, in the resulting document, has the value from the last document
    * merged for the field.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/mergeObjects/ \$mergeObjects]]
    */
  def mergeObjects(expression: Expression): MergeObjects = MergeObjects(expression)

  /** Returns the population standard deviation of the input values.
    *
    * Use if the values encompass the entire population of data you want to represent and do not wish to generalize about
    * a larger population.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/stdDevPop/ \$stdDevPop]]
    *
    * @note Requires MongoDB 3.2 or greater
    */
  def stdDevPop(expression: Expression): StdDevPop = StdDevPop(expression)

  /** Returns the sample standard deviation of the input values.
    *
    * Use if the values encompass a sample of a population of data from which to generalize about the population.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/stdDevSamp/ \$stdDevSamp]]
    *
    * @note Requires MongoDB 3.2 or greater
    */
  def stdDevSamp(expression: Expression): StdDevSamp = StdDevSamp(expression)

  /** Returns the result of a user-defined accumulator function.
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
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/accumulator/ \$accumulator]]
    *
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
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bottom/ \$bottom]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def bottom(sortBy: Sort, output: Expression): Bottom = Bottom(sortBy, output)

  /** Returns an aggregation of the bottom n elements within a group, according to the specified sort order
    *
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    * @param n limits the number of results per group and has to be a positive integral expression
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bottomN/ \$bottomN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def bottomN(sortBy: Sort, output: Expression, n: Expression): BottomN = BottomN(sortBy, output, n)

  /** Returns the top element within a group according to the specified sort order.
    *
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/top/ \$top]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def top(sortBy: Sort, output: Expression): Top = Top(sortBy, output)

  /** Returns an aggregation of the top n elements within a group, according to the specified sort order
    *
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    * @param n limits the number of results per group and has to be a positive integral expression
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/topN/ \$topN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def topN(sortBy: Sort, output: Expression, n: Expression): TopN = TopN(sortBy, output, n)

  /** Returns the number of documents in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/count-accumulator/ \$count]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def count(): Count.type = Count

  /** Creates an Accumulator from a raw Bson.
    *
    * It is less type safe but useful when you want to use an accumulator that is not yet supported by this library.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    */
  def raw(bson: Bson): Raw = Raw(bson)

  /** Creates an Accumulator from a raw extended Json.
    *
    * It is less type safe but useful when you want to use an accumulator that is not yet supported by this library.
    *
    * @param json the raw extended Json
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#accumulators---group---bucket---bucketauto---setwindowfields- Accumulators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): Raw = Raw(BsonDocument.parse(json))
}
