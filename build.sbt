import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

val scala2_13 = "2.13.8"
val scala3    = "3.1.2"

ThisBuild / scalaVersion := scala2_13
ThisBuild / crossScalaVersions ++= Seq(scala2_13, scala3)

val zioVersion          = "1.0.15"
val zioInteropRSVersion = "1.3.12"

val mongoVersion      = "4.6.1"
val flapdoodleVersion = "3.4.6"
val bsonJsr310Version = "3.5.4"

val scalatestVersion = "3.2.12"

Global / onChangedBuildSource := ReloadOnSourceChanges

autoCompilerPlugins := true

ThisBuild / parallelExecution        := false
ThisBuild / Test / parallelExecution := false
ThisBuild / fork                     := true
ThisBuild / Test / fork              := true

lazy val IntegrationTest = config("it") extend Test

ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / scalafixDependencies += "org.scala-lang"       %% "scala-rewrites"   % "0.1.3"

inThisBuild(
  List(
    organization := "io.github.zeal18",
    homepage     := Some(url("https://github.com/zeal18/zio-mongodb")),
    licenses     := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository     := "https://s01.oss.sonatype.org/service/local",
    developers := List(
      Developer(
        "zeal18",
        "Aleksei Lezhoev",
        "lezhoev@gmail.com",
        url("https://github.com/zeal18"),
      ),
    ),
  ),
)

val commonSettings =
  Seq(
    updateOptions := updateOptions.value.withCachedResolution(true),
    // enable in case we start using SNAPSHOT dependencies
    // updateOptions := updateOptions.value.withLatestSnapshots(false),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions -= "-Xfatal-warnings", // remove the flag added in sbt-tpolecat plugin
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq(
          "-source:3.0-migration",
        )
      case _ =>
        Seq(
          "-Xsource:3",
          "-Ymacro-annotations",
          if (insideCI.value) "-Wconf:any:error"
          else "-Wconf:any:warning",
        )
    }),
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq()
      case _ =>
        Seq(
          compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
          compilerPlugin(scalafixSemanticdb),
        )
    }),
  )

val integrationTestSettings =
  Defaults.itSettings ++ inConfig(IntegrationTest)(
    scalafmtConfigSettings ++ scalafixConfigSettings(IntegrationTest),
  )

lazy val root =
  (project in file(".")).aggregate(bson, driver).settings(publish / skip := true)

lazy val bson = (project in file("bson")).settings(
  name := "zio-mongodb-bson",
  commonSettings,
  libraryDependencies ++= Seq(
    "org.mongodb"          % "bson"                     % mongoVersion,
    "io.github.cbartosiak" % "bson-codecs-jsr310"       % bsonJsr310Version,
    "org.scalatest"       %% "scalatest"                % scalatestVersion % Test,
    "org.scalatest"       %% "scalatest-flatspec"       % scalatestVersion % Test,
    "org.scalatest"       %% "scalatest-shouldmatchers" % scalatestVersion % Test,
  ),
)

lazy val driver = (project in file("driver"))
  .configs(IntegrationTest)
  .settings(
    name := "zio-mongodb-driver",
    commonSettings,
    integrationTestSettings,
    // workaround for
    // [error] While parsing annotations in .coursier/https/repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.2.1/mongodb-driver-core-4.2.1.jar(com/mongodb/lang/Nullable.class), could not find MAYBE in enum <none>.
    // [error] This is likely due to an implementation restriction: an annotation argument cannot refer to a member of the annotated class (scala/bug#7014).
    scalacOptions += "-Wconf:msg=While parsing annotations in:silent",
    libraryDependencies ++= Seq(
      "dev.zio"    %% "zio"                            % zioVersion,
      "dev.zio"    %% "zio-interop-reactivestreams"    % zioInteropRSVersion,
      "org.mongodb" % "mongodb-driver-reactivestreams" % mongoVersion,
      "dev.zio"    %% "zio-test"                       % zioVersion % Test,
      "dev.zio"    %% "zio-test-sbt"                   % zioVersion % Test,
    ),
  )
  .dependsOn(bson)

addCommandAlias("fmt", "all scalafmtSbt; scalafmt; Test / scalafmt; scalafix; Test / scalafix")
addCommandAlias(
  "check",
  "all scalafmtSbtCheck; scalafmtCheck; Test / scalafmtCheck; scalafix --check; Test / scalafix --check",
)
