package io.tabmo.auth

import org.patricknoir.kafka.reactive.server.ReactiveRoute
import org.patricknoir.kafka.reactive.server.dsl._
import org.patricknoir.kafka.reactive.server.streams._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import cats.data.Xor
import io.circe.Json

object Boot extends App {

  implicit val system = ActorSystem("AuthService")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val warmup = request.sync[String, String]("warmup") { _ =>
    "I'm ready!"
  }

  val getIdentity = request.aSync[Json, String]("get-identity") { token =>
    val accreditedUsers = Seq("pierre", "sarah")

    token.cursor.get[String]("sub").fold(
      decodingFailure => Xor.left(new Error("Invalid token")),
      {
        case user if accreditedUsers.contains(user) => Xor.right(user)
        case user => Xor.left(new Error(s"Not accredited user $user"))
      }
    )
  }

  val route: ReactiveRoute =  warmup ~ getIdentity

  val kafkaServers = Set("localhost:9092")
  val source = ReactiveKafkaSource.create("auth", kafkaServers, "authService")
  val sink = ReactiveKafkaSink.create(kafkaServers)

  val reactiveSystem = source ~> route ~> sink

  reactiveSystem.run()
  println("AUTH reactive system is running")

}
