import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import server.WebServer

object Boot extends App{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val webServer = new WebServer()

  val port = 10002
  val bindingFuture = Http().bindAndHandle(webServer.route, "localhost", port)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      println(s"[flights] Server is listening on localhost:$port")

    case Failure(e) ⇒
      println(s"[flights] Binding failed with ${e.getMessage}")
      e.printStackTrace()
      system.terminate()
  }

}
