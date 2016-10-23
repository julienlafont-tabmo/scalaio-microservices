package common.models

import io.circe.Json
import io.circe.syntax._

import java.util.UUID

case class WebSocketResponse(id: UUID, data: Json)

object WebSocketResponse {

  import io.circe.generic.semiauto._

  implicit val encoder = deriveEncoder[WebSocketResponse]
  implicit val decoder = deriveDecoder[WebSocketResponse]

  def apply(response: Json)(implicit event: WebSocketEvent): WebSocketResponse = WebSocketResponse(event.id, response)
  def error(msg: String)(implicit event: WebSocketEvent): WebSocketResponse = WebSocketResponse(event.id, Json.obj("error" -> msg.asJson))
  def systemError(msg: String): WebSocketResponse = WebSocketResponse(UUID.fromString("00000000-0000-0000-0000-000000000000"), Json.obj("error" -> msg.asJson))
}
