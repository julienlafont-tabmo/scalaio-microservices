package io.tabmo.microservices.api.commands

import scala.concurrent.duration._

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import io.tabmo.microservices.api.commands.GetTravel.Execute
import io.tabmo.microservices.api.models.RemoteSystems
import io.tabmo.microservices.auth.AuthActor
import io.tabmo.microservices.flight.FlightActor
import io.tabmo.microservices.hotel.HotelActor

import cats.data.Xor
import io.circe.Json

class GetTravel(requester: ActorRef, systems: RemoteSystems) extends Actor {

  override def receive = waitIdentityResponse()

  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(20000.millis) {
      self ! ReceiveTimeout
    }
  }

  // Step one - acquire subject from token
  def waitIdentityResponse(): Receive = {

    case Execute(token) =>
      systems.auth ! AuthActor.GetIdentity(token)

    case AuthActor.GetIdentityResponse(response) =>
      response match {
        case Xor.Left(error) => {
          respondError(error)
        }
        case Xor.Right(subject) =>
          systems.hotel ! HotelActor.ListHotels(subject)
          systems.flight ! FlightActor.ListFlights(subject)

          context.become(fetchTravelData(subject))
      }

    case ReceiveTimeout => respondError("timeout")

  }

  // Data to collect
  var hotels: Option[Json] = None
  var flights: Option[Json] = None

  // Step two - acquire flights and travels, and respond to requester
  def fetchTravelData(subject: String): Receive = {
    case FlightActor.ListFlightsResponse(Xor.Left(error)) =>
      respondError(error)

    case HotelActor.ListHotelsResponse(Xor.Left(error)) =>
      respondError(error)

    case FlightActor.ListFlightsResponse(Xor.Right(flightsJson)) =>
      flights = Some(flightsJson)
      collectResults()

    case HotelActor.ListHotelsResponse(Xor.Right(hotelsJson)) =>
      hotels = Some(hotelsJson)
      collectResults()

    case ReceiveTimeout => respondError("timeout")
  }

  def collectResults() = {
    (hotels zip flights).headOption.foreach { case (h, f) =>
      respondSuccess(h, f)
    }
  }

  def respondSuccess(hotels: Json, flights: Json) = {
    val msg = Xor.Right(Json.obj("hotels" -> hotels, "flights" -> flights))
    requester ! msg
    context.stop(self)
  }

  def respondError(error: String) = {
    val msg = Xor.Left(Json.obj("error" -> Json.fromString(error)))
    requester ! msg
    context.stop(self)
  }


}

object GetTravel {
  case class Execute(token: Json)

  def props(requester: ActorRef, systems: RemoteSystems) = {
    Props(new GetTravel(requester, systems))
  }
}
