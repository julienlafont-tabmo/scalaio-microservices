package io.tabmo.api

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import org.patricknoir.kafka.reactive.client.KafkaReactiveClient
import org.patricknoir.kafka.reactive.client.config.KafkaRClientSettings
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.tabmo.api.server.WebServer

object Boot extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val client = new KafkaReactiveClient(KafkaRClientSettings.default)
  warmup(client)

  val api = new WebServer(client)
  val bindingFuture = Http().bindAndHandle(api.route, "localhost", 8080)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      println(s"[api] Server is listening on localhost:8080")

    case Failure(e) ⇒
      println(s"[api] Binding failed with ${e.getMessage}")
      e.printStackTrace()
      system.terminate()
  }



  private def warmup(client: KafkaReactiveClient) = {
    println("Warmup Kafka connection...")

    implicit val timeout = Timeout(45.seconds)
    Await.ready(client.request[String, String]("kafka:auth/warmup", "hey bro!"), Duration.Inf)

    println("Ready!")
  }
}
