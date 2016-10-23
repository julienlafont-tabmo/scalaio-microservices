package io.tabmo

import scala.util.{Failure, Success}

import com.typesafe.config.ConfigFactory
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import io.tabmo.microservices.api.ApiActor
import io.tabmo.microservices.auth.AuthActor
import io.tabmo.microservices.flight.FlightActor
import io.tabmo.microservices.hotel.HotelActor
import io.tabmo.webserver.WebServer

object MicroservicesApplication {

  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "flights")
      startFlightRemoteSystem()

    if (args.isEmpty || args.head == "hotels")
      startHotelsRemoteSystem()

    if (args.isEmpty || args.head == "auth")
      startAuthRemoteSystem()

    if (args.isEmpty || args.head == "api")
      startApiSystem()

  }

  val flightPath = "akka.tcp://FlightSystem@127.0.0.1:2554/user/flightActor"
  val hotelPath = "akka.tcp://HotelSystem@127.0.0.1:2555/user/hotelActor"
  val authPath = "akka.tcp://AuthSystem@127.0.0.1:2556/user/authActor"


  def startApiSystem(): Unit = {
    val system = ActorSystem("ApiSystem", ConfigFactory.load("api"))

    val actor = system.actorOf(ApiActor.props(flightPath, hotelPath, authPath), "api")

    println(s"Started ApiActor at $actor")

    startWebServer(actor)
  }

  def startFlightRemoteSystem(): Unit = {
    val system = ActorSystem("FlightSystem", ConfigFactory.load("remote-flight"))
    val actor = system.actorOf(FlightActor.props(), "flightActor")

    println(s"Starter FlightActor at $actor")
  }

  def startHotelsRemoteSystem(): Unit = {
    val system = ActorSystem("HotelSystem", ConfigFactory.load("remote-hotel"))
    val actor = system.actorOf(HotelActor.props(), "hotelActor")

    println(s"Starter HotelActor at $actor")
  }

  def startAuthRemoteSystem(): Unit = {
    val system = ActorSystem("AuthSystem", ConfigFactory.load("remote-auth"))

    val actor = system.actorOf(AuthActor.props(), "authActor")

    println(s"Starter AuthActor at $actor")
  }

  def startWebServer(api: ActorRef) = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val server = new WebServer(api)
    val bindingFuture = Http().bindAndHandle(server.route, "localhost", 8080)

    bindingFuture.onComplete {
      case Success(binding) ⇒
        println(s"[WebServer] Server is listening on localhost:8080")

      case Failure(e) ⇒
        println(s"[WebServer] Binding failed with ${e.getMessage}")
        e.printStackTrace()
        system.terminate()
    }
  }
}
