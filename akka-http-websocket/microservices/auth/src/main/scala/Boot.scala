import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import server.WebServer
import scala.util.{Failure, Success}

object Boot extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val api = new WebServer

  val port = 10000
  val bindingFuture = Http().bindAndHandle(api.route, "localhost", port)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      println(s"[auth] Server is listening on localhost:$port")

    case Failure(e) ⇒
      println(s"[auth] Binding failed with ${e.getMessage}")
      e.printStackTrace()
      system.terminate()
  }

}
