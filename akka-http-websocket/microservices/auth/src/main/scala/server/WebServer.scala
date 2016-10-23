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
        case "get-identity" => { implicit event =>
          getUserIdentityFromToken()
        }

        case "ping" => { implicit event =>
          WebSocketResponse(Json.True)
        }
      }
    }

  val accreditedUsers = Seq("pierre", "sarah")

  // In real application, use a JWT library to check token validity and extract usefull data
  def getUserIdentityFromToken()(implicit event: WebSocketEvent): WebSocketResponse = {
    event.data.getOrElse(Json.Null).cursor.get[String]("sub").fold(
      decodingFailure => WebSocketResponse.error("Invalid token"),
      {
        case user if accreditedUsers.contains(user) => WebSocketResponse(Json.obj("subject" -> user.asJson))
        case user => WebSocketResponse.error(s"Not accredited user $user")
      }
    )
  }

}


