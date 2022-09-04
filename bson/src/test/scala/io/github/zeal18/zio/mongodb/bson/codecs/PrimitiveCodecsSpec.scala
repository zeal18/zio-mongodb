package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.*

object PrimitiveCodecsSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] =
    suite("PrimitiveCodecsSpec")(
      suite("BooleanCodec")(
        testCodecRoundtrip("true", true, "true"),
        testCodecRoundtrip("false", false, "false"),
      ),
      suite("IntCodec")(
        testCodecRoundtrip("1", 1, "1"),
        testCodecRoundtrip("-1", -1, "-1"),
        testCodecRoundtrip("0", 0, "0"),
        testCodecRoundtrip("Int.MaxValue", Int.MaxValue, Int.MaxValue.toString),
        testCodecRoundtrip("Int.MinValue", Int.MinValue, Int.MinValue.toString),
      ),
      suite("LongCodec")(
        testCodecRoundtrip("1", 1L, "1"),
        testCodecRoundtrip("-1", -1L, "-1"),
        testCodecRoundtrip("0", 0L, "0"),
        testCodecRoundtrip("Long.MaxValue", Long.MaxValue, Long.MaxValue.toString),
        testCodecRoundtrip("Long.MinValue", Long.MinValue, Long.MinValue.toString),
      ),
      suite("ShortCodec")(
        testCodecRoundtrip("1", 1.toShort, "1"),
        testCodecRoundtrip("-1", -1.toShort, "-1"),
        testCodecRoundtrip("0", 0.toShort, "0"),
        testCodecRoundtrip("Short.MaxValue", Short.MaxValue, Short.MaxValue.toString),
        testCodecRoundtrip("Short.MinValue", Short.MinValue, Short.MinValue.toString),
      ),
      suite("DoubleCodec")(
        testCodecRoundtrip("1.0", 1.0, "1.0"),
        testCodecRoundtrip("-1.0", -1.0, "-1.0"),
        testCodecRoundtrip("0.0", 0.0, "0.0"),
        testCodecRoundtrip("Double.MaxValue", Double.MaxValue, Double.MaxValue.toString),
        testCodecRoundtrip("Double.MinValue", Double.MinValue, Double.MinValue.toString),
      ),
      suite("FloatCodec")(
        testCodecRoundtrip("1.0", 1.0f, "1.0"),
        testCodecRoundtrip("-1.0", -1.0f, "-1.0"),
        testCodecRoundtrip("0.0", 0.0f, "0.0"),
        testCodecRoundtrip("Float.MaxValue", Float.MaxValue, "3.4028234663852886E38"),
        testCodecRoundtrip("Float.MinValue", Float.MinValue, "-3.4028234663852886E38"),
      ),
      suite("ByteCodec")(
        testCodecRoundtrip("1", 1.toByte, "1"),
        testCodecRoundtrip("-1", -1.toByte, "-1"),
        testCodecRoundtrip("0", 0.toByte, "0"),
        testCodecRoundtrip("Byte.MaxValue", Byte.MaxValue, Byte.MaxValue.toString),
        testCodecRoundtrip("Byte.MinValue", Byte.MinValue, Byte.MinValue.toString),
      ),
      suite("StringCodec")(
        testCodecRoundtrip("empty string", "", """"""""),
        testCodecRoundtrip("Hello world", "Hello world", """"Hello world""""),
      ),
    )
}
