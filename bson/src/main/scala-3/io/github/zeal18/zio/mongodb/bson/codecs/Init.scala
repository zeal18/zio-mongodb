package io.github.zeal18.zio.mongodb.bson.codecs

import scala.quoted.*
import EasierValDef.*

object Init:
  def apply(using qq: Quotes)(
    freshNameFn: Int => String = "i" + _,
    flags: qq.reflect.Flags = qq.reflect.Flags.EmptyFlags,
  ): Init { val q: qq.type } =
    new Init(using qq)(freshNameFn, flags).asInstanceOf[Init { val q: qq.type }] // TODO: S3 bug

class Init(using val q: Quotes)(freshNameFn: Int => String, flags: q.reflect.Flags):
  import q.reflect.*

  var seen: Map[Expr[Any], TypedValDef[Any]] = Map.empty
  var stmts                                  = Vector.empty[Statement]

  private var vars = 0
  def newName(): String =
    vars += 1
    freshNameFn(vars)

  def +=(t: Statement): Unit =
    stmts :+= t

  def ++=(ts: IterableOnce[Statement]): Unit =
    ts.iterator.foreach(this.+=)

  def valDef[A: Type](
    expr: Expr[A],
    name: String = newName(),
    reuse: Boolean = true,
    extraFlags: Flags = Flags.EmptyFlags,
    onInit: Boolean = true,
  ): TypedValDef.WithQuotes[A, q.type] =
    val allFlags    = flags | extraFlags
    val allowReuse  = reuse && !allFlags.is(Flags.Mutable)
    val alreadySeen = if allowReuse then seen.get(expr) else None
    alreadySeen match
      case None =>
        val vd = typedValDef(name, allFlags)(expr)
        if onInit then this += vd.valDef
        seen += ((expr, vd.subst[Any]))
        vd
      case Some(vd) =>
        vd.subst[A].substQ

  def wrapTerm(term: Term): Block =
    Block(stmts.toList, term)

  def wrapExpr[A: Type](expr: Expr[A]): Expr[A] =
    wrapTerm(expr.asTerm).asExprOf[A]
