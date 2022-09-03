package io.github.zeal18.zio.mongodb.driver.hints

import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonInt32
import io.github.zeal18.zio.mongodb.driver.indexes.IndexKey

sealed trait Hint { self =>
  def toBson: Either[String, BsonDocument] =
    self match {
      case Hint.IndexName(name) => Left(name)
      case Hint.Index(key)      => Right(key.toBson.toBsonDocument())
      case Hint.ForwardScan     => Right(new BsonDocument("$natural", new BsonInt32(1)))
      case Hint.ReverseScan     => Right(new BsonDocument("$natural", new BsonInt32(-1)))
    }
}

object Hint {
  final case class IndexName(name: String) extends Hint
  final case class Index(key: IndexKey)    extends Hint
  case object ForwardScan                  extends Hint
  case object ReverseScan                  extends Hint
}
