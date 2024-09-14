package io.github.zeal18.zio.mongodb.bson.codecs

import scala.annotation.nowarn

import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import io.github.zeal18.zio.mongodb.bson.codecs.utils.*
import zio.test.*

object DerivedCodecErrorsSpec extends ZIOSpecDefault {
  private case class Simple(a: Int)

  sealed private trait SimpleEnum
  private object SimpleEnum {
    case object A extends SimpleEnum
    case object B extends SimpleEnum
  }
  private case class EnumWrapper(b: SimpleEnum)

  sealed private trait SimpleCoproduct
  private object SimpleCoproduct {
    case class A(a: Int)    extends SimpleCoproduct
    case class B(b: String) extends SimpleCoproduct
  }
  private case class CoproductWrapper(c: SimpleCoproduct)

  private case class WrappedList[A](e: List[A])

  override def spec =
    suite("DerivedCodecErrorsSpec")(
      suite("case class")(
        testCodecDecodeError[Simple](
          "wrong field type",
          """{"a": "wrong"}""",
          (result: BsonError) =>
            assertTrue(result.asInstanceOf[BsonError.ProductError].field == "a") &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .name == "java.lang.Integer",
              ),
        ),
      ),
      suite("enum")(
        testCodecDecodeError[EnumWrapper](
          "wrong enum value",
          """{"b": "wrong"}""",
          (result: BsonError) =>
            assertTrue(result.asInstanceOf[BsonError.ProductError].field == "b") &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .name == "io.github.zeal18.zio.mongodb.bson.codecs.DerivedCodecErrorsSpec.SimpleEnum",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.CoproductError]
                  .subtype == "wrong",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.CoproductError]
                  .underlying
                  .asInstanceOf[BsonError.GeneralError]
                  .msg == "unsupported discriminator value",
              ),
        ),
      ),
      suite("coproduct")(
        testCodecDecodeError[CoproductWrapper](
          "missing descriminator field",
          """{"c": {"a": 1}}""",
          (result: BsonError) =>
            assertTrue(result.asInstanceOf[BsonError.ProductError].field == "c") &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .name == "io.github.zeal18.zio.mongodb.bson.codecs.DerivedCodecErrorsSpec.SimpleCoproduct",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.SerializationError]
                  .error
                  .getMessage == "Expected element name to be '_t', not 'a'.",
              ),
        ),
        testCodecDecodeError[CoproductWrapper](
          "wrong descriminator value",
          """{"c": {"_t": "wrong"}}""",
          (result: BsonError) =>
            assertTrue(result.asInstanceOf[BsonError.ProductError].field == "c") &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .name == "io.github.zeal18.zio.mongodb.bson.codecs.DerivedCodecErrorsSpec.SimpleCoproduct",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.CoproductError]
                  .subtype == "wrong",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.CoproductError]
                  .underlying
                  .asInstanceOf[BsonError.GeneralError]
                  .msg == "unsupported discriminator value",
              ),
        ),
        testCodecDecodeError[CoproductWrapper](
          "wrong value type",
          """{"c": {"_t": "A", "a": "wrong"}}""",
          (result: BsonError) =>
            assertTrue(result.asInstanceOf[BsonError.ProductError].field == "c") &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .name == "io.github.zeal18.zio.mongodb.bson.codecs.DerivedCodecErrorsSpec.SimpleCoproduct",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.CoproductError]
                  .subtype == "A",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.CoproductError]
                  .underlying
                  .asInstanceOf[BsonError.ProductError]
                  .field == "a",
              ),
        ),
      ),
      suite("collection")(
        testCodecDecodeError[WrappedList[Int]](
          "unexpected type",
          """{"e": [1, "2", 4]}""",
          (result: BsonError) =>
            assertTrue(result.asInstanceOf[BsonError.ProductError].field == "e") &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .name == "scala.collection.immutable.List",
              ) &&
              assertTrue(
                result
                  .asInstanceOf[BsonError.ProductError]
                  .underlying
                  .asInstanceOf[BsonError.CodecError]
                  .underlying
                  .asInstanceOf[BsonError.ArrayError]
                  .index == 1,
              ),
        ),
      ),
    )
}
