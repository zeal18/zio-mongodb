package io.github.zeal18.zio.mongodb.bson.codecs

import scala.deriving.*
import scala.quoted.*

private trait Field:
  type Name
  type Type

  val idx: Int
  val name: String
  def showType: String
  implicit val typeInstance: scala.quoted.Type[Type]

  final def typeRepr(using q: Quotes): q.reflect.TypeRepr =
    q.reflect.TypeRepr.of(using typeInstance)
end Field

private object Fields:
  def fromMirror[A: Type](m: Expr[Mirror.Of[A]])(using Quotes): List[Field] =
    import quotes.reflect.*

    def go[Ls: Type, Ts: Type](idx: Int): List[Field] =
      (Type.of[Ls], Type.of[Ts]) match
        case ('[l *: ll], '[t *: tt]) =>
          val t     = Type.of[t]
          val _idx  = idx
          val _name = TypeRepr.of[l] match
            case ConstantType(StringConstant(n)) => n
            case _                               => "?"
          val f: Field = new Field {
            override type Name = l
            override type Type = t
            override val idx                   = _idx
            override val name                  = _name
            override def showType              = Type.show[t]
            implicit override val typeInstance = t
          }
          f :: go[ll, tt](idx + 1)

        case ('[EmptyTuple], _) => Nil
    end go

    m match
      case '{
            $m: Mirror.ProductOf[A] { type MirroredElemLabels = ls; type MirroredElemTypes = ts }
          } =>
        go[ls, ts](0)
      case '{ $m: Mirror.SumOf[A] { type MirroredElemLabels = ls; type MirroredElemTypes = ts } } =>
        go[ls, ts](0)
  end fromMirror
end Fields
