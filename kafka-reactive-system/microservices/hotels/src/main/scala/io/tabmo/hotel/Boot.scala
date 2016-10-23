package io.tabmo.hotel

import org.patricknoir.kafka.reactive.server.ReactiveRoute
import org.patricknoir.kafka.reactive.server.dsl._
import org.patricknoir.kafka.reactive.server.streams.{ReactiveKafkaSink, ReactiveKafkaSource}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import cats.data.Xor
import io.circe.Json
import io.circe.syntax._

object Boot extends App {

  implicit val system = ActorSystem("HotelService")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val warmup = request.sync[String, String]("warmup") { _ =>
    "I'm ready!"
  }

  val listHotels = request.aSync[String, Json]("list") { subject =>
    val accomodationsByUser = Map(
      "pierre" -> Seq("Hotel 1", "Hotel 2", "Hotel 3"),
      "sarah" -> Seq("Hotel 3", "Hotel 4", "Hotel 5")
    )

    accomodationsByUser.get(subject) match {
      case Some(hotels) => Xor.right(Json.fromValues(hotels.map(_.asJson)))
      case None => Xor.left(new Error("No hotel found"))
    }
  }

  val route: ReactiveRoute =  warmup ~ listHotels

  val kafkaServers = Set("localhost:9092")
  val source = ReactiveKafkaSource.create("hotel", kafkaServers, "hotelService")
  val sink = ReactiveKafkaSink.create(kafkaServers)

  val reactiveSystem = source ~> route ~> sink

  reactiveSystem.run()
  println("HOTEL reactive system is running")

}
