package io.tabmo.flight

import org.patricknoir.kafka.reactive.server.ReactiveRoute
import org.patricknoir.kafka.reactive.server.dsl._
import org.patricknoir.kafka.reactive.server.streams.{ReactiveKafkaSink, ReactiveKafkaSource}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import cats.data.Xor
import io.circe.Json
import io.circe.syntax._

object Boot extends App {

  implicit val system = ActorSystem("FlightService")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val warmup = request.sync[String, String]("warmup") { _ =>
    "I'm ready!"
  }

  val listFlights = request.aSync[String, Json]("list") { subject =>
    val flightsByUser = Map(
      "pierre" -> Seq("05:00-09:00", "08:00-15:00", "13:00-17:00"),
      "sarah" -> Seq("11:00-13:00", "12:00-14:00", "18:00-20:00")
    )

    flightsByUser.get(subject) match {
      case Some(flights) => Xor.right(Json.fromValues(flights.map(_.asJson)))
      case None => Xor.left(new Error("No hotel found"))
    }
  }

  val route: ReactiveRoute =  warmup ~ listFlights

  val kafkaServers = Set("localhost:9092")
  val source = ReactiveKafkaSource.create("flight", kafkaServers, "flightService")
  val sink = ReactiveKafkaSink.create(kafkaServers)

  val reactiveSystem = source ~> route ~> sink

  reactiveSystem.run()
  println("FLIGHT reactive system is running")

}
