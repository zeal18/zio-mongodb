package io.github.zeal18.zio.mongodb.bson.codecs

import EasierValDef.*
import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.bson.annotations.BsonIgnore
import io.github.zeal18.zio.mongodb.bson.annotations.BsonProperty
import io.github.zeal18.zio.mongodb.bson.codecs.error.BsonError
import org.bson.BsonInvalidOperationException
import org.bson.BsonReader
import org.bson.BsonSerializationException
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

import scala.annotation.tailrec
import scala.compiletime
import scala.deriving.*
import scala.deriving.Mirror
import scala.quoted.*

object Macro:
  inline def delayedSummonInline[A] = scala.compiletime.summonInline[A]

  def derived[A: Type](logCode: Boolean)(using Quotes): Expr[Codec[A]] =
    if isCaseObject[A] then deriveCaseObject(logCode)
    else if isValueClass[A] then ???
    else if isProduct[A] || isCoproduct[A] then derivedADT(logCode)
    else '{ compiletime.error("Derivation of selected type is not supported") }

  private def derivedADT[A: Type](logCode: Boolean)(using Quotes): Expr[Codec[A]] =
    Preparations(logCode) { preparations =>
      derivedADTWithPreparations(logCode, preparations)
    }

  private def derivedADTWithPreparations[A: Type](logCode: Boolean, preparations: Preparations)(using
    Quotes,
  ): Prepared[A] =
    Expr.summonOrError[Mirror.Of[A]] match
      case '{ $m: Mirror.ProductOf[A] } =>
        deriveProduct(logCode, preparations, m)
      case '{ $m: Mirror.SumOf[A] } =>
        deriveCoproduct(logCode, preparations, m)

  private def isCaseObject[A: Type](using q: Quotes): Boolean =
    import q.reflect.*

    TypeRepr.of[A].typeSymbol.flags.is(Flags.Module)

  private def isValueClass[A: Type](using Quotes): Boolean = false
  private def isProduct[A: Type](using q: Quotes): Boolean =
    import q.reflect.*

    TypeRepr.of[A].typeSymbol.flags.is(Flags.Case)

  private def isCoproduct[A: Type](using q: Quotes): Boolean =
    import q.reflect.*

    val flags = TypeRepr.of[A].typeSymbol.flags

    flags.is(Flags.Sealed) || flags.is(Flags.Enum)

  private def deriveProduct[A: Type](
    logCode: Boolean,
    preparations: Preparations,
    mirror: Expr[Mirror.ProductOf[A]],
  )(using q: Quotes): Prepared[A] =
    preparations.addDeferred[A] {
      import q.reflect.*

      val caseClassTypeInfo = TypeInfo[A]

      if logCode then println(s"Deriving product: ${Type.show[A]}")

      val caseClass                   = TypeRepr.of[A]
      val caseClassName: Expr[String] = Expr(caseClassTypeInfo.short)

      val fields      = TypeRepr.of[A].typeSymbol.caseFields
      val defaults    = getDefaultValues[A]
      val annotations = fields
        .map { f =>
          f.name -> f.annotations.map(_.asExpr)
        }
        .filter(_._2.nonEmpty)
        .toMap

      val labels: Map[String, String] = fields
        .map { f =>
          val newName =
            annotations.getOrElse(f.name, Nil).collectFirst {
              case '{ new BsonId() }             => "_id"
              case '{ new BsonProperty($label) } => label.valueOrAbort
            }

          f.name -> newName
        }
        .collect { case (k, Some(v)) => k -> v }
        .toMap

      val ignored: Set[String] = fields
        .map { f =>
          annotations.getOrElse(f.name, Nil).collectFirst { case '{ new BsonIgnore() } =>
            f.name
          }
        }
        .collect { case Some(value) => value }
        .toSet

      val decode =
        if caseClass.typeArgs.isEmpty then deriveCaseClassDecode[A](defaults, labels, ignored)
        else deriveCaseClassDecodeWithTypeParams[A](mirror, defaults, labels, ignored)

      val filedsToProcess = fields.filterNot(f => ignored.contains(f.name))

      val fieldTypes = filedsToProcess.map(t => caseClass.memberType(t))

      val preparedFields = Expr.ofList(filedsToProcess.map { f =>
        val tpe = caseClass.memberType(f)
        tpe.asType match {
          case '[t] =>
            val paramTypeInfo = TypeInfo[t]
            val codecExpr     = '{ () => ${ preparations.getStablisedImplicitInstance[t] } }

            val derefDefSymbol = Symbol.newMethod(
              Symbol.spliceOwner,
              "_",
              MethodType(List("source"))(
                _ => List(caseClass),
                _ => tpe,
              ),
            )

            val functionDef = DefDef(
              derefDefSymbol,
              {
                case (src :: Nil) :: Nil =>
                  Some(Select(src.asExprOf[A].asTerm, f).changeOwner(derefDefSymbol))
                case _ => None
              },
            )

            val deref =
              Block(List(functionDef), Closure(Ref(derefDefSymbol), None)).asExprOf[Function1[A, t]]
            val label: Expr[String] = Expr(labels.getOrElse(f.name, f.name))

            '{ CaseClass.Field($label, $codecExpr, $deref) }
        }
      })

      val caseClassExpr     = '{ CaseClass[A]($caseClassName, $decode, $preparedFields) }
      val caseClassFullName = Expr(caseClassTypeInfo.full)

      '{ CaseClassCodec[A]($caseClassFullName, FlatCaseClassCodec[A]($caseClassExpr)) }
    }
  end deriveProduct

  /** this is a hack for instantiating classes with a type parameter
    *
    * for example
    * ```scala
    * case class Leaf[A](a: A) derives Codec
    * ```
    *
    * would generate the following constructor
    * ```scala
    * new Leaf[A](a = ???)
    * ```
    *
    * which fails with `constructor Leaf in class Leaf does not take parameters`.
    * Possible solutions are to find a way to delete the type parameter from the constructor
    * or to use Mirror for instantiation. This hack does the second way.
    */
  private def deriveCaseClassDecodeWithTypeParams[A: Type](
    mirror: Expr[Mirror.ProductOf[A]],
    defaults: Map[String, Expr[Any]],
    labels: Map[String, String],
    ignored: Set[String],
  )(using q: Quotes): Expr[Map[String, Any] => A] =
    import q.reflect.*

    val caseClass         = TypeRepr.of[A]
    val caseClassTypeInfo = TypeInfo[A]
    val fields            = Fields.fromMirror(mirror)

    def fieldsToA(data: Expr[Map[String, Any]]): Expr[A] =
      val elems = fields.map { f =>
        import f.{Type as F, typeInstance}

        val (name, notFound) = labels.get(f.name) match
          case None        => (Expr(f.name), Expr(s"Missing field: '${f.name}'"))
          case Some(label) =>
            (Expr(label), Expr(s"Missing field: '$label' (renamed from '${f.name}')"))

        if ignored.contains(f.name) then
          defaults.get(f.name) match
            case None =>
              report.errorAndAbort(
                s"Field '${caseClassTypeInfo.full}#${f.name}' is ignored but doesn't have a default value",
              )
            case Some(default) => default.asExprOf[F]
        else
          defaults.get(f.name) match
            case None =>
              '{
                $data.getOrElse($name, throw new org.bson.BsonSerializationException($notFound)).asInstanceOf[F]
              }
            case Some(default) => '{ $data.getOrElse($name, $default).asInstanceOf[F] }
      }

      '{ $mirror.fromProduct(Tuple.fromArray(Array(${ Varargs(elems) }*))) }

    '{ map => ${ fieldsToA('map) } }

  private def deriveCaseClassDecode[A: Type](
    defaults: Map[String, Expr[Any]],
    labels: Map[String, String],
    ignored: Set[String],
  )(using q: Quotes): Expr[Map[String, Any] => A] =
    import q.reflect.*

    val caseClass         = TypeRepr.of[A]
    val caseClassTypeInfo = TypeInfo[A]

    val fields = TypeRepr.of[A].typeSymbol.caseFields

    def fieldToArg(data: Expr[Map[String, Any]], field: Symbol): NamedArg =
      val (name, notFound) = labels.get(field.name) match
        case None        => (Expr(field.name), Expr(s"Missing field: '${field.name}'"))
        case Some(label) =>
          (Expr(label), Expr(s"Missing field: '$label' (renamed from '${field.name}')"))

      val tpe = caseClass.memberType(field)

      tpe.asType match
        case '[t] =>
          val valueExpr =
            if ignored.contains(field.name) then
              defaults.get(field.name) match
                case None =>
                  report.errorAndAbort(
                    s"Field '${caseClassTypeInfo.full}#${field.name}' is ignored but doesn't have a default value",
                  )
                case Some(default) => default.asExprOf[t]
            else
              defaults.get(field.name) match
                case None =>
                  '{
                    $data.getOrElse($name, throw new org.bson.BsonSerializationException($notFound)).asInstanceOf[t]
                  }
                case Some(default) => '{ $data.getOrElse($name, $default).asInstanceOf[t] }

          val argTerm =
            if isRepeated(tpe) then
              // need to change type from 'Seq[Int]' to 'Int*' for a varargs argument
              val subtype = TypeRepr.of[t].typeArgs.head
              subtype.asType match
                case '[st] =>
                  val repeatedAnyTypeTree =
                    Applied(TypeIdent(defn.RepeatedParamClass), List(TypeTree.of[st]))
                  Typed(valueExpr.asTerm, repeatedAnyTypeTree)
            else valueExpr.asTerm

          NamedArg(field.name, argTerm)

    '{ map =>
      ${
        val fieldsList = fields.map(fieldToArg('map, _))
        New(Inferred(TypeRepr.of[A]))
          .select(caseClass.typeSymbol.primaryConstructor)
          .appliedToArgs(fieldsList)
          .asExprOf[A]
      }
    }

  private def deriveCoproduct[A: Type](
    logCode: Boolean,
    preparations: Preparations,
    mirror: Expr[Mirror.SumOf[A]],
  )(using q: Quotes): Prepared[A] =
    import q.reflect.*

    val children = TypeRepr.of[A].typeSymbol.children
    val isEnum   = children.forall(_.flags.is(Flags.Module)) ||
      (TypeRepr.of[A].typeSymbol.flags.is(Flags.Enum) && children.forall(_.caseFields.isEmpty))

    if isEnum then Prepared(deriveEnum(logCode))
    else deriveMixedCoproduct(logCode, preparations, mirror)

  private def deriveEnum[A: Type](logCode: Boolean)(using q: Quotes): Expr[Codec[A]] =
    import q.reflect.*

    val enumTypeInfo = TypeInfo[A]

    if logCode then println(s"Deriving enum type: ${enumTypeInfo.full}")

    val enumSymbol = TypeRepr.of[A].typeSymbol
    val children   = TypeRepr.of[A].typeSymbol.children

    def childToName(child: Symbol): CaseDef =
      CaseDef(Ident(child.termRef), None, Block(Nil, Literal(StringConstant(child.name))))

    def nameToChild(child: Symbol): CaseDef =
      val someChild = '{ Some(${ Ident(child.termRef).asExprOf[A] }) }
      CaseDef(Literal(StringConstant(child.name)), None, Block(Nil, someChild.asTerm))

    val nameByValue: Expr[A => String] = '{ (a: A) =>
      ${
        val cases = children.map(childToName)
        Match('a.asTerm, cases).asExprOf[String]
      }
    }
    val valueByName: Expr[String => Option[A]] = '{ (s: String) =>
      ${
        val cases = children.map(nameToChild) :+
          CaseDef(Wildcard(), None, '{ None }.asTerm)

        Match('s.asTerm, cases).asExprOf[Option[A]]
      }
    }

    val result = '{ EnumCodec[A](${ Expr(enumTypeInfo.full) }, $nameByValue, $valueByName) }

    if logCode then printResult(result)

    result
  end deriveEnum

  private def deriveMixedCoproduct[A: Type](
    logCode: Boolean,
    preparations: Preparations,
    mirror: Expr[Mirror.SumOf[A]],
  )(using q: Quotes): Prepared[A] =
    import q.reflect.*

    val enumTypeInfo = TypeInfo[A]

    if logCode then println(s"Deriving coproduct type: ${Type.show[A]}")

    val fields = flattenCoproductSubtypes(mirror).zipWithIndex
    if logCode then println(s"Flattened coproduct subtypes are: ${fields.map(_._1.name).mkString(", ")}")

    fields.map(_._1.name).groupBy(identity).filter(_._2.size > 1).toList match
      case Nil       => ()
      case ambiguous =>
        report.errorAndAbort(
          s"Error deriving '${enumTypeInfo.full}': Ambiguous subtypes: '${ambiguous.map(_._1).mkString("', '")}'\n" +
            "Make sure all sealed subtypes and their sealed subtypes have unique names",
        )

    val nonRecursiveCases = Array.fill[Option[Expr[Codec[?]]]](fields.size)(None)

    def fieldSymbol(field: Field): Symbol =
      val t = field.typeRepr
      if t.termSymbol.flags.is(q.reflect.Flags.Case) then t.termSymbol
      else t.typeSymbol

    fields.foreach { case (field, idx) =>
      import field.{Type as F, typeInstance}

      val symbol = fieldSymbol(field)

      if symbol.flags.is(Flags.Module) || symbol.flags.is(Flags.StableRealizable) then
        if logCode then println(s"Deriving object child: ${Type.show[F]}")

        val obj = Ident(symbol.termRef).asExprOf[F]

        val codec = '{ ConstCodec($obj) }
        nonRecursiveCases(idx) = Some(codec)
      else if symbol.isClassDef then
        if logCode then println(s"Deriving class child: ${Type.show[F]}")

        Expr.summon[Codec[F]] match
          case Some(rf) =>
            if logCode then println(s"Found implicit codec for ${Type.show[F]}")
            val codec = preparations.stabliseInstance(Expr.summonLater[Codec[F]])
            nonRecursiveCases(idx) = Some(codec)
            codec
          case None =>
            if logCode then println(s"Missing implicit codec, deriving codec for ${Type.show[F]}")
            derivedADTWithPreparations[F](logCode, preparations)
      else
        report.errorAndAbort(
          s"Unsupported coproduct element: '${field.name}' with type '${Type.show[F]}'",
        )
    }

    preparations.addDeferred[A] {
      val childrenCodecs: Map[String, Expr[Codec[?]]] =
        fields.map { case (field, idx) =>
          import field.{Type as F, typeInstance}

          val symbol = fieldSymbol(field)

          val codec: Expr[Codec[?]] =
            val c = nonRecursiveCases(idx).getOrElse(preparations.need[F].varRef)
            '{
              if $c.isInstanceOf[CaseClassCodec[F]] then $c.asInstanceOf[CaseClassCodec[F]].flat
              else $c
            }

          field.name -> codec
        }.toMap

      def childToName(field: Field): CaseDef =
        childrenCodecs.get(field.name) match
          case None        => throw new Exception("")
          case Some(codec) =>
            val tuple  = '{ (${ Expr(field.name) }, $codec) }
            val symbol = fieldSymbol(field)
            if symbol.isClassDef then
              CaseDef(Typed(Wildcard(), TypeTree.of[field.Type]), None, Block(Nil, tuple.asTerm))
            else CaseDef(Ident(symbol.termRef), None, Block(Nil, tuple.asTerm))

      def nameToChild(field: Field): CaseDef =
        childrenCodecs.get(field.name) match
          case None        => throw new Exception("")
          case Some(codec) =>
            val someChild = '{ Some($codec.asInstanceOf[Codec[A]]) }
            CaseDef(Literal(StringConstant(field.name)), None, Block(Nil, someChild.asTerm))

      val codecByValue: Expr[A => (String, Codec[?])] = '{ (a: A) =>
        ${
          val cases = fields.map { case (f, _) => childToName(f) }
          Match('a.asTerm, cases).asExprOf[(String, Codec[?])]
        }
      }
      val codecByName: Expr[String => Option[Codec[A]]] = '{ (s: String) =>
        ${
          val cases = fields.map { case (f, _) => nameToChild(f) } :+
            CaseDef(Wildcard(), None, '{ None }.asTerm)

          Match('s.asTerm, cases).asExprOf[Option[Codec[A]]]
        }
      }
      val discriminator = Expr("_t")

      val result = '{
        CoproductCodec[A](
          ${ Expr(enumTypeInfo.full) },
          $discriminator,
          $codecByValue,
          $codecByName,
        )
      }

      result
    }

  private def deriveCaseObject[A: Type](logCode: Boolean)(using q: Quotes): Expr[Codec[A]] =
    import q.reflect.*

    val objectTypeInfo = TypeInfo[A]

    if logCode then println(s"Deriving object: ${objectTypeInfo.full}")

    val obj = Ident(TypeRepr.of[A].typeSymbol.companionModule.termRef).asExprOf[A]

    val result = '{
      CaseObjectCodec(${ Expr(objectTypeInfo.short) }, ${ Expr(objectTypeInfo.full) }, $obj)
    }

    if logCode then printResult(result)

    result

  private[codecs] def printResult(result: Expr[?])(using q: Quotes): Unit =
    import q.reflect.*
    println(
      s"\nDerived ${result.asTerm.tpe.show}:\n${result.asTerm.show.replace("io.github.zeal18.zio.mongodb.bson.codecs.", "").replace(".apply(", "(").replace("scala.", "").replace("RecursiveDerivationTest.", "")}\n",
    )

  private def isRepeated(using q: Quotes)(typeRepr: q.reflect.TypeRepr): Boolean =
    typeRepr match
      case a: q.reflect.AnnotatedType =>
        a.annotation.tpe match
          case tr: q.reflect.TypeRef => tr.name == "Repeated"
          case _                     => false
      case _ => false

  private def getDefaultValues[T: Type](using Quotes): Map[String, Expr[Any]] =
    import quotes.reflect.*

    val tpe = TypeRepr.of[T].typeSymbol

    tpe.primaryConstructor.paramSymss.flatten
      .filter(_.isValDef)
      .zipWithIndex
      .flatMap { case (field, i) =>
        tpe.companionClass
          .declaredMethod(s"$$lessinit$$greater$$default$$${i + 1}")
          .headOption
          .map(method =>
            field.name -> Ident(tpe.companionModule.termRef)
              .select(method)
              .asExpr,
          )
      }
      .toMap

  private def flattenCoproductSubtypes[A: Type](mirror: Expr[Mirror.SumOf[A]])(using
    q: Quotes,
  ): List[Field] =
    val fields = Fields.fromMirror(mirror)
    fields.flatMap { f =>
      import f.{Type as F, typeInstance}

      Expr.summonOrError[Mirror.Of[F]] match
        case '{ $m: Mirror.ProductOf[F] } => List(f)
        case '{ $m: Mirror.SumOf[F] }     => flattenCoproductSubtypes[F](m)
    }

  extension (unused: Expr.type)
    /** Requires that macro be transparent.
      *
      * https://github.com/lampepfl/dotty/issues/12359
      */
    def summonLater[A: Type](using Quotes): Expr[A] =
      '{ Macro.delayedSummonInline[A] }

    def summonOrError[A](using Type[A])(using q: Quotes): Expr[A] =
      import quotes.reflect.*
      Implicits.search(TypeRepr.of[A]) match
        case iss: ImplicitSearchSuccess => iss.tree.asExpr.asInstanceOf[Expr[A]]
        case isf: ImplicitSearchFailure => q.reflect.report.errorAndAbort(isf.explanation)

  /** Used as a placeholder to make the type checker happy and be replaced by the real coded afterwards
    */
  private[codecs] class FakeCodec[A] extends Codec[A]:
    override def decode(reader: BsonReader, decoderContext: DecoderContext): A              = ???
    override def encode(writer: BsonWriter, value: A, encoderContext: EncoderContext): Unit = ???

end Macro
