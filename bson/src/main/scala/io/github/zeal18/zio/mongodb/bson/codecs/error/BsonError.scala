package io.github.zeal18.zio.mongodb.bson.codecs.error

import org.bson.BSONException

sealed trait BsonError extends Exception { self =>
  override def getMessage(): String = getMessage(0)

  def getMessage(depth: Int): String = {
    val prefix = "\t" * depth

    (self match {
      case BsonError.GeneralError(message) =>
        s"\n$prefix$message"
      case BsonError.SerializationError(error) =>
        s"\n$prefix${error.getMessage()}"
      case BsonError.ArrayError(index, error) =>
        s"\n${prefix}Error on element with index '$index': ${error.getMessage(depth + 1)}"
      case BsonError.ProductError(field, tpe, error) =>
        s"\n${prefix}Error on field '$field' with type '$tpe': ${error.getMessage(depth)}"
      case BsonError.CoproductError(subtype, error) =>
        s"\n${prefix}Error processing subtype '$subtype': ${error.getMessage(depth + 1)}"
      case BsonError.CodecError(name, error) =>
        s"\n${prefix}Codec '$name' failed with: ${error.getMessage(depth + 1)}"
    })
  }
}

private[codecs] object BsonError {
  final case class GeneralError(msg: String)                                       extends BsonError
  final case class SerializationError(error: BSONException)                        extends BsonError
  final case class ArrayError(index: Int, underlying: BsonError)                   extends BsonError
  final case class ProductError(field: String, tpe: String, underlying: BsonError) extends BsonError
  final case class CoproductError(subtype: String, underlying: BsonError)          extends BsonError
  final case class CodecError(name: String, underlying: BsonError)                 extends BsonError
}
