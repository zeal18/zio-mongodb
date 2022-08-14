package io.github.zeal18.zio.mongodb.bson.annotations

import scala.annotation.StaticAnnotation

/** Annotation to mark a field as an identifier for a document.
  */
case class BsonId() extends StaticAnnotation
