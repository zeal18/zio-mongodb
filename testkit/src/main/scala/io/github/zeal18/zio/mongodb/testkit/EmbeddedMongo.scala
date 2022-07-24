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
import zio.Has
import zio.RIO
import zio.Task
import zio.UIO
import zio.ZIO
import zio.ZLayer
import zio.blocking.Blocking
import zio.blocking.effectBlocking
import zio.system.System
import zio.test.environment.Live

object EmbeddedMongo {

  private val artifactStorePath: RIO[System, FixedPath] =
    for {
      system    <- ZIO.service[System.Service]
      gitlabDir <- system.env("CI_PROJECT_DIR")
      githubDir <- system.env("GITHUB_WORKSPACE")
      homeDir   <- system.property("user.home")
      tmpDir    <- system.property("java.io.tmpdir")

      baseDir = gitlabDir orElse githubDir orElse homeDir orElse tmpDir

      path <- baseDir.fold[Task[FixedPath]](
        Task.fail(new Throwable("Could not resolve base directory")),
      )(p => Task.succeed(new FixedPath(s"$p/.embedmongo")))
    } yield path

  private def runtimeConfig(artifactStorePath: Directory): Task[RuntimeConfig] = {
    val command = Command.MongoD

    Task(
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

  private def mongodConfig(version: IFeatureAwareVersion): Task[MongodConfig] = Task(
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
  ): ZLayer[Live, Throwable, Has[MongodProcess]] =
    ZLayer.fromManaged(
      (for {
        liveEnv       <- Live.live(ZIO.environment[System with Blocking])
        asp           <- artifactStorePath.provide(liveEnv)
        runtimeConfig <- runtimeConfig(asp)
        mongodConfig  <- mongodConfig(version)

        mongodProcess <- effectBlocking(
          MongodStarter.getInstance(runtimeConfig).prepare(mongodConfig).start(),
        ).provide(liveEnv)
      } yield mongodProcess).toManaged(p => UIO(p.stop())),
    )
}
