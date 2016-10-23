package common.models

import io.circe.Json
import java.util.UUID

case class WebSocketEvent(event: String, id: UUID, data: Option[Json])

object WebSocketEvent {
  import io.circe.generic.semiauto._

  implicit val encoder = deriveEncoder[WebSocketEvent]
  implicit val decoder = deriveDecoder[WebSocketEvent]
}
