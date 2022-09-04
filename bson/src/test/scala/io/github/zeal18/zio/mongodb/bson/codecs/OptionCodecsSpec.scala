package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.*

object OptionsCodecsSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("OptionsCodecsSpec")(
      suite("OptionCodec")(
        testCodecRoundtrip[Option[Int]]("None", None, "null"),
        testCodecRoundtrip[Option[Int]]("Some(1)", Some(1), "1"),
      ),
      suite("Option List")(
        testCodecRoundtrip[Option[List[Int]]]("None", None, "null"),
        testCodecRoundtrip[Option[List[Int]]]("Some(empty)", Some(List.empty), "[]"),
        testCodecRoundtrip[Option[List[Int]]]("Some(1)", Some(List(1)), "[1]"),
        testCodecRoundtrip[Option[List[Int]]]("Some(1, 2)", Some(List(1, 2)), "[1, 2]"),
      ),
      suite("nested options twice")(
        testCodecRoundtrip[Option[Option[Int]]]("None", None, "null"),
        testCodecRoundtrip[Option[Option[Int]]]("Some(None)", Some(None), """{"_option": null}"""),
        testCodecRoundtrip[Option[Option[Int]]](
          "Some(Some(1))",
          Some(Some(1)),
          """{"_option": 1}""",
        ),
      ),
      suite("nested options thrice")(
        testCodecRoundtrip[Option[Option[Option[Int]]]]("None", None, "null"),
        testCodecRoundtrip[Option[Option[Option[Int]]]](
          "Some(None)",
          Some(None),
          """{"_option": null}""",
        ),
        testCodecRoundtrip[Option[Option[Option[Int]]]](
          "Some(Some(None))",
          Some(Some(None)),
          """{"_option": {"_option": null}}""",
        ),
        testCodecRoundtrip[Option[Option[Option[Int]]]](
          "Some(Some(Some(1)))",
          Some(Some(Some(1))),
          """{"_option": {"_option": 1}}""",
        ),
      ),
      suite("nested options four times")(
        testCodecRoundtrip[Option[Option[Option[Option[Int]]]]]("None", None, "null"),
        testCodecRoundtrip[Option[Option[Option[Option[Int]]]]](
          "Some(None)",
          Some(None),
          """{"_option": null}""",
        ),
        testCodecRoundtrip[Option[Option[Option[Option[Int]]]]](
          "Some(Some(None))",
          Some(Some(None)),
          """{"_option": {"_option": null}}""",
        ),
        testCodecRoundtrip[Option[Option[Option[Option[Int]]]]](
          "Some(Some(Some(None)))",
          Some(Some(Some(None))),
          """{"_option": {"_option": {"_option": null}}}""",
        ),
        testCodecRoundtrip[Option[Option[Option[Option[Int]]]]](
          "Some(Some(Some(Some(1))))",
          Some(Some(Some(Some(1)))),
          """{"_option": {"_option": {"_option": 1}}}""",
        ),
      ),
    )
}
