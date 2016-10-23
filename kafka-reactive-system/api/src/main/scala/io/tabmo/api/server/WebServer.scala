package io.tabmo.api.server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import org.patricknoir.kafka.reactive.client.KafkaReactiveClient

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.util.Timeout
import cats.data.XorT
import cats.implicits._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.tabmo.api.directives.{HeaderJsonDirectives, XorTDirectives}
import io.circe.Json

class WebServer(client: KafkaReactiveClient)(implicit system: ActorSystem) extends Directives with CirceSupport with HeaderJsonDirectives with XorTDirectives {

  def route =
    path("travel") {
      headerAsJson("token") { token =>
        complete {
          fetchTravel(token)
        }
      }
    }

  def fetchTravel(token: Json): XorT[Future, Error, Json] = {
    implicit val timeout = Timeout(200.millis)

    for {
      subject <- XorT(client.request[Json, String]("kafka:auth/get-identity", token))
      flights <- XorT(client.request[String, Json]("kafka:flight/list", subject))
      hotels <- XorT(client.request[String, Json]("kafka:hotel/list", subject))
    } yield {
      Json.obj(
        "flights" -> flights,
        "hotels" -> hotels
      )
    }
  }

}
