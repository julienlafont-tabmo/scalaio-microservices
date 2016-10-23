package io.tabmo.server

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.FiniteDuration
import java.util.UUID

import common.models.{WebSocketEvent, WebSocketResponse}
import org.jfarcand.wcs.{TextListener, WebSocket}
import akka.actor.ActorSystem
import akka.pattern.after

import io.circe.parser
import io.circe.Json
import io.circe.syntax._

class ReactiveWebSocket(url: String)(implicit actorSystem: ActorSystem) {

  import actorSystem.dispatcher

  private val socket = WebSocket().open(url)

  def ask(event: String, msg: Json)(implicit timeout: FiniteDuration): Future[Json] = {
    val uuid = UUID.randomUUID()
    val promise = Promise[Json]()

    val oneShotListener = new TextListener {
      override def onMessage(message: String): Unit = {
        parser.decode[WebSocketResponse](message).toOption
          .filter(_.id == uuid)
          .foreach { response => promise.success(response.data) }
      }
    }

    // Return a failed future if the timeout expired
    val timeoutFuture = after(timeout, actorSystem.scheduler)(Future.failed(new java.util.concurrent.TimeoutException()))
    val future = Future.firstCompletedOf(Seq(promise.future, timeoutFuture))

    // And always remove its listener when the future complete (in success or error)
    future.onComplete { _ =>
      socket.removeListener(oneShotListener)
    }

    socket
      .listener(oneShotListener)
      .send(WebSocketEvent(event, uuid, Some(msg)).asJson.noSpaces)

    future
  }


  def tell(event: String, msg: Json): Unit = {
    val uuid = UUID.randomUUID()
    socket.send(WebSocketEvent(event, uuid, Some(msg)).asJson.noSpaces)
  }

  def close() = socket.close

}
