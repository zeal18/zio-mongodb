package io.github.zeal18.zio.mongodb.testkit

import de.flapdoodle.embed.mongo.distribution.*
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.io.ProcessOutput
import de.flapdoodle.embed.process.io.directories.PersistentDir
import de.flapdoodle.reverse.TransitionWalker.ReachedState
import de.flapdoodle.reverse.transitions.Start
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.Live

object EmbeddedMongo {
  private val persistentDir: Task[PersistentDir] =
    for {
      system    <- ZIO.system
      gitlabDir <- system.env("CI_PROJECT_DIR")
      githubDir <- system.env("GITHUB_WORKSPACE")
      homeDir   <- system.property("user.home")
      tmpDir    <- system.property("java.io.tmpdir")

      baseDir = gitlabDir orElse githubDir orElse homeDir orElse tmpDir

      path <- baseDir.fold[Task[PersistentDir]](
        ZIO.fail(new Throwable("Could not resolve base directory")),
      )(p => ZIO.attempt(PersistentDir.inWorkingDir(s"$p/.embedmongo").get))
    } yield path

  def live(
    version: IFeatureAwareVersion = Version.Main.V8_0,
  ): ZLayer[Any, Throwable, ReachedState[RunningMongodProcess]] =
    ZLayer.scoped(ZIO.acquireRelease(for {
      dir <- Live.live(persistentDir)

      mongodProcess <- ZIO.attemptBlocking {
        ImmutableMongod
          .builder()
          .processOutput(Start.to(classOf[ProcessOutput]).initializedWith(ProcessOutput.silent()))
          .persistentBaseDir(Start.to(classOf[PersistentDir]).initializedWith(dir))
          .build()
          .start(version)
      }
    } yield mongodProcess)(p => ZIO.succeed(p.close())))
}
