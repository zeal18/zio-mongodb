package io.github.zeal18.zio.mongodb.bson.codecs

import zio.test.*

object MagnoliaCodecsSpec extends DefaultRunnableSpec {
  private case class Simple(a: Int, b: String)
  private object Simple {
    implicit val codec: Codec[Simple] = MagnoliaCodec.gen[Simple]
  }

  private case class Nested(a: Simple, b: Long)
  private object Nested {
    implicit val codec: Codec[Nested] = MagnoliaCodec.gen[Nested]
  }

  private case class SimpleAuto(c: Int, d: String)
  private case class NestedAuto(f: SimpleAuto, e: String)
  private object NestedAuto {
    implicit val codec: Codec[NestedAuto] = MagnoliaCodec.autoGen[NestedAuto]
  }

  private case object CaseObject {
    implicit val codec: Codec[CaseObject.type] = MagnoliaCodec.gen[CaseObject.type]
  }

  private class StringValueClass(val a: String) extends AnyVal
  private object StringValueClass {
    implicit val codec: Codec[StringValueClass] = MagnoliaCodec.gen[StringValueClass]
  }

  private class IntValueClass(val number: Int) extends AnyVal
  private object IntValueClass {
    implicit val codec: Codec[IntValueClass] = MagnoliaCodec.gen[IntValueClass]
  }

  sealed private trait SimpleEnum
  private object SimpleEnum {
    case object A extends SimpleEnum
    case object B extends SimpleEnum
    case object C extends SimpleEnum

    implicit val codec: Codec[SimpleEnum] = MagnoliaCodec.gen[SimpleEnum]
  }

  sealed private trait SimpleCoproduct
  private object SimpleCoproduct {
    case class A(a: Int)    extends SimpleCoproduct
    case class B(b: String) extends SimpleCoproduct
    case class C(c: Long)   extends SimpleCoproduct

    implicit val codec: Codec[SimpleCoproduct] = MagnoliaCodec.gen[SimpleCoproduct]
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

    implicit val codec: Codec[ComplexCoproduct] = MagnoliaCodec.gen[ComplexCoproduct]
  }

  sealed private trait Tree[A]
  private object Tree {
    case class Leaf[A](a: A)                          extends Tree[A]
    case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]

    implicit val codec: Codec[Tree[Int]] = MagnoliaCodec.gen[Tree[Int]]
  }

  sealed private trait ListTree[A]
  private object ListTree {
    case class Leaf[A](a: A)                        extends ListTree[A]
    case class Node[A](elements: List[ListTree[A]]) extends ListTree[A]

    implicit val codec: Codec[ListTree[Int]] = MagnoliaCodec.gen[ListTree[Int]]
  }

  override def spec: ZSpec[Environment, Failure] =
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
      suite("value class")(
        testCodecRoundtrip("string value class", new StringValueClass("2"), """"2""""),
        testCodecRoundtrip("int value class", new IntValueClass(42), """42"""),
      ),
      suite("simple enum")(
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
          Tree.Leaf(1),
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
    )
}
