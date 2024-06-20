package io.github.zeal18.zio.mongodb.driver

import de.flapdoodle.embed.mongo.distribution.Version
import io.github.zeal18.zio.mongodb.bson.ObjectId
import io.github.zeal18.zio.mongodb.bson.annotations.BsonId
import io.github.zeal18.zio.mongodb.bson.codecs.Codec
import io.github.zeal18.zio.mongodb.testkit.EmbeddedMongo
import zio.Console.*
import zio.*

object Main extends ZIOAppDefault {
  val client: ZLayer[Any, Throwable, MongoClient] =
    EmbeddedMongo.live(Version.V7_0_2).flatMap { embeddedProcess =>
      val serverAddress = embeddedProcess.get.current().getServerAddress()
      val host          = serverAddress.getHost()
      val port          = serverAddress.getPort()

      MongoClient.live(s"mongodb://$host:$port/?retryWrites=true&w=majority")
    }

  val database: ZLayer[Any, Throwable, MongoDatabase] =
    client >>> ZLayer.fromZIO(ZIO.serviceWith[MongoClient](_.getDatabase("my-database")))

  // "BsonId" annotation is needed to write the "id" field as "_id"
  // otherwise there will be two "_id" and "id" fields in the document
  case class User(@BsonId id: ObjectId, name: String, email: String)
  object User {
    implicit val codec: Codec[User] = Codec.derived[User]
  }

  override def run = {
    val user = User(new ObjectId(), "John Doe", "john.doe@example.com")

    for {
      users <- ZIO.serviceWith[MongoDatabase](_.getCollection[User]("users"))

      insertResult <- users.insertOne(user)
      _            <- printLine(insertResult)

      foundUser <- users.find(filters.eq(user.id)).runHead

      _ <- foundUser match {
        case None        => printError("User not found!")
        case Some(found) => printLine(s"Found user: $found")
      }

    } yield 0
  }.provide(database)
}
