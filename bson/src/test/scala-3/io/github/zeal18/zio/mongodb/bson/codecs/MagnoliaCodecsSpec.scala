package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.bson.annotations.BsonIgnore
import io.github.zeal18.zio.mongodb.bson.annotations.BsonProperty
import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.*

object MagnoliaCodecsSpec extends ZIOSpecDefault {
  private case class Simple(a: Int, b: String)

  private case class Nested(a: Simple, b: Long)

  private case class SimpleAuto(c: Int, d: String)
  private case class NestedAuto(f: SimpleAuto, e: String)

  private case object CaseObject

  private opaque type StringOpaqueType = String
  private object StringOpaqueType {
    def apply(value: String): StringOpaqueType = value
  }
  private opaque type IntOpaqueType = Int
  private object IntOpaqueType {
    def apply(value: Int): IntOpaqueType = value
  }

  sealed private trait SimpleEnum
  private object SimpleEnum {
    case object A extends SimpleEnum
    case object B extends SimpleEnum
    case object C extends SimpleEnum
  }

  sealed private trait SimpleCoproduct
  private object SimpleCoproduct {
    case class A(a: Int)    extends SimpleCoproduct
    case class B(b: String) extends SimpleCoproduct
    case class C(c: Long)   extends SimpleCoproduct
  }

  sealed private trait ComplexCoproduct
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
  }

  sealed private trait Tree[A]
  private object Tree {
    case class Leaf[A](a: A)                          extends Tree[A]
    case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]

    implicit def codec[A: Codec]: Codec[Tree[A]] = Codec.derived[Tree[A]]
  }

  sealed private trait ListTree[A]
  private object ListTree {
    case class Leaf[A](a: A)                        extends ListTree[A]
    case class Node[A](elements: List[ListTree[A]]) extends ListTree[A]

    implicit def codec[A: Codec]: Codec[ListTree[A]] = Codec.derived[ListTree[A]]
  }

  case class WithoutCodec(a: Int)
  case class CaseClassWithOption(a: Option[WithoutCodec], b: Int)

  case class OverridedChild(a: Int)
  object OverridedChild {
    implicit val codec: Codec[OverridedChild] =
      implicitly[Codec[Int]].bimap(OverridedChild.apply(_), _.a)
  }
  case class CaseClassWithOverridedChild(a: Option[OverridedChild], b: Int)

  case class ListChild(a: Int)
  case class CaseClassWithOptionList(a: Option[List[ListChild]], b: Int)

  case class PropertyAnnotation(@BsonProperty("b") a: Int)
  case class IgnoreAnnotation(a: Int, @BsonIgnore b: Int = 0)
  case class IdAnnotation(@BsonId a: Int)

  override def spec =
    suite("MagnoliaCodecsSpec")(
      suite("case class")(
        testCodecRoundtrip("simple case class", Simple(1, "2"), """{"a": 1, "b": "2"}"""),
        testCodecRoundtrip(
          "nested case class",
          Nested(Simple(1, "2"), 3L),
          """{"a": {"a": 1, "b": "2"}, "b": 3}""",
        ),
        testCodecRoundtrip(
          "nested auto derived case class",
          NestedAuto(SimpleAuto(6, "7"), "5"),
          """{"f": {"c": 6, "d": "7"}, "e": "5"}""",
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
      suite("simple coproduct")(
        testCodecRoundtrip[SimpleCoproduct](
          "A",
          SimpleCoproduct.A(1),
          """{"_t": "A", "a": 1}""",
        ),
        testCodecRoundtrip[SimpleCoproduct](
          "B",
          SimpleCoproduct.B("2"),
          """{"_t": "B", "b": "2"}""",
        ),
        testCodecRoundtrip[SimpleCoproduct](
          "C",
          SimpleCoproduct.C(3L),
          """{"_t": "C", "c": 3}""",
        ),
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
      suite("autoderived case class with an option")(
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
      suite("autoderived case class with overrided child codec")(
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
      suite("autoderived case class with an option list")(
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
