package server

import common.WebSocketJsonDirectives
import common.models.{WebSocketEvent, WebSocketResponse}
import akka.actor.ActorSystem

import io.circe.Json
import io.circe.syntax._

class WebServer(implicit system: ActorSystem) extends WebSocketJsonDirectives {

  def route =
    path("ws") {
      handlerWebSocketJson {
        case "list" => { implicit event =>
          listAvailableAccomodations()
        }

        case "ping" => { implicit event =>
          WebSocketResponse(Json.True)
        }
      }
    }

  val accomodationsByUser = Map(
    "pierre" -> Seq("Hotel 1", "Hotel 2", "Hotel 3"),
    "sarah" -> Seq("Hotel 3", "Hotel 4", "Hotel 5")
  )

  def listAvailableAccomodations()(implicit event: WebSocketEvent): WebSocketResponse = {
    event.data.getOrElse(Json.Null).cursor.get[String]("subject").fold(
      _ => WebSocketResponse.error("User not found"),
      subject => WebSocketResponse(Json.fromValues(accomodationsByUser.getOrElse(subject, Nil).map(_.asJson)))
    )
  }

}


