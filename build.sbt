val scala2_13 = "2.13.16"
val scala3    = "3.3.4"

ThisBuild / scalaVersion       := scala2_13
ThisBuild / crossScalaVersions := Seq(scala2_13, scala3)

val zioVersion          = "2.1.15"
val zioInteropRSVersion = "2.0.2"

val mongoVersion = "5.3.1"
val rsVersion    = "1.0.4"

val flapdoodleVersion = "4.18.1"
val immutablesVersion = "2.10.1"

val magnolia2Version = "1.1.10"

Global / onChangedBuildSource := ReloadOnSourceChanges

autoCompilerPlugins := true

ThisBuild / Test / parallelExecution := false
ThisBuild / fork                     := true
ThisBuild / Test / fork              := true

inThisBuild(
  List(
    organization           := "io.github.zeal18",
    homepage               := Some(url("https://github.com/zeal18/zio-mongodb")),
    licenses               := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
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
          "-Yretain-trees", // to enable default values for codec derivation
          "-Xmax-inlines:64",
        )
      case _ =>
        Seq(
          "-Xsource:3-cross",
          "-Ymacro-annotations",
          if (insideCI.value) "-Wconf:any:error"
          else "-Wconf:any:warning",
        )
    }),
    Compile / doc / scalacOptions -= "-Wconf:any:error",
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq()
      case _ =>
        Seq(
          compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
        )
    }),
  )

lazy val root =
  (project in file(".")).aggregate(bson, driver, testkit, driverItTests).settings(publish / skip := true)

lazy val bson = (project in file("bson")).settings(
  name := "zio-mongodb-bson",
  commonSettings,
  libraryDependencies ++= Seq(
    "org.mongodb" % "bson"         % mongoVersion,
    "dev.zio"    %% "zio-test"     % zioVersion % Test,
    "dev.zio"    %% "zio-test-sbt" % zioVersion % Test,
  ),
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Seq.empty
    case _ =>
      Seq(
        "com.softwaremill.magnolia1_2" %% "magnolia"      % magnolia2Version,
        "org.scala-lang"                % "scala-reflect" % scalaVersion.value % Provided, // required by magnolia
      )
  }),
)

lazy val driver: Project = (project in file("driver"))
  .settings(
    name := "zio-mongodb-driver",
    commonSettings,
    // workaround for
    // [error] While parsing annotations in .coursier/https/repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.2.1/mongodb-driver-core-4.2.1.jar(com/mongodb/lang/Nullable.class), could not find MAYBE in enum <none>.
    // [error] This is likely due to an implementation restriction: an annotation argument cannot refer to a member of the annotated class (scala/bug#7014).
    scalacOptions += "-Wconf:msg=While parsing annotations in:silent",
    libraryDependencies ++= Seq(
      "dev.zio"            %% "zio"                            % zioVersion,
      "dev.zio"            %% "zio-interop-reactivestreams"    % zioInteropRSVersion,
      "org.mongodb"         % "mongodb-driver-reactivestreams" % mongoVersion,
      "org.reactivestreams" % "reactive-streams-tck"           % rsVersion  % Test,
      "dev.zio"            %% "zio-test"                       % zioVersion % Test,
      "dev.zio"            %% "zio-test-sbt"                   % zioVersion % Test,
    ),
  )
  .dependsOn(bson)

lazy val testkit = (project in file("testkit"))
  .settings(
    name := "zio-mongodb-testkit",
    commonSettings,
    libraryDependencies ++= Seq(
      "dev.zio"            %% "zio"                       % zioVersion,
      "dev.zio"            %% "zio-test"                  % zioVersion,
      "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % flapdoodleVersion,
    ),
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        List(
          // required by flapdoodle
          "org.immutables" % "builder" % immutablesVersion,
          "org.immutables" % "value"   % immutablesVersion,
        )
      case _ => List.empty
    }),
  )
  .dependsOn(driver)

// as a separate project to avoid circular dependencies
lazy val driverItTests = (project in file("driver-it-tests"))
  .settings(
    commonSettings,
    name           := "zio-mongodb-driver-it-tests",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    ),
  )
  .dependsOn(driver)
  .dependsOn(testkit)
