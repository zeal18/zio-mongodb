package io.github.zeal18.zio.mongodb.bson.codecs

/** Represents a case class
  *
  * @param shortName class name for better error reporting
  * @param decode function to build a case class from a map of fields
  * @param params list of params
  */
case class CaseClass[A](
  shortName: String,
  decode: Map[String, Any] => A,
  fields: List[CaseClass.Field[?, A]],
)

object CaseClass:
  /** A case class field representation
    *
    * @param label field label (not "name" because it could be renamed)
    * @param codec codec to be able to decode the field
    * @param deref function to extract the field value from a case class
    */
  case class Field[FieldT, ClassT](
    label: String,
    codec: () => Codec[FieldT],
    deref: ClassT => FieldT,
  )
