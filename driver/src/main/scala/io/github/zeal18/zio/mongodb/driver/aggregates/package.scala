package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation.*
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.model.UnwindOptions
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import org.bson.codecs.Codec

package object aggregates {

  /** Creates a `\$count` pipeline stage using the field name "count" to store the result
    *
    * @return the `\$count` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/count/ \$count]]
    * @note Requires MongoDB 3.4 or greater
    */
  def count(): Count = Count("count")

  /** Creates a `\$count` pipeline stage using the named field to store the result
    *
    * @param field the field in which to store the count
    * @return the `\$count` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/count/ \$count]]
    * @note Requires MongoDB 3.4 or greater
    */
  def count(field: String): Count = Count(field)

  /** Creates a `\$match` pipeline stage for the specified filter
    *
    * @param filter the filter to match
    * @return the `\$match` pipeline stage
    * @see Filters
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/match/ \$match]]
    */
  def `match`(filter: Filter): Match = Match(filter)

  /** Creates a `\$match` pipeline stage for the specified filter
    *
    * A friendly alias for the `match` method.
    *
    * @param filter the filter to match against
    * @return the `\$match` pipeline stage
    * @see Filters
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/match/ \$match]]
    */
  def filter(filter: Filter): Match = Match(filter)

  /** Creates a `\$facet` pipeline stage
    *
    * @param facets the facets to use
    * @return the new pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/facet/ \$facet]]
    * @note Requires MongoDB 3.4 or greater
    */
  def facet(facets: Facet*): Facets = Facets(facets)

  /** Creates a `\$limit` pipeline stage for the specified filter
    *
    * @param limit the limit
    * @return the `\$limit` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/limit/ \$limit]]
    */
  def limit(limit: Int): Limit = Limit(limit)

  /** Creates a `\$group` pipeline stage for the specified filter
    *
    * @param id the id expression for the group
    * @param fieldAccumulators zero or more field accumulator pairs
    * @tparam TExpression the expression type
    * @return the `\$group` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/group/ \$group]]
    * @see [[https://www.mongodb.com/docs/manual/meta/aggregation-quick-reference/#aggregation-expressions Expressions]]
    */
  def group[A](id: A, fieldAccumulators: Accumulator*)(implicit c: Codec[A]): Group[A] =
    Group(id, fieldAccumulators, c)

  /** Creates a `\$lookup` pipeline stage for the specified filter
    *
    * @param from the name of the collection in the same database to perform the join with.
    * @param localField specifies the field from the local collection to match values against.
    * @param foreignField specifies the field in the from collection to match values against.
    * @param as the name of the new array field to add to the input documents.
    * @return the `\$lookup` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/ \$lookup]]
    * @note Requires MongoDB 3.2 or greater
    */
  def lookup(from: String, localField: String, foreignField: String, as: String): Lookup =
    Lookup(from, localField, foreignField, as)

  /** Creates a `\$lookup` pipeline stage, joining the current collection with the one specified in from using the given pipeline
    *
    * @param from     the name of the collection in the same database to perform the join with.
    * @param pipeline the pipeline to run on the joined collection.
    * @param as       the name of the new array field to add to the input documents.
    * @return         the `\$lookup` pipeline stage:
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/ \$lookup]]
    * @note Requires MongoDB 3.6 or greater
    */
  def lookup(from: String, pipeline: Seq[Aggregation], as: String): LookupPipeline =
    LookupPipeline(from, let = Seq.empty, pipeline, as)

  /** Creates a `\$lookup` pipeline stage, joining the current collection with the one specified in from using the given pipeline
    *
    * @param from     the name of the collection in the same database to perform the join with.
    * @param let      the variables to use in the pipeline field stages.
    * @param pipeline the pipeline to run on the joined collection.
    * @param as       the name of the new array field to add to the input documents.
    * @return         the `\$lookup` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/ \$lookup]]
    * @note Requires MongoDB 3.6 or greater
    */
  def lookup(
    from: String,
    let: Seq[Variable[?]],
    pipeline: Seq[Aggregation],
    as: String,
  ): LookupPipeline =
    LookupPipeline(from, let, pipeline, as)

  /** Creates a `\$project` pipeline stage for the specified projection
    *
    * @param projection the projection
    * @return the `\$project` pipeline stage
    * @see Projections
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/ \$project]]
    */
  def project(projection: Projection): Project = Project(projection)

  /** Creates a `\$unwind` pipeline stage for the specified field name, which must be prefixed by a `\$` sign.
    *
    * @param fieldName the field name, prefixed by a  `\$` sign
    * @return the `\$unwind` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/unwind/ \$unwind]]
    */
  def unwind(fieldName: String): Unwind = Unwind(fieldName, new UnwindOptions())

  /** Creates a `\$unwind` pipeline stage for the specified field name, which must be prefixed by a `\$` sign.
    *
    * @param fieldName the field name, prefixed by a  `\$` sign
    * @return the `\$unwind` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/unwind/ \$unwind]]
    */
  def unwind(fieldName: String, unwindOptions: UnwindOptions): Unwind =
    Unwind(fieldName, unwindOptions)
}
