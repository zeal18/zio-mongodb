package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import io.github.zeal18.zio.mongodb.driver.aggregates.windows.Window.*
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonDocument

package object windows {

  /** Returns a sum of numerical values. Ignores non-numeric values.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sum/ \$sum]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def sum(expression: Expression): Sum = Sum(expression)

  /** Returns an average of numerical values. Ignores non-numeric values.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/avg/ \$avg]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def avg(expression: Expression): Avg = Avg(expression)

  /** Returns an aggregation of the first n elements within a group. Only meaningful when documents are in a defined order.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/first/ \$first]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def first(expression: Expression): First = First(expression)

  /** Returns an aggregation of the first n elements within a group. Only meaningful when documents are in a defined order.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN/ \$firstN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def firstN(input: Expression, n: Expression): FirstN = FirstN(input, n)

  /** Returns a value from the last document for each group. Order is only defined if the documents are sorted.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/last/ \$last]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def last(expression: Expression): Last = Last(expression)

  /** Returns an aggregation of the last n elements within a group. Only meaningful when documents are in a defined order.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN/ \$lastN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def lastN(input: Expression, n: Expression): LastN = LastN(input, n)

  /** Returns the highest expression value for each group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/max/ \$max]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def max(expression: Expression): Max = Max(expression)

  /** Returns an aggregation of the n maximum valued elements in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/maxN/ \$maxN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def maxN(input: Expression, n: Expression): MaxN = MaxN(input, n)

  /** Returns the lowest expression value for each group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/min/ \$min]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def min(expression: Expression): Min = Min(expression)

  /** Returns an aggregation of the n minimum valued elements in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/minN/ \$minN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def minN(input: Expression, n: Expression): MinN = MinN(input, n)

  /** Returns an array of expression values for documents in each group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/push/ \$push]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def push(expression: Expression): Push = Push(expression)

  /** Returns an array of unique expression values for each group. Order of the array elements is undefined.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/addToSet/ \$addToSet]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def addToSet(expression: Expression): AddToSet = AddToSet(expression)

  /** Returns the population standard deviation of the input values.
    *
    * Use if the values encompass the entire population of data you want to represent and do not wish to generalize about
    * a larger population.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/stdDevPop/ \$stdDevPop]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def stdDevPop(expression: Expression): StdDevPop = StdDevPop(expression)

  /** Returns the sample standard deviation of the input values.
    *
    * Use if the values encompass a sample of a population of data from which to generalize about the population.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/stdDevSamp/ \$stdDevSamp]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def stdDevSamp(expression: Expression): StdDevSamp = StdDevSamp(expression)

  /** Returns the bottom element within a group according to the specified sort order.
    *
    * @param sortBy specifies the order of results
    * @param output represents the output for each element in the group and can be any expression
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
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
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
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
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
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
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/topN/ \$topN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def topN(sortBy: Sort, output: Expression, n: Expression): TopN = TopN(sortBy, output, n)

  /** Returns the number of documents in a group.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/count-accumulator/ \$count]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def count(): Count.type = Count

  /** Returns the population covariance of two numeric expressions.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/covariancePop/ \$covariancePop]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def covariancePop(expression1: Expression, expression2: Expression): CovariancePop =
    CovariancePop(expression1, expression2)

  /** Returns the sample covariance of two numeric expressions.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/covarianceSamp/ \$covarianceSamp]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def covarianceSamp(expression1: Expression, expression2: Expression): CovarianceSamp =
    CovarianceSamp(expression1, expression2)

  /** Returns the document position (known as the rank) relative to other documents in the \$setWindowFields stage partition.
    * There are no gaps in the ranks. Ties receive the same rank.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/denseRank/ \$denseRank]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def denseRank(): DenseRank.type = DenseRank

  /** Returns the average rate of change within the specified window.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/derivative/ \$derivative]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def derivative(input: Expression, unit: Option[TimeUnit] = None): Derivative =
    Derivative(input, unit)

  /** Returns the position of a document (known as the document number) in the $setWindowFields stage partition.
    * Ties result in different adjacent document numbers.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/documentNumber/ \$documentNumber]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def documentNumber(): DocumentNumber.type = DocumentNumber

  /** Returns the exponential moving average for the numeric expression.
    *
    * @param input the numeric expression
    * @param n the number of historical documents that have a significant mathematical weight in the exponential moving average calculation
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/expMovingAvg/ \$expMovingAvg]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def expMovingAvg(input: Expression, n: Int): ExpMovingAvgN = ExpMovingAvgN(input, n)

  /** Returns the exponential moving average for the numeric expression.
    *
    * @param input the numeric expression
    * @param alpha the exponential decay value to use in the exponential moving average calculation
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/expMovingAvg/ \$expMovingAvg]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def expMovingAvg(input: Expression, alpha: Double): ExpMovingAvgAlpha =
    ExpMovingAvgAlpha(input, alpha)

  /** Returns the approximation of the area under a curve.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/integral/ \$integral]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def integral(input: Expression, unit: Option[TimeUnit] = None): Integral = Integral(input, unit)

  /** Fills null and missing fields in a window using linear interpolation based on surrounding field values.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/linearFill/ \$linearFill]]
    * @see [[https://en.wikipedia.org/wiki/Linear_interpolation Linear interpolation]]
    *
    * @note Requires MongoDB 5.3 or greater
    */
  def linearFill(expression: Expression): LinearFill = LinearFill(expression)

  /** Last observation carried forward. Sets values for null and missing fields in a window to the last non-null value for the field.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/locf/ \$locf]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def locf(expression: Expression): Locf = Locf(expression)

  /** Returns the document position (known as the rank) relative to other documents in the $setWindowFields stage partition.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/rank/ \$rank]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def rank(): Rank.type = Rank

  /** Returns the value from an expression applied to a document in a specified position relative to the current document in the $setWindowFields stage partition.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/shift/ \$shift]]
    *
    * @note Requires MongoDB 5.0 or greater
    */
  def shift(output: Expression, by: Int, default: Option[Expression] = None): Shift =
    Shift(output, by, default)

  /** Creates a Window from a raw Bson.
    *
    * It is less type safe but useful when you want to use a Window that is not yet supported by this library.
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    */
  def raw(bson: Bson): Raw = Raw(bson)

  /** Creates a Window from a raw extended Json.
    *
    * It is less type safe but useful when you want to use a Window that is not yet supported by this library.
    *
    * @param json the raw extended Json
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/#window-operators Windows]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): Raw = Raw(BsonDocument.parse(json))
}
