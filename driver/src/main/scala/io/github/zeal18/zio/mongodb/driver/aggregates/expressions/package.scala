package io.github.zeal18.zio.mongodb.driver.aggregates

import io.github.zeal18.zio.mongodb.bson.codecs.Encoder
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression.*
import io.github.zeal18.zio.mongodb.driver.sorts.Sort
import org.bson.BsonDocument
import org.bson.conversions.Bson

package object expressions {

  /** Aggregation expressions use field path to access fields in the input documents.
    *
    * To specify a field path, prefix the field name or the dotted field name (if the field is in the embedded document) with a dollar sign $. For example, "$user" to specify the field path for the user field or "$user.name" to specify the field path to "user.name" field.
    *
    * "$<field>" is equivalent to "$$CURRENT.<field>" where the CURRENT is a system variable that defaults to the root of the current object, unless stated otherwise in specific stages.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#field-paths Field Paths]]
    * @see [[https://www.mongodb.com/docs/manual/core/document/#dot-notation Dot Notation]]
    */
  def fieldPath(field: String): FieldPath = FieldPath(field)

  /** Returns a value without parsing. Use for values that the aggregation pipeline may interpret as an expression.
    *
    * Literals can be of any type. However, MongoDB parses string literals that start with a dollar sign $ as a path to a field and numeric/boolean literals in expression objects as projection flags. To avoid parsing literals, use the $literal expression.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#literals Literals]]
    */
  def literal[A](value: A)(implicit encoder: Encoder[A]): Literal[A] = Literal(value, encoder)

  /** Expression objects have the following form:
    * {{{
    * { field1: expression1, ... }
    * }}}
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#expression-objects Expression Objects]]
    */
  def obj(fields: Map[String, Expression]): ExpressionObject = ExpressionObject(fields)

  /** Expression objects have the following form:
    * {{{
    * { field1: expression1, ... }
    * }}}
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#expression-objects Expression Objects]]
    */
  def obj(fields: (String, Expression)*): ExpressionObject = ExpressionObject(fields.toMap)

  /** Can be any value. Will be parsed as an expression.
    *
    *  Use literals to avoid parsing values as expressions.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#expressions Expressions]]
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#literals Literals]]
    */
  def const[A](value: A)(implicit encoder: Encoder[A]): Variable.Constant[A] =
    Variable.Constant(value, encoder)

  /** Creates an Expression from a raw Bson.
    *
    * It is less type safe but useful when you want to use an expression that is not yet supported by this library.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#expressions Expressions]]
    */
  def raw(bson: Bson): Raw = Raw(bson)

  /** Creates an Expression from a raw extended Json.
    *
    * It is less type safe but useful when you want to use an expression that is not yet supported by this library.
    *
    * @param json the raw extended Json
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#expressions Expressions]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): Raw = Raw(BsonDocument.parse(json))

  /** Returns the current datetime value, which is same across all members of the deployment and remains constant throughout the aggregation pipeline.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.NOW NOW]]
    *
    * @note Requires MongoDB 4.2 or greater
    */
  val now: Variable.Now.type = Variable.Now

  /** Returns the current timestamp value, which is same across all members of the deployment and remains constant throughout the aggregation pipeline. For replica sets and sharded clusters only.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.CLUSTER_TIME CLUSTER_TIME]]
    *
    * @note Requires MongoDB 4.2 or greater
    */
  val clusterTime: Variable.ClusterTime.type = Variable.ClusterTime

  /** References the root document, i.e. the top-level document.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.ROOT ROOT]]
    */
  val root: Variable.Root.type = Variable.Root

  /** References the start of the field path, which by default is ROOT but can be changed.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.CURRENT CURRENT]]
    */
  val current: Variable.Current.type = Variable.Current

  /** Allows for the conditional exclusion of fields.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.REMOVE REMOVE]]
    *
    * @note Requires MongoDB 3.6 or greater
    */
  val remove: Variable.Remove.type = Variable.Remove

  /** One of the allowed results of a $redact expression.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.DESCENT DESCENT]]
    */
  val descent: Variable.Descent.type = Variable.Descent

  /** One of the allowed results of a $redact expression.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.PRUNE PRUNE]]
    */
  val prune: Variable.Prune.type = Variable.Prune

  /** One of the allowed results of a $redact expression.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-variables Aggregation Variables]]
    * @see [[https://www.mongodb.com/docs/manual/reference/aggregation-variables/#mongodb-variable-variable.KEEP KEEP]]
    */
  val keep: Variable.Keep.type = Variable.Keep

  /** Returns the absolute value of a number.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/abs Abs]]
    */
  def abs(number: Expression): Operator.Abs = Operator.Abs(number)

  /** Adds numbers together or adds numbers and a date. If adding numbers and a date, treats the numbers as milliseconds.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/add Add]]
    */
  def add(expressions: Expression*): Operator.Add = Operator.Add(expressions)

  /** Returns the smallest integer greater than or equal to the specified number.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/ceil Ceil]]
    */
  def ceil(number: Expression): Operator.Ceil = Operator.Ceil(number)

  /** Returns the result of dividing the first number by the second. Accepts two argument expressions.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/divide Divide]]
    */
  def divide(dividend: Expression, divisor: Expression): Operator.Divide =
    Operator.Divide(dividend, divisor)

  /** Raises e to the specified exponent.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/exp Exp]]
    */
  def exp(exponent: Expression): Operator.Exp = Operator.Exp(exponent)

  /** Returns the largest integer less than or equal to the specified number.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/floor Floor]]
    */
  def floor(number: Expression): Operator.Floor = Operator.Floor(number)

  /** Calculates the natural logarithm ln (i.e loge) of a number and returns the result as a double.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://docs.mongodb.com/manual/reference/operator/aggregation/ln/ Ln]]
    */
  def ln(number: Expression): Operator.Ln = Operator.Ln(number)

  /** Calculates the log of a number in the specified base and returns the result as a double.
    *
    * @param number a non-negative number
    * @param base a positive number greater than 1
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://docs.mongodb.com/manual/reference/operator/aggregation/log/ Log]]
    */
  def log(number: Expression, base: Expression): Operator.Log = Operator.Log(number, base)

  /** Calculates the log base 10 of a number and returns the result as a double.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://docs.mongodb.com/manual/reference/operator/aggregation/log10/ Log10]]
    */
  def log10(number: Expression): Operator.Log10 = Operator.Log10(number)

  /** Divides one number by another and returns the remainder.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[https://docs.mongodb.com/manual/reference/operator/aggregation/mod/ Mod]]
    */
  def mod(dividend: Expression, divisor: Expression): Operator.Mod = Operator.Mod(dividend, divisor)

  /** Multiplies numbers together and returns the result. Pass the arguments to $multiply in an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/multiply/ Multiply]]
    */
  def multiply(expressions: Expression*): Operator.Multiply = Operator.Multiply(expressions)

  /** Raises a number to the specified exponent and returns the result.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/pow/ Pow]]
    */
  def pow(base: Expression, exponent: Expression): Operator.Pow = Operator.Pow(base, exponent)

  /** Rounds a number to to a whole integer or to a specified decimal place.
    *
    * @param number Can be any valid expression that resolves to a number.
    * @param place Can be any valid expression that resolves to an integer between -20 and 100, exclusive. e.g. -20 < place < 100. Defaults to 0 if unspecified.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/round/ Round]]
    *
    * @note Requires MongoDB 4.2 or greater
    */
  def round(number: Expression, place: Option[Expression] = None): Operator.Round =
    Operator.Round(number, place)

  /** Calculates the square root of a positive number and returns the result as a double.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/sqrt/ Sqrt]]
    */
  def sqrt(number: Expression): Operator.Sqrt = Operator.Sqrt(number)

  /** Subtracts two numbers to return the difference, or two dates to return the difference in milliseconds,
    * or a date and a number in milliseconds to return the resulting date.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/subtract/ Subtract]]
    */
  def subtract(minuend: Expression, subtrahend: Expression): Operator.Subtract =
    Operator.Subtract(minuend, subtrahend)

  /** Truncates a number to a whole integer or to a specified decimal place.
    *
    * @param number Can be any valid expression that resolves to a number.
    * @param place Can be any valid expression that resolves to an integer between -20 and 100, exclusive. e.g. -20 < place < 100. Defaults to 0 if unspecified.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#arithmetic-expression-operators Arithmetic Expression Operators]]
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/trunc/ Trunc]]
    */
  def trunc(number: Expression, place: Option[Expression] = None): Operator.Trunc =
    Operator.Trunc(number, place)

  /** Returns the element at the specified array index.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/arrayElemAt/ ArrayElemAt]]
    */
  def arrayElemAt(array: Expression, idx: Expression): Operator.ArrayElemAt =
    Operator.ArrayElemAt(array, idx)

  /** Converts an array into a single document.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/arrayToObject/ ArrayToObject]]
    */
  def arrayToObject(array: Expression): Operator.ArrayToObject = Operator.ArrayToObject(array)

  /** Concatenates arrays to return the concatenated array.
    *
    * @param arrays Can be any valid expression as long as they resolve to arrays. If any argument resolves to a value of null or refers to a missing field, $concatArrays returns null.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/concatArrays/ ConcatArrays]]
    */
  def concatArrays(arrays: Expression*): Operator.ConcatArrays = Operator.ConcatArrays(arrays)

  /** Selects a subset of the array to return an array with only the elements that match the filter condition.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/filter/ Filter]]
    */
  def filter(
    input: Expression,
    cond: Expression,
    as: Option[String] = None,
    limit: Option[Expression] = None,
  ): Operator.Filter =
    Operator.Filter(input, cond, as, limit)

  /** Returns the first element in an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/first-array-element/ First Array Element]]
    *
    * @note Requires MongoDB 4.4 or greater
    */
  def first(array: Expression): Operator.First = Operator.First(array)

  /** Returns a specified number of elements from the beginning of an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/firstN-array-element/ FirstN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def firstN(array: Expression, n: Expression): Operator.FirstN = Operator.FirstN(array, n)

  /** Returns a boolean indicating whether a specified value is in an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/in/ In]]
    */
  def in(value: Expression, array: Expression): Operator.In = Operator.In(value, array)

  /** Searches an array for an occurrence of a specified value and returns the array index of the first occurrence.
    * If the substring is not found, returns -1.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/indexOfArray/ IndexOfArray]]
    */
  def indexOfArray(
    array: Expression,
    value: Expression,
    start: Option[Expression] = None,
    end: Option[Expression] = None,
  ): Operator.IndexOfArray =
    Operator.IndexOfArray(array, value, start, end)

  /** Determines if the operand is an array. Returns a boolean.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/isArray/ IsArray]]
    */
  def isArray(expression: Expression): Operator.IsArray = Operator.IsArray(expression)

  /** Returns the last element of an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/last-array-element Last]]
    *
    * @note Requires MongoDB 4.4 or greater
    */
  def last(array: Expression): Operator.Last = Operator.Last(array)

  /** Returns a specified number of elements from the end of an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lastN-array-element/ Last]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def lastN(array: Expression, n: Expression): Operator.LastN = Operator.LastN(array, n)

  /**  Applies an expression to each item in an array and returns an array with the applied results.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/map/ Map]]
    */
  def map(input: Expression, in: Expression, as: Option[String] = None): Operator.Map =
    Operator.Map(input, in, as)

  /** Returns the n largest values in an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/maxN-array-element/ MaxN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def maxN(array: Expression, n: Expression): Operator.MaxN = Operator.MaxN(array, n)

  /** Returns the n smallest values in an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/minN-array-element/ MinN]]
    *
    * @note Requires MongoDB 5.2 or greater
    */
  def minN(array: Expression, n: Expression): Operator.MinN = Operator.MinN(array, n)

  /** Converts a document to an array. The return array contains an element for each field/value pair in the original document.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/objectToArray/ ObjectToArray]]
    */
  def objectToArray(expression: Expression): Operator.ObjectToArray =
    Operator.ObjectToArray(expression)

  /** Returns an array whose elements are a generated sequence of numbers.    *
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/range/ Range]]
    */
  def range(start: Expression, end: Expression, step: Option[Expression] = None): Operator.Range =
    Operator.Range(start, end, step)

  /** Applies an expression to each element in an array and combines them into a single value.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/reduce/ Reduce]]
    */
  def reduce(input: Expression, initialValue: Expression, in: Expression): Operator.Reduce =
    Operator.Reduce(input, initialValue, in)

  /** Returns an array with the elements in reverse order.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/reverseArray/ ReverseArray]]
    */
  def reverseArray(expression: Expression): Operator.ReverseArray =
    Operator.ReverseArray(expression)

  /** Returns the number of elements in the array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/size/ Size]]
    */
  def size(expression: Expression): Operator.Size = Operator.Size(expression)

  /** Returns a subset of an array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/slice/ Slice]]
    */
  def slice(array: Expression, position: Option[Expression], n: Expression): Operator.Slice =
    Operator.Slice(array, position, n)

  /** Sorts an array based on its elements. The sort order is user specified.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/ SortArray]]
    */
  def sortArray(array: Expression, sort: Sort): Operator.SortArrayByField =
    Operator.SortArrayByField(array, sort)

  /** Sorts an array based on its elements. The sort order is user specified.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortArray/ SortArray]]
    */
  def sortArray(array: Expression, asc: Boolean): Operator.SortArrayByValue =
    Operator.SortArrayByValue(array, asc)

  /** Merge two arrays together. The shortest array length determines the number of arrays in the output array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/zip/ Zip]]
    */
  def zip(inputs: Seq[Expression]): Operator.ZipShortest = Operator.ZipShortest(inputs)

  /** Merge two arrays together. The length of the longest array determines the number of arrays in the output array.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#array-expression-operators Array Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/zip/ Zip]]
    */
  def zip(inputs: Seq[Expression], defaults: Expression): Operator.ZipLongest =
    Operator.ZipLongest(inputs, defaults)

  /** Returns true only when all its expressions evaluate to true.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#boolean-expression-operators Boolean Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/and/ And]]
    */
  def and(expressions: Expression*): Operator.And = Operator.And(expressions)

  /** Returns the boolean value that is the opposite of its argument expression.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#boolean-expression-operators Boolean Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/not/ Not]]
    */
  def not(expression: Expression): Operator.Not = Operator.Not(expression)

  /** Returns true when any of its expressions evaluates to true.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#boolean-expression-operators Boolean Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/or/ Or]]
    */
  def or(expressions: Expression*): Operator.Or = Operator.Or(expressions)

  /** Compares two values and returns:
    * * -1 if the first value is less than the second.
    * * 1 if the first value is greater than the second.
    * * 0 if the two values are equivalent.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/cmp/ Cmp]]
    */
  def cmp(expression1: Expression, expression2: Expression): Operator.Cmp =
    Operator.Cmp(expression1, expression2)

  /** Compares two values and returns:
    * * true when the values are equivalent.
    * * false when the values are not equivalent.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/eq/ Eq]]
    */
  def eq(expression1: Expression, expression2: Expression): Operator.Eq =
    Operator.Eq(expression1, expression2)

  /** Compares two values and returns:
    * * true when the first value is greater than the second value.
    * * false when the first value is less than or equivalent to the second value.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/gt/ Gt]]
    */
  def gt(expression1: Expression, expression2: Expression): Operator.Gt =
    Operator.Gt(expression1, expression2)

  /** Compares two values and returns:
    * * true when the first value is greater than or equivalent to the second value.
    * * false when the first value is less than the second value.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/gte/ Gte]]
    */
  def gte(expression1: Expression, expression2: Expression): Operator.Gte =
    Operator.Gte(expression1, expression2)

  /** Compares two values and returns:
    * * true when the first value is less than the second value.
    * * false when the first value is greater than or equivalent to the second value.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lt/ Lt]]
    */
  def lt(expression1: Expression, expression2: Expression): Operator.Lt =
    Operator.Lt(expression1, expression2)

  /** Compares two values and returns:
    * * true when the first value is less than or equivalent to the second value.
    * * false when the first value is greater than the second value.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lte/ Lte]]
    */
  def lte(expression1: Expression, expression2: Expression): Operator.Lte =
    Operator.Lte(expression1, expression2)

  /** Compares two values and returns:
    * * true when the values are not equivalent.
    * * false when the values are equivalent.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#comparison-expression-operators Comparison Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/ne/ Ne]]
    */
  def ne(expression1: Expression, expression2: Expression): Operator.Ne =
    Operator.Ne(expression1, expression2)

  /** Evaluates a boolean expression to return one of the two specified return expressions.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#conditional-expression-operators Conditional Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/cond/ Cond]]
    */
  def cond(predicate: Expression, ifTrue: Expression, ifFalse: Expression): Operator.Cond =
    Operator.Cond(predicate, ifTrue, ifFalse)

  /** Evaluates input expressions for null values and returns:
    * * The first non-null input expression value found.
    * * A replacement expression value if all input expressions evaluate to null.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#conditional-expression-operators Conditional Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/ifNull/ IfNull]]
    *
    * @note In MongoDB 4.4 and earlier versions $ifNull only accepts a single input expression
    */
  def ifNull(replacement: Expression, expressions: Expression*): Operator.IfNull =
    Operator.IfNull(expressions, replacement)

  /** Evaluates a series of case expressions. When it finds an expression which evaluates to true, $switch executes a specified expression and breaks out of the control flow.
    *
    * @param branches A sequence of "case" -> "then" pairs
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#conditional-expression-operators Conditional Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/switch/ Switch]]
    */
  def switch(
    branches: Seq[(Expression, Expression)],
    default: Option[Expression] = None,
  ): Operator.Switch = Operator.Switch(branches, default)

  /** Defines a custom aggregation function or expression in JavaScript.
    *
    * @param body
    * @param args
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#custom-aggregation-expression-operators Custom Aggregation Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/function/ Function]]
    *
    * @note Executing JavaScript inside an aggregation expression may decrease performance. Only use the $function operator if the provided pipeline operators cannot fulfill your application's needs.
    * @note requires MongoDB 4.4 or later
    */
  def function(body: String, args: Option[Expression] = None): Operator.Function =
    Operator.Function(body, args)

  /** Returns the size of a given string or binary data value's content in bytes.
    *
    * @param expression Can be any valid expression as long as it resolves to either a string or binary data value.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#data-size-expression-operators Data Size Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/binarySize/ BinarySize]]
    *
    * @note requires MongoDB 4.4 or later
    */
  def binarySize(expression: Expression): Operator.BinarySize = Operator.BinarySize(expression)

  /** Returns the size in bytes of a given document.
    *
    * @param expression Can be any valid expression as long as it resolves to either an object or null.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#data-size-expression-operators Data Size Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bsonSize/ BsonSize]]
    *
    * @note requires MongoDB 4.4 or later
    */
  def bsonSize(expression: Expression): Operator.BsonSize = Operator.BsonSize(expression)

  /** Increments a Date object by a specified number of time units.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateAdd/ DateAdd]]
    *
    * @note requires MongoDB 5.0 or later
    */
  def dateAdd(
    startDate: Expression,
    unit: Expression,
    amount: Expression,
    timezone: Option[Expression] = None,
  ): Operator.DateAdd = Operator.DateAdd(startDate, unit, amount, timezone)

  /** Returns the difference between two dates as a number of time units.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateDiff/ DateDiff]]
    *
    * @note requires MongoDB 5.0 or later
    */
  def dateDiff(
    startDate: Expression,
    endDate: Expression,
    unit: Expression,
    timezone: Option[Expression] = None,
    startOfWeek: Option[Expression] = None,
  ): Operator.DateDiff = Operator.DateDiff(startDate, endDate, unit, timezone, startOfWeek)

  /** Constructs and returns a Date object given the date's constituent properties.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateFromParts/ DateFromParts]]
    */
  def dateFromParts(
    year: Expression,
    month: Option[Expression],
    day: Option[Expression],
    hour: Option[Expression],
    minute: Option[Expression],
    second: Option[Expression],
    millisecond: Option[Expression],
    timezone: Option[Expression],
  ): Operator.DateFromParts =
    Operator.DateFromParts(year, month, day, hour, minute, second, millisecond, timezone)

  /** Constructs and returns a Date object given the date's constituent properties in ISO week date format.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateFromParts/ DateFromParts]]
    */
  def dateFromPartsISO(
    isoWeekYear: Expression,
    isoWeek: Option[Expression],
    isoDayOfWeek: Option[Expression],
    hour: Option[Expression],
    minute: Option[Expression],
    second: Option[Expression],
    millisecond: Option[Expression],
    timezone: Option[Expression],
  ): Operator.DateFromPartsISO = Operator.DateFromPartsISO(
    isoWeekYear,
    isoWeek,
    isoDayOfWeek,
    hour,
    minute,
    second,
    millisecond,
    timezone,
  )

  /** Converts a date/time string to a date object.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateFromString/ DateFromString]]
    */
  def dateFromString(
    dateString: Expression,
    format: Option[Expression] = None,
    timezone: Option[Expression] = None,
    onError: Option[Expression] = None,
    onNull: Option[Expression] = None,
  ): Operator.DateFromString =
    Operator.DateFromString(dateString, format, timezone, onError, onNull)

  /** Decrements a Date object by a specified number of time units.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateSubtract/ DateSubtract]]
    *
    * @note requires MongoDB 5.0 or later
    */
  def dateSubtract(
    startDate: Expression,
    unit: Expression,
    amount: Expression,
    timezone: Option[Expression] = None,
  ): Operator.DateSubtract = Operator.DateSubtract(startDate, unit, amount, timezone)

  /** Returns a document that contains the constituent parts of a given BSON Date value as individual properties.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateToParts/ DateToParts]]
    */
  def dateToParts(
    date: Expression,
    timezone: Option[Expression] = None,
    iso8601: Option[Boolean] = None,
  ): Operator.DateToParts = Operator.DateToParts(date, timezone, iso8601)

  /** Returns the date as a formatted string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateToString/ DateToString]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateToString/#format-specifiers Format Specifiers]]
    */
  def dateToString(
    date: Expression,
    format: String,
    timezone: Option[Expression] = None,
    onNull: Option[Expression] = None,
  ): Operator.DateToString = Operator.DateToString(date, format, timezone, onNull)

  /** Truncates a date.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateTrunc/ DateTrunc]]
    *
    * @note requires MongoDB 5.0 or later
    */
  def dateTrunc(
    date: Expression,
    unit: Expression,
    binSize: Option[Expression] = None,
    timezone: Option[Expression] = None,
    startOfWeek: Option[Expression] = None,
  ): Operator.DateTrunc = Operator.DateTrunc(date, unit, binSize, timezone, startOfWeek)

  /** Returns the day of the month for a date as a number between 1 and 31.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfMonth/ DayOfMonth]]
    */
  def dayOfMonth(date: Expression): Operator.DayOfMonth = Operator.DayOfMonth(date)

  /** Returns the day of the week for a date as a number between 1 (Sunday) and 7 (Saturday).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfWeek/ DayOfWeek]]
    */
  def dayOfWeek(date: Expression): Operator.DayOfWeek = Operator.DayOfWeek(date)

  /** Returns the day of the year for a date as a number between 1 and 366 (leap year).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/dayOfYear/ DayOfYear]]
    */
  def dayOfYear(date: Expression): Operator.DayOfYear = Operator.DayOfYear(date)

  /** Returns the hour portion of a date as a number between 0 and 23.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/hour/ Hour]]
    */
  def hour(date: Expression): Operator.Hour = Operator.Hour(date)

  /** Returns the weekday number in ISO 8601 format, ranging from 1 (for Monday) to 7 (for Sunday).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoDayOfWeek/ IsoDayOfWeek]]
    */
  def isoDayOfWeek(date: Expression): Operator.IsoDayOfWeek = Operator.IsoDayOfWeek(date)

  /** Returns the week number in ISO 8601 format, ranging from 1 to 53.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeek/ IsoWeek]]
    */
  def isoWeek(date: Expression): Operator.IsoWeek = Operator.IsoWeek(date)

  /** Returns the year number in ISO 8601 format.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/isoWeekYear/ IsoWeekYear]]
    */
  def isoWeekYear(date: Expression): Operator.IsoWeekYear = Operator.IsoWeekYear(date)

  /** Returns the millisecond portion of a date as an integer between 0 and 999.
    *
    * @note This operator is not available for the Atlas Free Tier cluster.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/millisecond/ Millisecond]]
    */
  def millisecond(date: Expression): Operator.Millisecond = Operator.Millisecond(date)

  /** Returns the minute portion of a date as a number between 0 and 59.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/minute/ Minute]]
    */
  def minute(date: Expression): Operator.Minute = Operator.Minute(date)

  /** Returns the month of a date as a number between 1 and 12 (January to December).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/month/ Month]]
    */
  def month(date: Expression): Operator.Month = Operator.Month(date)

  /** Returns the second portion of a date as a number between 0 and 60 (leap seconds).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/second/ Second]]
    */
  def second(date: Expression): Operator.Second = Operator.Second(date)

  /** Converts value to a Date.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toDate/ ToDate]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toDate(value: Expression): Operator.ToDate = Operator.ToDate(value)

  /** Returns the week number for a date as a number between 0 (the partial week that precedes the first Sunday of the year) and 53 (leap year).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/week/ Week]]
    */
  def week(date: Expression): Operator.Week = Operator.Week(date)

  /** Returns the year portion of a date as a number (e.g. 2014).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#date-expression-operators Date Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/year/ Year]]
    */
  def year(date: Expression): Operator.Year = Operator.Year(date)

  /** Returns the value of a specified field from a document. You can use $getField to retrieve the value of fields with names that contain periods (.) or start with dollar signs ($).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#miscellaneous-operators Miscellaneous Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/getField/ GetField]]
    *
    * @note requires MongoDB 5.0 or later
    */
  def getField(field: Expression, input: Option[Expression] = None): Operator.GetField =
    Operator.GetField(field, input)

  /** Returns a random float between 0 and 1.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#miscellaneous-operators Miscellaneous Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/rand/ Rand]]
    *
    * @note requires MongoDB 4.4.2 or later
    */
  val rand: Operator.Rand.type = Operator.Rand

  /** Randomly select documents at a given rate.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#miscellaneous-operators Miscellaneous Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sampleRate/ SampleRate]]
    *
    * @note requires MongoDB 4.4.2 or later
    */
  def sampleRate(rate: Expression): Operator.SampleRate = Operator.SampleRate(rate)

  /** Combines multiple documents into a single document.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#object-expression-operators Object Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/mergeObjects/ MergeObjects]]
    */
  def mergeObjects(expressions: Expression*): Operator.MergeObjects =
    Operator.MergeObjects(expressions)

  /** Adds, updates, or removes a specified field in a document. You can use $setField to add, update, or remove fields with names that contain periods (.) or start with dollar signs ($).
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#object-expression-operators Object Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/setField/ SetField]]
    *
    * @note requires MongoDB 5.0 or later
    */
  def setField(field: Expression, input: Expression, value: Expression): Operator.SetField =
    Operator.SetField(field, input, value)

  /** Returns true if no element of a set evaluates to false, otherwise, returns false.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/allElementsTrue/ AllElementsTrue]]
    */
  def allElementsTrue(expression: Expression): Operator.AllElementsTrue =
    Operator.AllElementsTrue(expression)

  /** Returns true if any elements of a set evaluate to true; otherwise, returns false.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/anyElementTrue/ AnyElementTrue]]
    */
  def anyElementTrue(expression: Expression): Operator.AnyElementTrue =
    Operator.AnyElementTrue(expression)

  /** Returns a set with elements that appear in the first set but not in the second set.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/setDifference/ SetDifference]]
    */
  def setDifference(first: Expression, second: Expression): Operator.SetDifference =
    Operator.SetDifference(first, second)

  /** Returns true if the input sets have the same distinct elements.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/setEquals/ SetEquals]]
    */
  def setEquals(arrays: Expression*): Operator.SetEquals = Operator.SetEquals(arrays)

  /** Returns a set with elements that appear in all of the input sets.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/setIntersection/ SetIntersection]]
    */
  def setIntersection(arrays: Expression*): Operator.SetIntersection =
    Operator.SetIntersection(arrays)

  /** Returns true if all elements of the first set appear in the second set, including when the first set equals the second set.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/setIsSubset/ SetIsSubset]]
    */
  def setIsSubset(first: Expression, second: Expression): Operator.SetIsSubset =
    Operator.SetIsSubset(first, second)

  /** Returns a set with elements that appear in any of the input sets.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#set-expression-operators Set Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/setUnion/ SetUnion]]
    */
  def setUnion(arrays: Expression*): Operator.SetUnion = Operator.SetUnion(arrays)

  /** Concatenates any number of strings.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/concat/ Concat]]
    */
  def concat(expressions: Expression*): Operator.Concat = Operator.Concat(expressions)

  /** Searches a string for an occurrence of a substring and returns the UTF-8 byte index of the first occurrence.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/indexOfBytes/ IndexOfBytes]]
    */
  def indexOfBytes(
    string: Expression,
    substring: Expression,
    start: Option[Expression] = None,
    end: Option[Expression] = None,
  ): Operator.IndexOfBytes =
    Operator.IndexOfBytes(string, substring, start, end)

  /** Searches a string for an occurrence of a substring and returns the UTF-8 code point index of the first occurrence.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/indexOfCP/ IndexOfCP]]
    */
  def indexOfCP(
    string: Expression,
    substring: Expression,
    start: Option[Expression] = None,
    end: Option[Expression] = None,
  ): Operator.IndexOfCP =
    Operator.IndexOfCP(string, substring, start, end)

  /** Removes whitespace or the specified characters from the beginning of a string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/ltrim/ LTrim]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def lTrim(string: Expression, chars: Option[Expression] = None): Operator.LTrim =
    Operator.LTrim(string, chars)

  /** Applies a regular expression (regex) to a string and returns information on the first matched substring.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/regexFind/ RegexFind]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def regexFind(
    input: Expression,
    regex: Expression,
    options: Option[Expression] = None,
  ): Operator.RegexFind =
    Operator.RegexFind(input, regex, options)

  /** Applies a regular expression (regex) to a string and returns information on the all matched substrings.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/regexFindAll/ RegexFindAll]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def regexFindAll(
    input: Expression,
    regex: Expression,
    options: Option[Expression] = None,
  ): Operator.RegexFindAll =
    Operator.RegexFindAll(input, regex, options)

  /** Applies a regular expression (regex) to a string and returns a boolean that indicates if a match is found or not.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/regexMatch/ RegexMatch]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def regexMatch(
    input: Expression,
    regex: Expression,
    options: Option[Expression] = None,
  ): Operator.RegexMatch =
    Operator.RegexMatch(input, regex, options)

  /** Replaces the first instance of a matched string in a given input.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/replaceOne/ ReplaceOne]]
    *
    * @note requires MongoDB 4.4 or later
    */
  def replaceOne(
    input: Expression,
    find: Expression,
    replacement: Expression,
  ): Operator.ReplaceOne =
    Operator.ReplaceOne(input, find, replacement)

  /** Replaces all instances of a matched string in a given input.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/replaceAll/ ReplaceAll]]
    *
    * @note requires MongoDB 4.4 or later
    */
  def replaceAll(
    input: Expression,
    find: Expression,
    replacement: Expression,
  ): Operator.ReplaceAll =
    Operator.ReplaceAll(input, find, replacement)

  /** Removes whitespace or the specified characters from the end of a string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/rtrim/ RTrim]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def rTrim(string: Expression, chars: Option[Expression] = None): Operator.RTrim =
    Operator.RTrim(string, chars)

  /** Splits a string into substrings based on a delimiter. Returns an array of substrings.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/split/ Split]]
    */
  def split(string: Expression, delimiter: Expression): Operator.Split =
    Operator.Split(string, delimiter)

  /** Returns the number of UTF-8 encoded bytes in a string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/strLenBytes/ StrLenBytes]]
    */
  def strLenBytes(string: Expression): Operator.StrLenBytes = Operator.StrLenBytes(string)

  /** Returns the number of UTF-8 code points in a string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/strLenCP/ StrLenCP]]
    */
  def strLenCP(string: Expression): Operator.StrLenCP = Operator.StrLenCP(string)

  /** Performs case-insensitive string comparison and returns: 0 if two strings are equivalent, 1 if the first string is greater than the second, and -1 if the first string is less than the second.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/strcasecmp/ StrCaseCmp]]
    */
  def strCaseCmp(string1: Expression, string2: Expression): Operator.StrCaseCmp =
    Operator.StrCaseCmp(string1, string2)

  /** Returns the substring of a string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/substrBytes/ SubstrBytes]]
    */
  def substrBytes(string: Expression, start: Expression, count: Expression): Operator.SubstrBytes =
    Operator.SubstrBytes(string, start, count)

  /** Returns the substring of a string.
    *
    *  @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    *  @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/substrCP/ SubstrCP]]
    */
  def substrCP(string: Expression, start: Expression, count: Expression): Operator.SubstrCP =
    Operator.SubstrCP(string, start, count)

  /** Converts a string to lowercase.
    *
    *  @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    *  @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toLower/ ToLower]]
    */
  def toLower(string: Expression): Operator.ToLower = Operator.ToLower(string)

  /** Converts value to a string.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toString/ ToString]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toString(value: Expression): Operator.ToString = Operator.ToString(value)

  /** Removes whitespace or the specified characters from the beginning and end of a string.
    *
    *  @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    *  @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/trim/ Trim]]
    *
    *  @note requires MongoDB 4.0 or later
    */
  def trim(string: Expression, chars: Option[Expression] = None): Operator.Trim =
    Operator.Trim(string, chars)

  /** Converts a string to uppercase.
    *
    *  @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#string-expression-operators String Expression Operators]]
    *  @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toUpper/ ToUpper]]
    */
  def toUpper(string: Expression): Operator.ToUpper = Operator.ToUpper(string)

  /** Access available per-document metadata related to the aggregation operation.
    *
    *  @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#text-expression-operator Text Expression Operator]]
    *  @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/meta/ Meta]]
    */
  def meta(keyword: Expression): Operator.Meta = Operator.Meta(keyword)

  /** Returns the sine of a value that is measured in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sin/ Sin]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def sin(value: Expression): Operator.Sin = Operator.Sin(value)

  /** Returns the cosine of a value that is measured in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/cos/ Cos]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def cos(value: Expression): Operator.Cos = Operator.Cos(value)

  /** Returns the tangent of a value that is measured in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/tan/ Tan]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def tan(value: Expression): Operator.Tan = Operator.Tan(value)

  /** Returns the inverse sin (arc sine) of a value in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/asin/ Asin]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def asin(value: Expression): Operator.Asin = Operator.Asin(value)

  /** Returns the inverse cosine (arc cosine) of a value in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/acos/ Acos]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def acos(value: Expression): Operator.Acos = Operator.Acos(value)

  /** Returns the inverse tangent (arc tangent) of a value in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/atan/ Atan]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def atan(value: Expression): Operator.Atan = Operator.Atan(value)

  /** Returns the inverse tangent (arc tangent) of y / x in radians, where y and x are the first and second values passed to the expression respectively.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/atan2/ Atan2]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def atan2(y: Expression, x: Expression): Operator.Atan2 = Operator.Atan2(y, x)

  /** Returns the inverse hyperbolic sine (hyperbolic arc sine) of a value in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/asinh/ Asinh]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def asinh(value: Expression): Operator.Asinh = Operator.Asinh(value)

  /** Returns the inverse hyperbolic cosine (hyperbolic arc cosine) of a value in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/acosh/ Acosh]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def acosh(value: Expression): Operator.Acosh = Operator.Acosh(value)

  /** Returns the inverse hyperbolic tangent (hyperbolic arc tangent) of a value in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/atanh/ Atanh]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def atanh(value: Expression): Operator.Atanh = Operator.Atanh(value)

  /** Returns the hyperbolic sine of a value that is measured in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sinh/ Sinh]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def sinh(value: Expression): Operator.Sinh = Operator.Sinh(value)

  /** Returns the hyperbolic cosine of a value that is measured in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/cosh/ Cosh]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def cosh(value: Expression): Operator.Cosh = Operator.Cosh(value)

  /** Returns the hyperbolic tangent of a value that is measured in radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/tanh/ Tanh]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def tanh(value: Expression): Operator.Tanh = Operator.Tanh(value)

  /** Converts a value from degrees to radians.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/degreesToRadians/ DegreesToRadians]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def degreesToRadians(value: Expression): Operator.DegreesToRadians =
    Operator.DegreesToRadians(value)

  /** Converts a value from radians to degrees.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#trigonometry-expression-operators Trigonometry Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/radiansToDegrees/ RadiansToDegrees]]
    *
    * @note requires MongoDB 4.2 or later
    */
  def radiansToDegrees(value: Expression): Operator.RadiansToDegrees =
    Operator.RadiansToDegrees(value)

  /** Converts a value to a specified type.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/convert/ Convert]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def convert(
    input: Expression,
    to: Expression,
    onError: Option[Expression] = None,
    onNull: Option[Expression] = None,
  ): Operator.Convert =
    Operator.Convert(input, to, onError, onNull)

  /** Returns boolean true if the specified expression resolves to an integer, decimal, double, or long.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/isNumber/ IsNumber]]
    *
    * @note requires MongoDB 4.4 or later
    */
  def isNumber(value: Expression): Operator.IsNumber = Operator.IsNumber(value)

  /** Converts value to a boolean.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toBool/ ToBool]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toBool(value: Expression): Operator.ToBool = Operator.ToBool(value)

  /** Converts value to a Decimal128.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toDecimal/ ToDecimal]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toDecimal(value: Expression): Operator.ToDecimal = Operator.ToDecimal(value)

  /** Converts value to a double.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toDouble/ ToDouble]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toDouble(value: Expression): Operator.ToDouble = Operator.ToDouble(value)

  /** Converts value to an integer.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toInt/ ToInt]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toInt(value: Expression): Operator.ToInt = Operator.ToInt(value)

  /** Converts value to a long.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toLong/ ToLong]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toLong(value: Expression): Operator.ToLong = Operator.ToLong(value)

  /** Converts value to an ObjectId.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/toObjectId/ ToObjectId]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def toObjectId(value: Expression): Operator.ToObjectId = Operator.ToObjectId(value)

  /** Returns a string that specifies the BSON type of the argument.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#type-expression-operators Type Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/type/ Type]]
    *
    * @note requires MongoDB 4.0 or later
    */
  def `type`(value: Expression): Operator.Type = Operator.Type(value)

  /** Defines variables for use within the scope of a subexpression and returns the result of the subexpression.
    *
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#variable-expression-operators Variable Expression Operators]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/let/ Let]]
    */
  def let(
    vars: Map[String, Expression],
    in: Expression,
  ): Operator.Let = Operator.Let(vars, in)
}
