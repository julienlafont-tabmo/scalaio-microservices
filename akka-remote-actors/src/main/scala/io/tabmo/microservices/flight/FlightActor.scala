package io.tabmo.microservices.flight

import akka.actor.{Actor, ActorLogging, Props}
import io.tabmo.microservices.flight.FlightActor._

import cats.data.Xor
import io.circe.Json
import io.circe.syntax._

class FlightActor extends Actor with ActorLogging {

  val flightsByUser = Map(
    "pierre" -> Seq("05:00-09:00", "08:00-15:00", "13:00-17:00"),
    "sarah" -> Seq("11:00-13:00", "12:00-14:00", "18:00-20:00")
  )

  override def receive: Receive = {
    case ListFlights(sub) => {
      val response = flightsByUser.get(sub) match {
        case Some(flights) => Xor.right(Json.fromValues(flights.map(_.asJson)))
        case None => Xor.left("No flight found")
      }

      sender() ! ListFlightsResponse(response)
    }
  }
}

object FlightActor {
  def props() = Props(new FlightActor())

  case class ListFlights(subject: String)
  case class ListFlightsResponse(response: Xor[String, Json])
}
