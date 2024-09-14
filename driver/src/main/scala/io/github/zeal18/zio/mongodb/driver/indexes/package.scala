package io.github.zeal18.zio.mongodb.driver

import org.bson.BsonDocument
import org.bson.conversions.Bson

package object indexes {

  /** Create an index key for an ascending index on the given fields.
    *
    * @param fieldNames the field names, which must contain at least one
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/indexes indexes]]
    */
  def asc(fieldNames: String*): IndexKey.Asc = IndexKey.Asc(fieldNames)

  /** Create an index key for an ascending index on the given fields.
    *
    * @param fieldNames the field names, which must contain at least one
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/indexes indexes]]
    */
  def desc(fieldNames: String*): IndexKey.Desc = IndexKey.Desc(fieldNames)

  /** Create an index key for an 2dsphere index on the given fields.
    *
    * @param fieldNames the field names, which must contain at least one
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/2dsphere 2dsphere IndexKey]]
    */
  def geo2dsphere(fieldNames: String*): IndexKey.Geo2dsphere = IndexKey.Geo2dsphere(fieldNames)

  /** Create an index key for a 2d index on the given field.
    *
    * <p>
    * <strong>Note: </strong>A 2d index is for data stored as points on a two-dimensional plane.
    * The 2d index is intended for legacy coordinate pairs used in MongoDB 2.2 and earlier.
    * </p>
    *
    * @param fieldName the field to create a 2d index on
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/2d 2d index]]
    */
  def geo2d(fieldNames: String*): IndexKey.Geo2d = IndexKey.Geo2d(fieldNames)

  /** Create an index key for a text index on the given field.
    *
    * @param fieldName the field to create a text index on
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/index-text text index]]
    */
  def text(fieldNames: String*): IndexKey.Text = IndexKey.Text(fieldNames)

  /** Create an index key for a hashed index on the given field.
    *
    * @param fieldName the field to create a hashed index on
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/index-hashed hashed index]]
    */
  def hashed(fieldNames: String*): IndexKey.Hashed = IndexKey.Hashed(fieldNames)

  /** create a compound index specifications.  If any field names are repeated, the last one takes precedence.
    *
    * @param indexes the index specifications
    * @return the compound index specification
    * @see [[https://www.mongodb.com/docs/manual/core/index-compound compoundIndex]]
    */
  def compound(indexes: IndexKey*): IndexKey.Compound = IndexKey.Compound(indexes)

  /** Creates an index from a raw Bson.
    *
    * It is less type safe but useful when you want to use an index that is not yet supported by this library.
    *
    * @param index the raw Bson
    * @see [[https://www.mongodb.com/docs/manual/indexes/ Indexes]]
    */
  def raw(index: Bson): IndexKey.Raw = IndexKey.Raw(index)

  /** Creates an index from a raw extended Json.
    *
    * It is less type safe but useful when you want to use an index that is not yet supported by this library.
    *
    * @param json the raw extended Json
    * @see [[https://www.mongodb.com/docs/manual/indexes/ Indexes]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): IndexKey.Raw = IndexKey.Raw(BsonDocument.parse(json))
}
