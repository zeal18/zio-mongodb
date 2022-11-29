package io.github.zeal18.zio.mongodb.bson.codecs

import scala.quoted.*

private case class TypeInfo(
  owner: String,
  short: String,
  typeParams: Iterable[TypeInfo],
) {
  def full: String = s"$owner.$short"
}

private object TypeInfo:
  def apply[T: Type](using Quotes): TypeInfo =
    import quotes.reflect.*

    def normalizedName(s: Symbol): String =
      if s.flags.is(Flags.Module) then s.name.stripSuffix("$") else s.name

    def name(tpe: TypeRepr): String = tpe match
      case TermRef(typeRepr, name) if tpe.typeSymbol.flags.is(Flags.Module) =>
        name.stripSuffix("$")
      case TermRef(typeRepr, name) => name
      case _                       => normalizedName(tpe.typeSymbol)

    def ownerNameChain(sym: Symbol): List[String] =
      if sym.isNoSymbol then List.empty
      else if sym == defn.EmptyPackageClass then List.empty
      else if sym == defn.RootPackage then List.empty
      else if sym == defn.RootClass then List.empty
      else ownerNameChain(sym.owner) :+ normalizedName(sym)

    def owner(tpe: TypeRepr): String =
      ownerNameChain(tpe.typeSymbol.maybeOwner).mkString(".")

    def typeInfo(tpe: TypeRepr): TypeInfo = tpe match
      case AppliedType(tpe, args) =>
        TypeInfo(owner(tpe), name(tpe), args.map(typeInfo))
      case _ =>
        TypeInfo(owner(tpe), name(tpe), Nil)

    typeInfo(TypeRepr.of[T])
