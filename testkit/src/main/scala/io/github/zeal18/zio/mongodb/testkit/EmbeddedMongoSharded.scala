package io.github.zeal18.zio.mongodb.testkit

import de.flapdoodle.embed.mongo.distribution.*
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.io.ProcessOutput
import de.flapdoodle.embed.process.io.directories.PersistentDir
import de.flapdoodle.reverse.TransitionWalker.ReachedState
import de.flapdoodle.reverse.transitions.Start
import zio.ZIO
import zio.ZLayer
import zio.test.Live
import de.flapdoodle.embed.mongo.commands.MongodArguments
import de.flapdoodle.embed.mongo.config.Storage
import de.flapdoodle.embed.mongo.transitions.Mongos
import de.flapdoodle.embed.mongo.commands.MongosArguments
import de.flapdoodle.embed.mongo.transitions.RunningMongosProcess
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.reverse.StateID
import de.flapdoodle.embed.process.distribution.Version as ProcessVersion
import io.github.zeal18.zio.mongodb.driver.MongoClient
import de.flapdoodle.embed.mongo.commands.ServerAddress
import io.github.zeal18.zio.mongodb.bson.conversions.Bson
import io.github.zeal18.zio.mongodb.bson.BsonDocument
import io.github.zeal18.zio.mongodb.bson.BsonDouble
import zio.Scope

object EmbeddedMongoSharded {

  def live(
    version: IFeatureAwareVersion = Version.Main.V6_0,
  ): ZLayer[Any, Throwable, ReachedState[RunningMongosProcess]] =
    ZLayer.scoped(for {
      dir <- Live.live(EmbeddedMongo.persistentDir)

      configServerReplicaSetName = "ConfigServerSet"
      shardReplicaSetName        = "ShardSet"

      configServerArguments = MongodArguments
        .defaults()
        .withIsConfigServer(true)
        .withReplication(Storage.of(configServerReplicaSetName, 0))

      shardServerArguments = MongodArguments
        .defaults()
        .withIsShardServer(true)
        .withUseNoJournal(false)
        .withReplication(Storage.of(shardReplicaSetName, 0))

      configServerOne <- startMongod(version, configServerArguments, dir)
      configServerTwo <- startMongod(version, configServerArguments, dir)

      configServerOneAdress = configServerOne.current().getServerAddress()
      configServerTwoAdress = configServerTwo.current().getServerAddress()

      _ <- rsInitiate(
        configServerReplicaSetName,
        true,
        configServerOneAdress,
        configServerTwoAdress,
      )

      shardServerOne <- startMongod(version, shardServerArguments, dir)
      shardServerTwo <- startMongod(version, shardServerArguments, dir)

      _ <- rsInitiate(
        shardReplicaSetName,
        false,
        shardServerOne.current().getServerAddress(),
        shardServerTwo.current().getServerAddress(),
      )

      mongosConfigClusterArguments = MongosArguments
        .defaults()
        .withReplicaSet(configServerReplicaSetName)
        .withConfigDB(
          List(configServerOneAdress, configServerTwoAdress).map(_.toString).mkString(","),
        )

      mongosConfigClusterServer <- startMongos(version, mongosConfigClusterArguments, dir)

      _ <- runCommandInAdminDB(
        mongosConfigClusterServer.current().getServerAddress(),
        BsonDocument(
          "addShard" ->
            s"$shardReplicaSetName/${shardServerOne.current().getServerAddress()},${shardServerTwo.current().getServerAddress()}",
        ),
      )
    } yield mongosConfigClusterServer)

  private def startMongod(
    version: ProcessVersion,
    config: MongodArguments,
    persistentDir: PersistentDir,
  ): ZIO[Scope, Throwable, ReachedState[RunningMongodProcess]] =
    ZIO
      .attempt(
        Mongod
          .instance()
          .transitions(version)
          .replace(Start.to(classOf[MongodArguments]).initializedWith(config))
          .replace(Start.to(classOf[ProcessOutput]).initializedWith(ProcessOutput.silent()))
          .replace(Start.to(classOf[PersistentDir]).initializedWith(persistentDir))
          .walker()
          .initState(StateID.of(classOf[RunningMongodProcess])),
      )
      .withFinalizer(p => ZIO.succeed(p.close()))

  private def startMongos(
    version: ProcessVersion,
    config: MongosArguments,
    persistentDir: PersistentDir,
  ): ZIO[Scope, Throwable, ReachedState[RunningMongosProcess]] =
    ZIO
      .attempt(
        Mongos
          .instance()
          .transitions(version)
          .replace(Start.to(classOf[MongosArguments]).initializedWith(config))
          .replace(Start.to(classOf[ProcessOutput]).initializedWith(ProcessOutput.silent()))
          .replace(Start.to(classOf[PersistentDir]).initializedWith(persistentDir))
          .walker()
          .initState(StateID.of(classOf[RunningMongosProcess])),
      )
      .withFinalizer(p => ZIO.succeed(p.close()))

  private def rsInitiate(
    replicaSetName: String,
    configServer: Boolean,
    one: ServerAddress,
    others: ServerAddress*,
  ): ZIO[Any, Throwable, Unit] = {
    val members = (one +: others).zipWithIndex.map { case (server, idx) =>
      BsonDocument("_id" -> idx, "host" -> server.toString)
    }
    val command = BsonDocument("replSetInitiate" -> {
      if (configServer)
        BsonDocument("_id"    -> replicaSetName, "configsvr" -> true, "members" -> members)
      else BsonDocument("_id" -> replicaSetName, "members"   -> members)
    })

    runCommandInAdminDB(one, command)
  }

  private def runCommandInAdminDB(
    server: ServerAddress,
    command: Bson,
  ): ZIO[Any, Throwable, Unit] = {
    val connectionString = s"mongodb://${server.getHost()}:${server.getPort()}"
    ZIO.scoped[Any] {
      for {
        client <- MongoClient.live(connectionString).build.map(_.get)
        db = client.getDatabase("admin")
        isSuccess <- db
          .runCommand(command)
          .map(_.flatMap(_.get[BsonDouble]("ok")).exists(_.doubleValue() >= 1.0))
        _ <- ZIO.fail(new IllegalStateException("Faild to run command")).when(!isSuccess)
      } yield ()
    }
  }
}
