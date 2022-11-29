package io.github.zeal18.zio.mongodb.bson.codecs

import scala.quoted.*

private case class Prepared[A](
  typ: Type[A],
  varRef: Expr[Codec[A]],
  complete: Option[() => Expr[Unit]],
):
  def subst[B] = this.asInstanceOf[Prepared[B]]

private object Prepared:
  def apply[A](codec: Expr[Codec[A]])(using t: Type[A]): Prepared[A] =
    Prepared[A](t, codec, complete = None)
