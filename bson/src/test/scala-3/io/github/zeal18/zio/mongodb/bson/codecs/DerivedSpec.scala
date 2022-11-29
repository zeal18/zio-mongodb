package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.bson.annotations.BsonIgnore
import io.github.zeal18.zio.mongodb.bson.annotations.BsonProperty
import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.*

object DerivedSpec extends ZIOSpecDefault {
  private case class Simple(a: Int, b: String)
  private case class Nested(a: Simple, b: Long)

  case class Root(foo: Option[Foo])
  case class Foo(bar: Option[Foo]) derives Codec

  case class Generic[B](a: B)

  case class VarargsField(va: Int*)

  case class DefaultField(a: Int, d: String = "default") derives Codec

  private case object CaseObject derives Codec

  opaque type StringOpaqueType = String
  private object StringOpaqueType {
    def apply(value: String): StringOpaqueType = value
  }
  opaque type IntOpaqueType = Int
  private object IntOpaqueType {
    def apply(value: Int): IntOpaqueType = value
  }

  sealed private trait SimpleEnum
  private object SimpleEnum {
    case object A extends SimpleEnum
    case object B extends SimpleEnum
    case object C extends SimpleEnum

    given Codec[SimpleEnum] = Codec.derived
  }

  enum Scala3Enum derives Codec:
    case A, B, C

  enum Scala3ParamEnum(a: Int) derives Codec:
    case A extends Scala3ParamEnum(1)
    case B extends Scala3ParamEnum(3)
    case C extends Scala3ParamEnum(42)

  sealed trait MixedCoproduct
  object MixedCoproduct:
    case class A(a: Int)    extends MixedCoproduct
    case class B(b: String) extends MixedCoproduct
    case object C           extends MixedCoproduct

    given Codec[MixedCoproduct] = Codec.derived[MixedCoproduct]

  enum Scala3CoproductEnum:
    case A(a: Int)
    case B(b: String)
    case C

  object Scala3CoproductEnum {
    given Codec[Scala3CoproductEnum] = Codec.derived[Scala3CoproductEnum]
  }

  sealed private trait ComplexCoproduct derives Codec
  private object ComplexCoproduct {
    case class A(a: Int)      extends ComplexCoproduct
    sealed trait SubCoproduct extends ComplexCoproduct
    object SubCoproduct {
      case class B(b: String) extends SubCoproduct
      case class C(c: Long)   extends SubCoproduct
    }
    case class D(d: Long) extends ComplexCoproduct
    sealed trait SubEnum  extends ComplexCoproduct
    object SubEnum {
      case object E extends SubEnum
      case object F extends SubEnum
    }

    enum SubScala3Enum extends ComplexCoproduct:
      case G, H

    enum SubScala3CoproductEnum extends ComplexCoproduct:
      case K(k: Int)
      case L(l: String)
  }

  sealed private trait Tree[A]
  private object Tree {
    case class Leaf[A](a: A)                          extends Tree[A]
    case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]

