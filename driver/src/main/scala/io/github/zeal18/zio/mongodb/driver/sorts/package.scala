package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.driver.sorts.Sort.Asc
import io.github.zeal18.zio.mongodb.driver.sorts.Sort.Compound
import io.github.zeal18.zio.mongodb.driver.sorts.Sort.Desc
import io.github.zeal18.zio.mongodb.driver.sorts.Sort.TextScore

package object sorts {

  /** Create a sort specification for an ascending sort on the given fields.
    *
    * @param fieldNames the field names, which must contain at least one
    * @return the sort specification
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.sort/ Sort]]
    */
  def asc(fieldNames: String*): Sort = Compound(fieldNames.map(Asc(_)))

  /** Create a sort specification for a descending sort on the given fields.
    *
    * @param fieldNames the field names, which must contain at least one
    * @return the sort specification
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.sort/ Sort]]
    */
  def desc(fieldNames: String*): Sort = Compound(fieldNames.map(Desc(_)))

  /** Create a sort specification for the text score meta projection on the given field.
    *
    * @param fieldName the field name
    * @return the sort specification
    * @see Filters#text(String, TextSearchOptions)
    * @see [[https://www.mongodb.com/docs/manual/reference/method/cursor.sort/ Sort]]
    * @see [[https://www.mongodb.com/docs/manual/reference/operator/query/text/ Text search]]
    */
  def textScore(fieldNames: String*): Sort = Compound(fieldNames.map(TextScore(_)))

  /** Combine multiple sort specifications. If any field names are repeated, the last one takes precedence.
    *
    * @param sorts the sort specifications
    * @return the combined sort specification
    */
  def compound(sortings: Sort*): Sort = Compound(sortings)
}
