package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.driver.aggregates.Aggregation.*
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators.Accumulator
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions.Expression
import io.github.zeal18.zio.mongodb.driver.filters.Filter
import io.github.zeal18.zio.mongodb.driver.projections.Projection
import org.bson.BsonDocument
import org.bson.conversions.Bson

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
    * @param filter the filter to match
    * @return the `\$match` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/match/ \$match]]
    */
  def `match`(expression: Expression): MatchExpr = MatchExpr(expression)

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
  def group(id: Expression, fieldAccumulators: Map[String, Accumulator] = Map.empty): Group =
    Group(id, fieldAccumulators)

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
    * @param preserveNullAndEmptyArrays flag depicting if the unwind stage should include documents that have null values or empty arrays
    * @param arrayIndexFieldName the field to be used to store the array index of the unwound item
    *
    * @return the `\$unwind` pipeline stage
    *
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/unwind/ \$unwind]]
    */
  def unwind(
    fieldName: String,
    preserveNullAndEmptyArrays: Option[Boolean] = None,
    includeArrayIndex: Option[String] = None,
  ): Unwind =
    Unwind(fieldName, preserveNullAndEmptyArrays, includeArrayIndex)

  /** Creates a `\$sort` pipeline stage for the specified sort specification
    *
    * @param sort the sort specification
    * @see Sorts
    * @see [[http://docs.mongodb.org/manual/reference/operator/aggregation/sort/#sort-aggregation \$sort]]
    */
  def sort(sort: sorts.Sort): Aggregation.Sort = Aggregation.Sort(sort)

  /* Categorizes incoming documents into groups, called buckets, based on a specified expression and bucket boundaries and outputs a document per each bucket.
   *
   * @return the `\$bucket` pipeline stage
   * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bucket/ \$bucket]]
   */
  def bucket[Boundary: Numeric](
    groupBy: Expression,
    boundaries: Seq[Boundary],
  )(implicit
    boundariesCodec: Codec[Boundary],
  ): Bucket[Boundary, Int] =
    Bucket(groupBy, boundaries, None, Map.empty, boundariesCodec, Codec.int)

  /* Categorizes incoming documents into groups, called buckets, based on a specified expression and bucket boundaries and outputs a document per each bucket.
   *
   * @return the `\$bucket` pipeline stage
   * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bucket/ \$bucket]]
   */
  def bucket[Boundary: Numeric](
    groupBy: Expression,
    boundaries: Seq[Boundary],
    output: Map[String, Accumulator],
  )(implicit
    boundariesCodec: Codec[Boundary],
  ): Bucket[Boundary, Int] =
    Bucket(groupBy, boundaries, None, output, boundariesCodec, Codec.int)

  /* Categorizes incoming documents into groups, called buckets, based on a specified expression and bucket boundaries and outputs a document per each bucket.
   *
   * @return the `\$bucket` pipeline stage
   * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bucket/ \$bucket]]
   */
  def bucket[Boundary: Numeric, Default](
    groupBy: Expression,
    boundaries: Seq[Boundary],
    default: Default,
    output: Map[String, Accumulator] = Map.empty,
  )(implicit
    boundariesCodec: Codec[Boundary],
    defaultCodec: Codec[Default],
  ): Bucket[Boundary, Default] =
    Bucket(groupBy, boundaries, Some(default), output, boundariesCodec, defaultCodec)

  /* Categorizes incoming documents into a specific number of groups, called buckets, based on a specified expression. Bucket boundaries are automatically determined in an attempt to evenly distribute the documents into the specified number of buckets.
   *
   * @return the `\$bucketAuto` pipeline stage
   * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/bucketAuto/ \$bucketAuto]]
   */
  def bucketAuto(
    groupBy: Expression,
    buckets: Int,
    output: Map[String, Accumulator] = Map.empty,
    granularity: Option[BucketGranularity] = None,
  ): BucketAuto =
    BucketAuto(groupBy, buckets, output, granularity)

  /** Groups incoming documents based on the value of a specified expression, then computes the count of documents in each distinct group.
    *
    * @return the `\$sortByCount` pipeline stage
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation/sortByCount/ \$sortByCount]]
    */
  def sortByCount(expression: Expression): SortByCount = SortByCount(expression)

  /** Creates a pipeline from a raw Bson.
    *
    * It is less type safe but useful when you want to use a pipeline that is not yet supported by this library.
    *
    * @param pipeline the raw Bson
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation-pipeline/ Aggregation Pipeline Stages]]
    */
  def raw(pipeline: Bson): Raw = Raw(pipeline)

  /** Creates a pipeline from a raw extended Json.
    *
    * It is less type safe but useful when you want to use a pipeline that is not yet supported by this library.
    *
    * @param json the raw extended Json
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/aggregation-pipeline/ Aggregation Pipeline Stages]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): Raw = Raw(BsonDocument.parse(json))
}
