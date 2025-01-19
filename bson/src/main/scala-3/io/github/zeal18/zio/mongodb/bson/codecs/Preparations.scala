package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.Macro.FakeCodec
import io.github.zeal18.zio.mongodb.bson.codecs.Macro.printResult
import io.github.zeal18.zio.mongodb.bson.codecs.Macro.summonLater

import scala.quoted.*

private class Preparations(logCode: Boolean)(using q: Quotes) {
  import q.reflect.*

  private val init1  = Init("a" + _)
  private val init2  = Init("b" + _, Flags.Lazy)
  private val lazies = List.newBuilder[Statement]
  private var preps  = Map.empty[TypeRepr, Prepared[?]]
  private val stable = collection.mutable.HashMap.empty[TypeRepr, Expr[Codec[Any]]]

  private def normalise[A](using t: Type[A]): TypeRepr =
    TypeRepr.of[A].dealias

  def addNow[A: Type](expr: Expr[Codec[A]]): Prepared[A] = {
    val vd = init1.valDef(expr, extraFlags = Flags.Implicit)
    val p  = Prepared[A](Type.of[A], vd.ref, None)
    preps = preps.updated(normalise[A], p)
    p
  }

  def addDeferred[A: Type](complete: => Expr[Codec[A]]): Prepared[A] =
    import Flags.*
    val name   = init1.newName()
    val theVar = init1.valDef('{ new FakeCodec[A](): Codec[A] }, name = name, extraFlags = Mutable)
    val theVal =
      init1.valDef(theVar.ref, name = s"_$name", extraFlags = Implicit | Lazy, onInit = false)

    lazies += theVal.valDef
    lazy val assignComplete = theVar.assign(complete)
    val p                   = Prepared[A](Type.of[A], theVar.ref, Some(() => assignComplete))
    preps = preps.updated(normalise[A], p)
    p

  /** @param typ Just `A`, not `Reusable[A]` */
  def get[A](using t: Type[A]): Option[Prepared[A]] =
    val t = normalise[A]
    preps.get(t).map(_.subst[A])

  /** @param typ Just `A`, not `Reusable[A]` */
  def need[A](using t: Type[A]): Prepared[A] =
    get[A].getOrElse(
      throw new IllegalStateException(s"Prepared type for ${normalise[A].show} not found!"),
    )

  def getOrSummonLater[A](using t: Type[A]): Expr[Codec[A]] =
    get[A] match {
      case Some(p) => p.varRef
      case None    => Expr.summonLater[Codec[A]]
    }

  def getStablisedImplicitInstance[A: Type]: Expr[Codec[A]] =
    def target: Expr[Codec[A]] =
      get[A] match
        case Some(p) => p.varRef
        case None    => init2.valDef(Expr.summonLater[Codec[A]]).ref
    stable.getOrElseUpdate(TypeRepr.of[A], target.asInstanceOf[Expr[Codec[Any]]]).asInstanceOf[Expr[Codec[A]]]

  def stabliseInstance[A: Type](e: Expr[Codec[A]]): Expr[Codec[A]] =
    init2.valDef(e).ref

  def result[A: Type](finalResult: Prepared[A]): Expr[Codec[A]] = {
    init1 ++= lazies.result()
    val result: Expr[Codec[A]] =
      init1.wrapExpr {
        init2.wrapExpr {
          val allPreps = preps.valuesIterator.flatMap(_.complete.map(_())).toList
          Expr.block(allPreps, finalResult.varRef)
        }
      }

    if logCode then printResult(result)

    result
  }
}

private object Preparations:
  def apply[A: Type](logCode: Boolean)(f: Preparations => Prepared[A])(using
    Quotes,
  ): Expr[Codec[A]] =
    val preparations = new Preparations(logCode)
    val prepared     = f(preparations)
    preparations.result(prepared)
