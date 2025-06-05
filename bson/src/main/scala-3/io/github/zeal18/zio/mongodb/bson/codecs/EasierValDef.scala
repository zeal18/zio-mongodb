package io.github.zeal18.zio.mongodb.bson.codecs

import scala.quoted.*

object EasierValDef {

  def untypedValDef(using
    q: Quotes,
  )(name: String, tpe: q.reflect.TypeRepr, flags: q.reflect.Flags = q.reflect.Flags.EmptyFlags)(
    rhs: q.reflect.Term,
  ): UntypedValDef.WithQuotes[q.type] =
    import quotes.reflect.*
    val sym = Symbol.newVal(Symbol.spliceOwner, name, tpe, flags, Symbol.noSymbol)
    val vd  = ValDef(sym, Some(rhs))
    Ref(sym) match
      case ref: Ident => UntypedValDef(using q)(sym, vd, ref)

  object UntypedValDef:
    type WithQuotes[Q <: Quotes] = UntypedValDef { val q: Q }

    def apply(using
      q: Quotes,
    )(
      symbol: q.reflect.Symbol,
      valDef: q.reflect.ValDef,
      ref: q.reflect.Ident,
    ): WithQuotes[q.type] =
      new UntypedValDef(using q)(symbol, valDef, ref).asInstanceOf[WithQuotes[q.type]] // TODO: S3

  final class UntypedValDef(using val q: Quotes)(
    val symbol: q.reflect.Symbol,
    val valDef: q.reflect.ValDef,
    val ref: q.reflect.Ident,
  ):
    import q.reflect.*

    def substQ(using qq: Quotes) =
      this.asInstanceOf[UntypedValDef.WithQuotes[qq.type]]

    def as[A: Type]: TypedValDef.WithQuotes[A, q.type] =
      TypedValDef(symbol, valDef, ref.asExprOf[A])

    def assign(rhs: Term): Assign =
      Assign(ref, rhs)

    def modify(f: Ident => Term): Assign =
      assign(f(ref))

  // ===================================================================================================================

  def typedValDef[A: Type](using
    q: Quotes,
  )(name: String, flags: q.reflect.Flags = q.reflect.Flags.EmptyFlags)(
    rhs: Expr[A],
  ): TypedValDef.WithQuotes[A, q.type] =
    import quotes.reflect.*
    val u = untypedValDef(using q)(name, TypeRepr.of[A], flags)(rhs.asTerm)
    u.as[A]

  object TypedValDef:
    type WithQuotes[A, Q <: Quotes] = TypedValDef[A] { val q: Q }

    def apply[A](using
      q: Quotes,
    )(symbol: q.reflect.Symbol, valDef: q.reflect.ValDef, ref: Expr[A]): WithQuotes[A, q.type] =
      new TypedValDef(using q)(symbol, valDef, ref).asInstanceOf[WithQuotes[A, q.type]] // TODO: S3

  final class TypedValDef[A](using val q: Quotes)(
    val symbol: q.reflect.Symbol,
    val valDef: q.reflect.ValDef,
    val ref: Expr[A],
  ) { self =>
    import q.reflect.*

    lazy val untyped: UntypedValDef.WithQuotes[q.type] =
      ref.asTerm match
        case i: Ident => (new UntypedValDef(using q)(symbol, valDef, i)).substQ

    def subst[B] =
      this.asInstanceOf[TypedValDef.WithQuotes[B, self.q.type]]

    def substQ(using qq: Quotes) =
      this.asInstanceOf[TypedValDef.WithQuotes[A, qq.type]]

    def use[B: Type](f: Expr[A] => Expr[B]): Expr[B] =
      Block(valDef :: Nil, f(ref).asTerm).asExprOf[B]

    def assignTerm(rhs: Expr[A]): Assign =
      untyped.assign(rhs.asTerm)

    def assign(rhs: Expr[A]): Expr[Unit] =
      assignTerm(rhs).asExprOf[Unit]

    def modify(f: Expr[A] => Expr[A]): Expr[Unit] =
      assign(f(ref))
  }
}
