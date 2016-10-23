package io.tabmo.microservices.hotel

import akka.actor.{Actor, ActorLogging, Props}
import io.tabmo.microservices.hotel.HotelActor._

import cats.data.Xor
import io.circe.Json
import io.circe.syntax._

class HotelActor extends Actor with ActorLogging {

  val accomodationsByUser = Map(
    "pierre" -> Seq("Hotel 1", "Hotel 2", "Hotel 3"),
    "sarah" -> Seq("Hotel 3", "Hotel 4", "Hotel 5")
  )

  override def receive: Receive = {
    case ListHotels(sub) => {
      val response = accomodationsByUser.get(sub) match {
        case Some(flights) => Xor.right(Json.fromValues(flights.map(_.asJson)))
        case None => Xor.left("No hotel found")
      }

      sender() ! ListHotelsResponse(response)
    }
  }
}

object HotelActor {
  def props() = Props(new HotelActor())

  case class ListHotels(subject: String)
  case class ListHotelsResponse(response: Xor[String, Json])
}
