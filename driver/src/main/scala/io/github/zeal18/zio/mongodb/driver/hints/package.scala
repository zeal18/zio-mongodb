package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.driver.indexes.Index
import io.github.zeal18.zio.mongodb.driver.indexes.IndexKey
import org.bson.BsonDocument
import org.bson.conversions.Bson

package object hints {

  /** Creates a hint based on the given index name.
    *
    * @param name the index name
    * @return the hint
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    */
  def indexName(name: String): Hint = Hint.IndexName(name)

  /** Creates a hint based on the given index key definition.
    *
    * @param key the index key
    * @return the hint
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    */
  def indexKey(key: IndexKey): Hint = Hint.Index(key)

  /** Creates a hint based on the given index definition.
    *
    * @param index the index definition
    * @return the hint
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    */
  def index(index: Index): Hint = Hint.Index(index.key)

  /** Creates a hint which forces the query to perform a forwards collection scan.
    *
    * @return the hint
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    */
  def forwardScan: Hint = Hint.ForwardScan

  /** Creates a hint which forces the query to perform a reverse collection scan.
    *
    * @return the hint
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    */
  def reverseScan: Hint = Hint.ReverseScan

  /** Creates a hint from a raw Bson.
    *
    * It is less type safe but useful when you want to use a hint that is not yet supported by this library.
    *
    * @param hint the raw Bson
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    */
  def raw(hint: Bson): Hint = Hint.Raw(hint)

  /** Creates a hint from a raw extended Json.
    *
    * It is less type safe but useful when you want to use a hint that is not yet supported by this library.
    *
    * @param json the raw extended Json
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.hint/ Hint]]
    * @see [[https://www.mongodb.com/docs/manual/reference/mongodb-extended-json/ MongoDB Extended JSON]]
    */
  def raw(json: String): Hint = Hint.Raw(BsonDocument.parse(json))
}
