package io.tabmo.webserver

import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import akka.pattern._
import akka.util.Timeout
import io.tabmo.microservices.api.ApiActor
import io.tabmo.webserver.directives.HeaderJsonDirectives

import cats.data.Xor
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.Json

class WebServer(api: ActorRef)(implicit actorSystem: ActorSystem) extends Directives with CirceSupport with HeaderJsonDirectives {

  import actorSystem.dispatcher

  def route =
    path("travel") {
      headerAsJson("token") { token =>
        complete {
          fetchTravel(token).map {
            case Xor.Left(error) => InternalServerError -> error
            case Xor.Right(response) => OK -> response
          }
        }
      }
    }

  def fetchTravel(token: Json): Future[Xor[Json, Json]] = {
    implicit val timeout = Timeout(1000.millis)
    api.ask(ApiActor.Travel(token)).mapTo[Xor[Json, Json]]
  }

}
