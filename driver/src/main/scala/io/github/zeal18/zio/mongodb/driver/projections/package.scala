package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.projections.Projection.*
import org.bson.codecs.Codec

package object projections {

  /** Creates a projection of a field whose value is computed from the given expression.  Projection with an expression is only supported
    * using the `\$project` aggregation pipeline stage.
    *
    * @param fieldName     the field name
    * @param  expression   the expression
    * @tparam A  the expression type
    * @return the projection
    * @see Aggregates#project(Bson)
    */
  def computed[A](fieldName: String, expression: A)(implicit c: Codec[A]): Computed[A] =
    Computed(fieldName, expression, c)

  /** Creates a projection that includes all of the given fields.
    *
    * @param fieldNames the field names
    * @return the projection
    */
  def include(fieldNames: String*): Include = Include(fieldNames)

  /** Creates a projection that excludes all of the given fields.
    *
    * @param fieldNames the field names
    * @return the projection
    */
  def exclude(fieldNames: String*): Exclude = Exclude(fieldNames)

  /** Creates a projection that excludes the _id field.  This suppresses the automatic inclusion of _id that is the default, even when
    * other fields are explicitly included.
    *
    * @return the projection
    */
  def excludeId(): ExcludeId.type = ExcludeId

  /** Creates a projection that includes for the given field only the first element of an array that matches the query filter.  This is
    * referred to as the positional `\$` operator.
    *
    * @param fieldName the field name whose value is the array
    * @return the projection
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/projection/positional/#projection Project the first matching element (\$ operator)]]
    */
  def elemMatch(fieldName: String): ElemFirstMatch = ElemFirstMatch(fieldName)

  /** Creates a projection that includes for the given field only the first element of the array value of that field that matches the given
    * query filter.
    *
    * @param fieldName the field name
    * @param filter    the filter to apply
    * @return the projection
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/projection/elemMatch elemMatch]]
    */
  def elemMatch(fieldName: String, filter: Filter): ElemMatch =
    ElemMatch(fieldName, filter)

  /** Creates a `\$meta` projection to the given field name for the given meta field name.
    *
    * @param fieldName the field name
    * @param metaFieldName the meta field name
    * @return the projection
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/projection/meta/#projection meta]]
    */
  def meta(fieldName: String, metaFieldName: String): Meta =
    Meta(fieldName, metaFieldName)

  /** Creates a projection to the given field name of the textScore, for use with text queries.
    *
    * @param fieldName the field name
    * @return the projection
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/projection/meta/#projection textScore]]
    */
  def metaTextScore(fieldName: String): Meta = Meta(fieldName, "textScore")

  /** Creates a projection to the given field name of a slice of the array value of that field.
    *
    * @param fieldName the field name
    * @param limit the number of elements to project.
    * @return the projection
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/projection/slice Slice]]
    */
  def slice(fieldName: String, limit: Int): Slice = Slice(fieldName, skip = 0, limit)

  /** Creates a projection to the given field name of a slice of the array value of that field.
    *
    * @param fieldName the field name
    * @param skip the number of elements to skip before applying the limit
    * @param limit the number of elements to project
    * @return the projection
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/projection/slice Slice]]
    */
  def slice(fieldName: String, skip: Int, limit: Int): Slice =
    Slice(fieldName, skip, limit)

  /** Creates a projection that combines the list of projections into a single one.  If there are duplicate keys, the last one takes
    * precedence.
    *
    * @param projections the list of projections to combine
    * @return the combined projection
    */
  def fields(projections: Projection*): Fields = Fields(projections)
}
