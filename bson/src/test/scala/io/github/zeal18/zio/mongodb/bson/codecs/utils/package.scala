package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import zio.test.*

import scala.util.Try

package object utils {
  private[codecs] def testCodecRoundtrip[A: Codec](
    title: String,
    value: A,
    expected: String,
  ) =
    test(title) {
      val codec = Codec[A]

      val writer = new BsonDocumentWriter(new BsonDocument)
      val encCtx = EncoderContext.builder().build()

      writer.writeStartDocument()
      writer.writeName("test-value")
      codec.encode(writer, value, encCtx)
      writer.writeEndDocument()

      val resultDoc = writer.getDocument()

      val reader = new BsonDocumentReader(resultDoc)
      val decCtx = DecoderContext.builder().build()

      reader.readStartDocument()
      reader.readName("test-value")
      val doc = codec.decode(reader, decCtx)
      reader.readEndDocument()

      assertTrue(doc == value, resultDoc.toString() == s"""{"test-value": $expected}""")
    }

  private[codecs] def testCodecDecode[A: Codec](
    title: String,
    bson: String,
    expected: A,
  ) =
    test(title) {
      val codec = Codec[A]

      val reader = new BsonDocumentReader(org.bson.BsonDocument.parse(bson))
      val decCtx = DecoderContext.builder().build()

      val doc = codec.decode(reader, decCtx)

      assertTrue(doc == expected)
    }

  private[codecs] def testCodecDecodeError[A: Codec](
    title: String,
    bson: String,
    assertion: BsonError => TestResult,
  ) =
    test(title) {
      val codec = Codec[A]

      val reader = new BsonDocumentReader(org.bson.BsonDocument.parse(bson))
      val decCtx = DecoderContext.builder().build()

      val result = Try(codec.decode(reader, decCtx))

      assertTrue(result.isFailure)
      &&
      result.fold[TestResult](
        {
          case e: BsonError.CodecError => assertion(e.underlying)
          case e =>
            TestResult(TestArrow.make((_: Any) => TestTrace.fail("Unexpected error: " + e)))
        },
        _ => TestResult(TestArrow.make((_: Any) => TestTrace.fail("Unexpected success"))),
      )
    }
}
