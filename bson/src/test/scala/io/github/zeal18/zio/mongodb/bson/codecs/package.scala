package io.github.zeal18.zio.mongodb.bson

import org.bson.BsonDocument
import org.bson.BsonDocumentReader
import org.bson.BsonDocumentWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import zio.test.*

package object codecs {
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
}
