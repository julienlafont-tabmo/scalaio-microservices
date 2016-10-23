package io.tabmo

import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import io.tabmo.server.WebServer

object Boot extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val api = new WebServer
  val bindingFuture = Http().bindAndHandle(api.route, "localhost", 8080)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      println(s"[api] Server is listening on localhost:8080")

    case Failure(e) ⇒
      println(s"[api] Binding failed with ${e.getMessage}")
      api.shutdown
      e.printStackTrace()
      system.terminate()
  }

}
