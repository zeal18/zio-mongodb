package io.github.zeal18.zio.mongodb.driver

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

  /** Create an index key for a geohaystack index on the given field.
    *
    * <p>
    * <strong>Note: </strong>For queries that use spherical geometry, a 2dsphere index is a better option than a haystack index.
    * 2dsphere indexes allow field reordering; geoHaystack indexes require the first field to be the location field. Also, geoHaystack
    * indexes are only usable via commands and so always return all results at once..
    * </p>
    *
    * @param fieldName the field to create a geoHaystack index on
    * @param additional the additional field that forms the geoHaystack index key
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/geohaystack geoHaystack index]]
    */
  @deprecated("geoHaystack is deprecated in MongoDB 4.4", "4.2.1")
  def geoHaystack(fieldName: String, additional: String): IndexKey.GeoHaystack =
    IndexKey.GeoHaystack(fieldName, additional)

  /** Create an index key for a text index on the given field.
    *
    * @param fieldName the field to create a text index on
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/text text index]]
    */
  def text(fieldNames: String*): IndexKey.Text = IndexKey.Text(fieldNames)

  /** Create an index key for a hashed index on the given field.
    *
    * @param fieldName the field to create a hashed index on
    * @return the index specification
    * @see [[https://www.mongodb.com/docs/manual/core/hashed hashed index]]
    */
  def hashed(fieldNames: String*): IndexKey.Hashed = IndexKey.Hashed(fieldNames)

  /** create a compound index specifications.  If any field names are repeated, the last one takes precedence.
    *
    * @param indexes the index specifications
    * @return the compound index specification
    * @see [[https://www.mongodb.com/docs/manual/core/index-compound compoundIndex]]
    */
  def compound(indexes: IndexKey*): IndexKey.Compound = IndexKey.Compound(indexes)
}
