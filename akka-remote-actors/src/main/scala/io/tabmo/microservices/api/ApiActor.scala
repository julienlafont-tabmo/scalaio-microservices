package io.tabmo.microservices.api

import akka.actor.{Actor, Props, ReceiveTimeout, Terminated}
import io.tabmo.microservices.api.ApiActor.Travel
import io.tabmo.microservices.api.commands.GetTravel
import io.tabmo.microservices.api.models.RemoteSystems

import io.circe.Json

class ApiActor(val flightPath: String, val hotelPath: String, val authPath: String)
    extends Actor with AcquireRemoteSystems {

  override def active(remoteSystems: RemoteSystems): Receive = {
    case Travel(token) => {
      // Summon a new actor in charge of calling and waiting all sub-systems responses
      val actor = context.actorOf(GetTravel.props(sender(), remoteSystems))
      actor ! GetTravel.Execute(token)
    }

    case Terminated(actor) =>
      reacquireDependencies(actor)

    case ReceiveTimeout => // What's up bro?
  }

}

object ApiActor {
  def props(flightPath: String, hotelPath: String, authPath: String) = {
    Props(new ApiActor(flightPath, hotelPath, authPath))
  }

  case class Travel(token: Json)

}
