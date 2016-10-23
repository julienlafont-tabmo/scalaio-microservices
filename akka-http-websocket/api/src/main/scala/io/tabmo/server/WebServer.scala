package io.tabmo.server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import io.tabmo.directives.{HeaderJsonDirectives, XorTDirectives}

import cats.data.{Xor, XorT}
import cats.implicits._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.syntax._
import io.circe.{DecodingFailure, Json}

class WebServer(implicit system: ActorSystem) extends Directives with CirceSupport with HeaderJsonDirectives with XorTDirectives {

  val webSocketAuth = new ReactiveWebSocket("ws://localhost:10000/ws")
  val webSocketHotels = new ReactiveWebSocket("ws://localhost:10001/ws")
  val webSocketFlights = new ReactiveWebSocket("ws://localhost:10002/ws")

  def route =
    path("travel") {
      headerAsJson("token") { token =>
        complete {
          fetchTravel(token)
        }
      }
    }

  def fetchTravel(token: Json): XorT[Future, ToResponseMarshallable, Json] = {
    for {
      subject <- XorT(webSocketAuth.ask("get-identity", token)(100.millis).map(extractSubject))
      flights <- XorT(webSocketFlights.ask("list", subject)(500.millis).map(extractFlights))
      hotels <- XorT(webSocketHotels.ask("list", subject)(500.millis).map(extractHotels))
    } yield {
      Json.obj(
        "flights" -> flights,
        "hotels" -> hotels
      )
    }
  }

  def extractSubject(json: Json): Xor[ToResponseMarshallable, Json] = {
    val cursor = json.cursor

    val subject: Xor[DecodingFailure, Json] = cursor.get[String]("subject")
      .map(subject => Json.obj("subject" -> subject.asJson))

    subject.leftMap { _ =>
      val error = cursor.get[String]("error").getOrElse("wtf error")
      InternalServerError -> error
    }
  }

  def extractFlights(json: Json): Xor[ToResponseMarshallable, Json] = {
    json.cursor.get[String]("error").fold(
      _ => Xor.right(json),
      error => Xor.left(InternalServerError -> error)
    )
  }

  def extractHotels(json: Json): Xor[ToResponseMarshallable, Json] = {
    json.cursor.get[String]("error").fold(
      _ => Xor.right(json),
      error => Xor.left(InternalServerError -> error)
    )
  }

  def shutdown = {
    webSocketAuth.close()
  }
}
