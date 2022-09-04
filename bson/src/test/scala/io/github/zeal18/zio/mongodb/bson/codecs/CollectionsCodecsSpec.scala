package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.*

object CollectionsCodecsSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("CollectionsCodecsSpec")(
      suite("ListCodec")(
        testCodecRoundtrip[List[Int]]("empty List", List.empty, "[]"),
        testCodecRoundtrip[List[Int]]("List(1)", List(1), "[1]"),
        testCodecRoundtrip[List[Int]]("List(1, 2, 3)", List(1, 2, 3), "[1, 2, 3]"),
      ),
      suite("SeqCodec")(
        testCodecRoundtrip[Seq[Int]]("empty Seq", Seq.empty, "[]"),
        testCodecRoundtrip[Seq[Int]]("Seq(1)", Seq(1), "[1]"),
        testCodecRoundtrip[Seq[Int]]("Seq(1, 2, 3)", Seq(1, 2, 3), "[1, 2, 3]"),
      ),
      suite("VectorCodec")(
        testCodecRoundtrip[Vector[Int]]("empty Vector", Vector.empty, "[]"),
        testCodecRoundtrip[Vector[Int]]("Vector(1)", Vector(1), "[1]"),
        testCodecRoundtrip[Vector[Int]]("Vector(1, 2, 3)", Vector(1, 2, 3), "[1, 2, 3]"),
      ),
      suite("SetCodec")(
        testCodecRoundtrip[Set[Int]]("empty Set", Set.empty, "[]"),
        testCodecRoundtrip[Set[Int]]("Set(1)", Set(1), "[1]"),
        testCodecRoundtrip[Set[Int]]("Set(1, 2, 3)", Set(1, 2, 3), "[1, 2, 3]"),
      ),
    )
}
