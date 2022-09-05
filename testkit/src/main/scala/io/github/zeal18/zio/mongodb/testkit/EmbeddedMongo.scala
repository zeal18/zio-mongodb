package io.github.zeal18.zio.mongodb.testkit

import de.flapdoodle.embed.mongo.*
import de.flapdoodle.embed.mongo.config.Defaults
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.*
import de.flapdoodle.embed.mongo.packageresolver.Command
import de.flapdoodle.embed.process.config.RuntimeConfig
import de.flapdoodle.embed.process.config.process.ProcessOutput
import de.flapdoodle.embed.process.io.directories.Directory
import de.flapdoodle.embed.process.io.directories.FixedPath
import de.flapdoodle.embed.process.runtime.Network
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.Live

object EmbeddedMongo {

  private val artifactStorePath: Task[FixedPath] =
    for {
      system    <- ZIO.system
      gitlabDir <- system.env("CI_PROJECT_DIR")
      githubDir <- system.env("GITHUB_WORKSPACE")
      homeDir   <- system.property("user.home")
      tmpDir    <- system.property("java.io.tmpdir")

      baseDir = gitlabDir orElse githubDir orElse homeDir orElse tmpDir

      path <- baseDir.fold[Task[FixedPath]](
        ZIO.fail(new Throwable("Could not resolve base directory")),
      )(p => ZIO.succeed(new FixedPath(s"$p/.embedmongo")))
    } yield path

  private def runtimeConfig(artifactStorePath: Directory): Task[RuntimeConfig] = {
    val command = Command.MongoD

    ZIO.attempt(
      RuntimeConfig.builder
        .processOutput(ProcessOutput.silent())
        .artifactStore(
          Defaults
            .extractedArtifactStoreFor(command)
            .withDownloadConfig(
              Defaults.downloadConfigFor(command).artifactStorePath(artifactStorePath).build(),
            ),
        )
        .build(),
    )
  }

  private def mongodConfig(version: IFeatureAwareVersion): Task[MongodConfig] = ZIO.attempt(
    MongodConfig
      .builder()
      .version(version)
      // make sure to only bind to localhost and use some free port
      .net(
        new Net(
          "localhost",
          Network.freeServerPort(Network.getLocalHost()),
          Network.localhostIsIPv6(),
        ),
      )
      .build(),
  )

  def live(
    version: IFeatureAwareVersion = Version.Main.PRODUCTION,
  ): ZLayer[Any, Throwable, MongodProcess] =
    ZLayer.scoped(ZIO.acquireRelease((for {
      asp           <- Live.live(artifactStorePath)
      runtimeConfig <- runtimeConfig(asp)
      mongodConfig  <- mongodConfig(version)

      mongodProcess <- ZIO.attemptBlocking(
        MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig).start(),
      )
    } yield mongodProcess))(p => ZIO.succeed(p.stop())))
}
