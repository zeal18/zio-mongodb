package io.github.zeal18.zio.mongodb.bson.codecs

private[codecs] trait DerivedCodec {
  transparent inline def derived[A]: Codec[A] =
    ${ Macro.derived[A](logCode = false) }

  transparent inline def derivedDebug[A]: Codec[A] =
    ${ Macro.derived[A](logCode = true) }

  transparent inline given autoDerived[A]: Codec[A] = derived
}
