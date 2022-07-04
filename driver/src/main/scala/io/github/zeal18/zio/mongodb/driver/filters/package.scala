package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.driver.filters.Filter.*
import org.bson.BsonType
import org.bson.codecs.Codec
import org.bson.conversions.Bson

package object filters {

  /** Creates a filter that matches all documents where the value of _id field equals the specified value. Note that this doesn't
    * actually generate a $eq operator, as the query language doesn't require it.
    *
    * @param value     the value, which may be null
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/eq $eq
    */
  def eq[A](value: A)(implicit c: Codec[A]): Eq[A] =
    eq("_id", value)

  /** Creates a filter that matches all documents where the value of the field name equals the specified value. Note that this doesn't
    * actually generate a $eq operator, as the query language doesn't require it.
    *
    * @param fieldName the field name
    * @param value     the value, which may be null
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/eq $eq
    */
  def eq[A](fieldName: String, value: A)(implicit c: Codec[A]): Eq[A] =
    Eq(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of _id field equals the specified value. Note that this doesn't
    * actually generate a $eq operator, as the query language doesn't require it.
    *
    * @param value     the value, which may be null
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/eq $eq
    */
  def equal[A](value: A)(implicit c: Codec[A]): Eq[A] =
    equal("_id", value)

  /** Creates a filter that matches all documents where the value of the field name equals the specified value. Note that this doesn't
    * actually generate a $eq operator, as the query language doesn't require it.
    *
    * @param fieldName the field name
    * @param value     the value, which may be null
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/eq $eq
    */
  def equal[A](fieldName: String, value: A)(implicit c: Codec[A]): Eq[A] =
    Eq(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of the field name does not equal the specified value.
    *
    * @param fieldName the field name
    * @param value     the value, which may be null
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/ne $ne
    */
  def ne[A](fieldName: String, value: A)(implicit c: Codec[A]): Ne[A] =
    Ne(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of the field name does not equal the specified value.
    *
    * @param fieldName the field name
    * @param value     the value, which may be null
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/ne $ne
    */
  def notEqual[A](fieldName: String, value: A)(implicit c: Codec[A]): Ne[A] =
    Ne(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of the given field is greater than the specified value.
    *
    * @param fieldName the field name
    * @param value     the value
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/gt $gt
    */
  def gt[A](fieldName: String, value: A)(implicit c: Codec[A]): Gt[A] =
    Gt(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of the given field is less than the specified value.
    *
    * @param fieldName the field name
    * @param value     the value
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/lt $lt
    */
  def lt[A](fieldName: String, value: A)(implicit c: Codec[A]): Lt[A] =
    Lt(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of the given field is greater than or equal to the specified value.
    *
    * @param fieldName the field name
    * @param value     the value
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/gte $gte
    */
  def gte[A](fieldName: String, value: A)(implicit c: Codec[A]): Gte[A] =
    Gte(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of the given field is less than or equal to the specified value.
    *
    * @param fieldName the field name
    * @param value     the value
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/lte $lte
    */
  def lte[A](fieldName: String, value: A)(implicit c: Codec[A]): Lte[A] =
    Lte(fieldName, value, c)

  /** Creates a filter that matches all documents where the value of a field equals any value in the list of specified values.
    *
    * @param fieldName the field name
    * @param values    the list of values
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/in $in
    */
  def in[A](fieldName: String, values: Set[A])(implicit c: Codec[A]): In[A] =
    In(fieldName, values, c)

  /** Creates a filter that matches all documents where the value of a field equals any value in the list of specified values.
    *
    * @param fieldName the field name
    * @param values    the list of values
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/in $in
    */
  def in[A](fieldName: String, values: Seq[A])(implicit c: Codec[A]): In[A] =
    In(fieldName, values.toSet, c)

  /** Creates a filter that matches all documents where the value of a field does not equal any of the specified values or does not exist.
    *
    * @param fieldName the field name
    * @param values    the list of values
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/nin $nin
    */
  def nin[A](fieldName: String, values: Set[A])(implicit c: Codec[A]): Nin[A] =
    Nin(fieldName, values.toSet, c)

  /** Creates a filter that matches all documents where the value of a field does not equal any of the specified values or does not exist.
    *
    * @param fieldName the field name
    * @param values    the list of values
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/nin $nin
    */
  def nin[A](fieldName: String, values: Seq[A])(implicit c: Codec[A]): Nin[A] =
    Nin(fieldName, values.toSet, c)

  /** Creates a filter that performs a logical AND of the provided list of filters.
    *
    * <blockquote><pre>
    *    and(eq("x", 1), lt("y", 3))
    * </pre></blockquote>
    *
    * will generate a MongoDB query like:
    * <blockquote><pre>
    *    { $and: [{x : 1}, {y : {$lt : 3}}]}
    * </pre></blockquote>
    *
    * @param filters the list of filters to and together
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/and $and
    */
  def and(filters: Set[Filter]): And = And(filters)

  /** Creates a filter that performs a logical AND of the provided list of filters.
    *
    * <blockquote><pre>
    *    and(eq("x", 1), lt("y", 3))
    * </pre></blockquote>
    *
    * will generate a MongoDB query like:
    * <blockquote><pre>
    *    { $and: [{x : 1}, {y : {$lt : 3}}]}
    * </pre></blockquote>
    *
    * @param filters the list of filters to and together
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/and $and
    */
  def and(filters: Filter*): And = And(filters.toSet)

  /** Creates a filter that preforms a logical OR of the provided list of filters.
    *
    * @param filters the list of filters to and together
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/or $or
    */
  def or(filters: Set[Filter]): Or = Or(filters)

  /** Creates a filter that preforms a logical OR of the provided list of filters.
    *
    * @param filters the list of filters to and together
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/or $or
    */
  def or(filters: Filter*): Or = Or(filters.toSet)

  /** Creates a filter that matches all documents that do not match the passed in filter.
    * Requires the field name to passed as part of the value passed in and lifts it to create a valid "$not" query:
    *
    * <blockquote><pre>
    *    not(eq("x", 1))
    * </pre></blockquote>
    *
    * will generate a MongoDB query like:
    * <blockquote><pre>
    *    {x : $not: {$eq : 1}}
    * </pre></blockquote>
    *
    * @param filter the value
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/not $not
    */
  def not(filter: Filter): Not = Not(filter)

  /** Creates a filter that performs a logical NOR operation on all the specified filters.
    *
    * @param filters the list of values
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/nor $nor
    */
  def nor(filters: Set[Filter]): Nor = Nor(filters)

  /** Creates a filter that performs a logical NOR operation on all the specified filters.
    *
    * @param filters the list of values
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/nor $nor
    */
  def nor(filters: Filter*): Nor = Nor(filters.toSet)

  /** Creates a filter that matches all documents that contain the given field.
    *
    * @param fieldName the field name
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/exists $exists
    */
  def exists(fieldName: String): Exists = Exists(fieldName, exists = true)

  /** Creates a filter that matches all documents that either contain or do not contain the given field, depending on the value of the
    * exists parameter.
    *
    * @param fieldName the field name
    * @param exists    true to check for existence, false to check for absence
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/exists $exists
    */
  def exists(fieldName: String, exists: Boolean): Exists = Exists(fieldName, exists)

  /** Creates a filter that matches all documents where the value of the field is of the specified BSON type.
    *
    * @param fieldName the field name
    * @param type      the BSON type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/type $type
    */
  def `type`(fieldName: String, `type`: BsonType): Type = Type(fieldName, `type`)

  /** Creates a filter that matches all documents where the value of a field divided by a divisor has the specified remainder (i.e. perform
    * a modulo operation to select documents).
    *
    * @param fieldName the field name
    * @param divisor   the modulus
    * @param remainder the remainder
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/mod $mod
    */
  def mod(fieldName: String, divisor: Long, remainder: Long): Mod =
    Mod(fieldName, divisor, remainder)

  /** Creates a filter that matches all documents where the value of the field matches the given regular expression pattern.
    *
    * @param fieldName the field name
    * @param pattern   the pattern
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/regex $regex
    */
  def regex(fieldName: String, pattern: String): Regex = Regex(fieldName, pattern, options = "")

  /** Creates a filter that matches all documents where the value of the field matches the given regular expression pattern.
    *
    * @param fieldName the field name
    * @param pattern   the pattern
    * @param options   the options
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/regex $regex
    */
  def regex(fieldName: String, pattern: String, options: String): Regex =
    Regex(fieldName, pattern, options)

  /** Creates a filter that matches all documents matching the given search term.
    *
    * @param search the search term
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/text $text
    */
  def text(search: String): Text =
    Text(search, language = None, caseSensitive = None, diacriticSensitive = None)

  /** Creates a filter that matches all documents matching the given search term.
    *
    * @param search the search term
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/text $text
    */
  def text(
    search: String,
    language: String,
    caseSensitive: Boolean,
    diacriticSensitive: Boolean,
  ): Text =
    Text(search, Some(language), Some(caseSensitive), Some(diacriticSensitive))

  /** Creates a filter that matches all documents for which the given expression is true.
    *
    * @param javaScriptExpression the JavaScript expression
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/where $where
    */
  def where(javaScriptExpression: String): Where = Where(javaScriptExpression)

  /** Allows the use of aggregation expressions within the query language.
    *
    * @param expression the aggregation expression
    * @param <TExpression> the expression type
    * @return the filter
    * @mongodb.server.release 3.6
    * @mongodb.driver.manual reference/operator/query/expr/ $expr
    */
  def expr(expression: String): Expr = Expr(expression)

  /** Creates a filter that matches all documents where the value of a field is an array that contains all the specified values.
    *
    * @param fieldName the field name
    * @param values    the list of values
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/all $all
    */
  def all[A](fieldName: String, values: Set[A])(implicit c: Codec[A]): All[A] =
    All[A](fieldName, values, c)

  /** Creates a filter that matches all documents where the value of a field is an array that contains all the specified values.
    *
    * @param fieldName the field name
    * @param values    the list of values
    * @tparam A        the value type
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/all $all
    */
  def all[A](fieldName: String, values: Seq[A])(implicit c: Codec[A]): All[A] =
    All[A](fieldName, values.toSet, c)

  /** Creates a filter that matches all documents containing a field that is an array where at least one member of the array matches the
    * given filter.
    *
    * @param fieldName the field name
    * @param filter    the filter to apply to each element
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/elemMatch $elemMatch
    */
  def elemMatch(fieldName: String, filter: Filter): ElemMatch = ElemMatch(fieldName, filter)

  /** Creates a filter that matches all documents where the value of a field is an array of the specified size.
    *
    * @param fieldName the field name
    * @param size      the size of the array
    * @return the filter
    * @mongodb.driver.manual reference/operator/query/size $size
    */
  def size(fieldName: String, size: Int): Size = Size(fieldName, size)

  /** Creates a filter that matches all documents where all of the bit positions are clear in the field.
    *
    * @param fieldName the field name
    * @param bitmask   the bitmask
    * @return the filter
    * @mongodb.server.release 3.2
    * @mongodb.driver.manual reference/operator/query/bitsAllClear $bitsAllClear
    */
  def bitsAllClear(fieldName: String, bitmask: Long): BitsAllClear =
    BitsAllClear(fieldName, bitmask)

  /** Creates a filter that matches all documents where all of the bit positions are set in the field.
    *
    * @param fieldName the field name
    * @param bitmask   the bitmask
    * @return the filter
    * @mongodb.server.release 3.2
    * @mongodb.driver.manual reference/operator/query/bitsAllSet $bitsAllSet
    */
  def bitsAllSet(fieldName: String, bitmask: Long): BitsAllSet =
    BitsAllSet(fieldName, bitmask)

  /** Creates a filter that matches all documents where any of the bit positions are clear in the field.
    *
    * @param fieldName the field name
    * @param bitmask   the bitmask
    * @return the filter
    * @mongodb.server.release 3.2
    * @mongodb.driver.manual reference/operator/query/BitsAnyClear $BitsAnyClear
    */
  def bitsAnyClear(fieldName: String, bitmask: Long): BitsAnyClear =
    BitsAnyClear(fieldName, bitmask)

  /** Creates a filter that matches all documents where any of the bit positions are set in the field.
    *
    * @param fieldName the field name
    * @param bitmask   the bitmask
    * @return the filter
    * @mongodb.server.release 3.2
    * @mongodb.driver.manual reference/operator/query/bitsAnySet $bitsAnySet
    */
  def bitsAnySet(fieldName: String, bitmask: Long): BitsAnySet =
    BitsAnySet(fieldName, bitmask)

  /** Creates a filter that matches all documents that validate against the given JSON schema document.
    *
    * @param schema the JSON schema to validate against
    * @return the filter
    * @mongodb.server.release 3.6
    * @mongodb.driver.manual reference/operator/query/jsonSchema/ $jsonSchema
    */
  def jsonSchema(schema: Bson): JsonSchema = JsonSchema(schema)

  /** Creates an empty filter that will match all documents.
    *
    * @return the filter
    */
  val empty: Filter = Empty
}
