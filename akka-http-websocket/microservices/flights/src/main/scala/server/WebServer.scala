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
          listCompatibleFlights()
        }

        case "ping" => { implicit event =>
          WebSocketResponse(Json.True)
        }
      }
    }

  val flightsByUser = Map(
    "pierre" -> Seq("05:00-09:00", "08:00-15:00", "13:00-17:00"),
    "sarah" -> Seq("11:00-13:00", "12:00-14:00", "18:00-20:00")
  )

  def listCompatibleFlights()(implicit event: WebSocketEvent): WebSocketResponse = {
    event.data.getOrElse(Json.Null).cursor.get[String]("subject").fold(
      _ => WebSocketResponse.error("User not found"),
      subject => WebSocketResponse(Json.fromValues(flightsByUser.getOrElse(subject, Nil).map(_.asJson)))
    )
  }

}


