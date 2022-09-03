package io.github.zeal18.zio.mongodb.driver.model

import com.mongodb.client.model.ReturnDocument as JReturnDocument

/** Indicates which document to return, the original document before change or the document after the change
  */
sealed trait ReturnDocument {
  private[driver] def toJava: JReturnDocument = this match {
    case ReturnDocument.Before => JReturnDocument.BEFORE
    case ReturnDocument.After  => JReturnDocument.AFTER
  }
}

object ReturnDocument {

  /** Indicates to return the document before the update, replacement, or insert occurred.
    */
  case object Before extends ReturnDocument

  /** Indicates to return the document after the update, replacement, or insert occurred.
    */
  case object After extends ReturnDocument
}
