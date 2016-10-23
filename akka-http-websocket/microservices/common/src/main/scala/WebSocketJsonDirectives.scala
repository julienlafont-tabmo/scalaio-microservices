package common

import scala.concurrent.duration._

import akka.NotUsed
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.server.{Directives, _}
import akka.stream.scaladsl.Flow
import cats.data.Xor
import io.circe.syntax._

import common.models._
import common.models.WebSocketResponse._
import common.models.WebSocketEvent._

trait WebSocketJsonDirectives extends Directives {

  private val messageToJson: Flow[Message, Xor[WebSocketResponse, WebSocketEvent], NotUsed] = Flow[Message].map {
    case TextMessage.Strict(msg) => {
      io.circe.parser.decode[WebSocketEvent](msg) match {
        case Xor.Left(error) => Xor.Left(WebSocketResponse.systemError(s"Invalid command received: $msg\n$error"))
        case Xor.Right(event) => Xor.Right(event)
      }
    }
    case _ => Xor.Left(WebSocketResponse.systemError("Invalid JSON received"))
  }.log("INPUT")

  private val jsonToMessage: Flow[Xor[WebSocketResponse, WebSocketResponse], Strict, NotUsed] = Flow[Xor[WebSocketResponse, WebSocketResponse]].map { response =>
    TextMessage.Strict(response.merge.asJson.noSpaces)
  }

  private val error404: PartialFunction[String, WebSocketEvent => WebSocketResponse] = { case _ =>
    implicit event =>
      WebSocketResponse.error("event not implemented")
  }

  protected def handlerWebSocketJson(handler: PartialFunction[String, WebSocketEvent => WebSocketResponse]): Route = {
    handleWebSocketMessages(
      messageToJson
        .map(msg => msg.map(event => handler.orElse(error404).apply(event.event)(event)))
        .via(jsonToMessage)
        .log("OUTPUT")
        .keepAlive(30.seconds, () => TextMessage("keepalive"))
    )
  }
}