    given codec[A: Codec]: Codec[Tree[A]] = Codec.derived
  }

  sealed private trait ListTree[A]
  private object ListTree {
    case class Leaf[A](a: A)                        extends ListTree[A]
    case class Node[A](elements: List[ListTree[A]]) extends ListTree[A]

    given codec[A: Codec]: Codec[ListTree[A]] = Codec.derived
  }

  case class WithoutCodec(a: Int) derives Codec
  case class CaseClassWithOption(a: Option[WithoutCodec], b: Int) derives Codec

  case class OverridedChild(a: Int)
  object OverridedChild {
    implicit val codec: Codec[OverridedChild] =
      implicitly[Codec[Int]].bimap(OverridedChild.apply(_), _.a)
  }
  case class CaseClassWithOverridedChild(a: Option[OverridedChild], b: Int) derives Codec

  case class ListChild(a: Int) derives Codec
  case class CaseClassWithOptionList(a: Option[List[ListChild]], b: Int) derives Codec

  case class PropertyAnnotation(@BsonProperty("b") a: Int) derives Codec
  case class IgnoreAnnotation(a: Int, @BsonIgnore b: Int = 0) derives Codec
  case class IdAnnotation(@BsonId a: Int) derives Codec

  override def spec =
    suite("DerivedSpec")(
      suite("case class")(
        testCodecRoundtrip("simple case class", Simple(1, "2"), """{"a": 1, "b": "2"}"""),
        testCodecRoundtrip(
          "nested case class",
          Nested(Simple(1, "2"), 3L),
          """{"a": {"a": 1, "b": "2"}, "b": 3}""",
        ),
        suite("recursive type")(
          testCodecRoundtrip[Root](
            "Leaf",
            Root(Some(Foo(Some(Foo(Some(Foo(Some(Foo(None))))))))),
            """{"foo": {"bar": {"bar": {"bar": {"bar": null}}}}}""",
          ),
        ),
        testCodecRoundtrip("generic", Generic[Int](43), """{"a": 43}"""),
        testCodecRoundtrip("varargs field", VarargsField(1, 3, 42), """{"va": [1, 3, 42]}"""),
        suite("default field")(
          testCodecRoundtrip("set", DefaultField(1, "set"), """{"a": 1, "d": "set"}"""),
          testCodecDecode("not set", """{"a": 1}""", DefaultField(1, "default")),
        ),
      ),
      suite("case object")(
        testCodecRoundtrip("case object", CaseObject, """"CaseObject""""),
      ),
      suite("opaque type")(
        testCodecRoundtrip("string opaque type", StringOpaqueType("2"), """"2""""),
        testCodecRoundtrip("int opaque type", IntOpaqueType(42), """42"""),
      ),
      suite("simple sealed trait enum")(
        testCodecRoundtrip[SimpleEnum]("A", SimpleEnum.A, """"A""""),
        testCodecRoundtrip[SimpleEnum]("B", SimpleEnum.B, """"B""""),
        testCodecRoundtrip[SimpleEnum]("C", SimpleEnum.C, """"C""""),
      ),
      suite("mixed sealed trait coproduct")(
        testCodecRoundtrip[MixedCoproduct]("A", MixedCoproduct.A(3), """{"_t": "A", "a": 3}"""),
        testCodecRoundtrip[MixedCoproduct]("B", MixedCoproduct.B("c"), """{"_t": "B", "b": "c"}"""),
        testCodecRoundtrip[MixedCoproduct]("C", MixedCoproduct.C, """{"_t": "C"}"""),
      ),
      suite("scala 3 enum")(
        testCodecRoundtrip[Scala3Enum]("A", Scala3Enum.A, """"A""""),
        testCodecRoundtrip[Scala3Enum]("B", Scala3Enum.B, """"B""""),
        testCodecRoundtrip[Scala3Enum]("C", Scala3Enum.C, """"C""""),
      ),
      suite("scala 3 parametrised enum")(
        testCodecRoundtrip[Scala3ParamEnum]("A", Scala3ParamEnum.A, """"A""""),
        testCodecRoundtrip[Scala3ParamEnum]("B", Scala3ParamEnum.B, """"B""""),
        testCodecRoundtrip[Scala3ParamEnum]("C", Scala3ParamEnum.C, """"C""""),
      ),
      suite("scala 3 coproduct enum")(
        testCodecRoundtrip[Scala3CoproductEnum](
          "A",
          Scala3CoproductEnum.A(3),
          """{"_t": "A", "a": 3}""",
        ),
        testCodecRoundtrip[Scala3CoproductEnum](
          "B",
          Scala3CoproductEnum.B("c"),
          """{"_t": "B", "b": "c"}""",
        ),
        testCodecRoundtrip[Scala3CoproductEnum]("C", Scala3CoproductEnum.C, """{"_t": "C"}"""),
      ),
      suite("complex coproduct")(
        testCodecRoundtrip[ComplexCoproduct](
          "A",
          ComplexCoproduct.A(1),
          """{"_t": "A", "a": 1}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubCoproduct.B",
          ComplexCoproduct.SubCoproduct.B("2"),
          """{"_t": "B", "b": "2"}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubCoproduct.C",
          ComplexCoproduct.SubCoproduct.C(3L),
          """{"_t": "C", "c": 3}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "D",
          ComplexCoproduct.D(4L),
          """{"_t": "D", "d": 4}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubEnum.E",
          ComplexCoproduct.SubEnum.E,
          """{"_t": "E"}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubEnum.F",
          ComplexCoproduct.SubEnum.F,
          """{"_t": "F"}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubScala3Enum.G",
          ComplexCoproduct.SubScala3Enum.G,
          """{"_t": "G"}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubScala3Enum.H",
          ComplexCoproduct.SubScala3Enum.H,
          """{"_t": "H"}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubScala3CoproductEnum.K",
          ComplexCoproduct.SubScala3CoproductEnum.K(8),
          """{"_t": "K", "k": 8}""",
        ),
        testCodecRoundtrip[ComplexCoproduct](
          "SubScala3CoproductEnum.L",
          ComplexCoproduct.SubScala3CoproductEnum.L("ll"),
          """{"_t": "L", "l": "ll"}""",
        ),
      ),
      suite("recursive type")(
        testCodecRoundtrip[Tree[Int]](
          "Leaf",
          Tree.Leaf(1).asInstanceOf[Tree[Int]],
          """{"_t": "Leaf", "a": 1}""",
        ),
        testCodecRoundtrip[Tree[Int]](
          "Node",
          Tree.Node(Tree.Leaf(1), Tree.Leaf(2)),
          """{"_t": "Node", "left": {"_t": "Leaf", "a": 1}, "right": {"_t": "Leaf", "a": 2}}""",
        ),
      ),
      suite("recursive type with a collection")(
        testCodecRoundtrip[ListTree[Int]](
          "Leaf",
          ListTree.Leaf(1),
          """{"_t": "Leaf", "a": 1}""",
        ),
        testCodecRoundtrip[ListTree[Int]](
          "Node",
          ListTree.Node(List(ListTree.Leaf(1), ListTree.Leaf(2))),
          """{"_t": "Node", "elements": [{"_t": "Leaf", "a": 1}, {"_t": "Leaf", "a": 2}]}""",
        ),
      ),
      suite("case class with an option")(
        testCodecRoundtrip(
          "Some",
          CaseClassWithOption(Some(WithoutCodec(1)), 2),
          """{"a": {"a": 1}, "b": 2}""",
        ),
        testCodecRoundtrip(
          "None",
          CaseClassWithOption(None, 3),
          """{"a": null, "b": 3}""",
        ),
      ),
      suite("case class with overrided child codec")(
        testCodecRoundtrip(
          "Some",
          CaseClassWithOverridedChild(Some(OverridedChild(1)), 2),
          """{"a": 1, "b": 2}""",
        ),
        testCodecRoundtrip(
          "None",
          CaseClassWithOverridedChild(None, 3),
          """{"a": null, "b": 3}""",
        ),
      ),
      suite("case class with an option list")(
        testCodecRoundtrip(
          "None",
          CaseClassWithOptionList(None, 3),
          """{"a": null, "b": 3}""",
        ),
        testCodecRoundtrip(
          "Some(List)",
          CaseClassWithOptionList(Some(List(ListChild(1), ListChild(2))), 3),
          """{"a": [{"a": 1}, {"a": 2}], "b": 3}""",
        ),
        testCodecRoundtrip(
          "Some(List.empty)",
          CaseClassWithOptionList(Some(List.empty), 3),
          """{"a": [], "b": 3}""",
        ),
      ),
      suite("bson annotations")(
        testCodecRoundtrip("property", PropertyAnnotation(1), """{"b": 1}"""),
        testCodecRoundtrip("ignore", IgnoreAnnotation(2), """{"a": 2}"""),
        testCodecDecode("ignore value", """{"a": 4, "b": 9}""", IgnoreAnnotation(a = 4, b = 0)),
        testCodecRoundtrip("id", IdAnnotation(42), """{"_id": 42}"""),
      ),
    )
}
