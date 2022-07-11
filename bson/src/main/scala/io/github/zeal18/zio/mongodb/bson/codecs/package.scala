package io.github.zeal18.zio.mongodb.bson

package object codecs {

  /** Type alias to the `BsonTypeClassMap`
    */
  type BsonTypeClassMap = org.bson.codecs.BsonTypeClassMap

  /** Companion to return the default `BsonTypeClassMap`
    */
  object BsonTypeClassMap {
    def apply(): BsonTypeClassMap = new BsonTypeClassMap()
  }

  /** Type alias to the `BsonTypeCodecMap`
    */
  type BsonTypeCodecMap = org.bson.codecs.BsonTypeCodecMap
}
