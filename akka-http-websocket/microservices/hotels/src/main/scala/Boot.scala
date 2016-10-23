import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import server.WebServer

import scala.util.{Failure, Success}

object Boot extends App{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val webServer = new WebServer()

  val port = 10001
  val bindingFuture = Http().bindAndHandle(webServer.route, "localhost", port)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      println(s"[hotels] Server is listening on localhost:$port")

    case Failure(e) ⇒
      println(s"[hotels] Binding failed with ${e.getMessage}")
      e.printStackTrace()
      system.terminate()
  }

}
