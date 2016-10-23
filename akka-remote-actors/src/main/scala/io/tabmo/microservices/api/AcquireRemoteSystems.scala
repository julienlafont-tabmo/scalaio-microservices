package io.tabmo.microservices.api

import scala.concurrent.duration._

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Identify, ReceiveTimeout}
import io.tabmo.microservices.api.models.RemoteSystems

/**
  * Actor initialization process: swich to active behavior when all remote systems are connected
  */
trait AcquireRemoteSystems extends Actor with ActorLogging {

  val hotelPath: String
  val authPath: String
  val flightPath: String

  private val requiredDependencies = Map(
    "flight" -> flightPath,
    "hotel" -> hotelPath,
    "auth" -> authPath
  )

  // Switch to this behavior when all remote system are connected
  def active(deps: RemoteSystems): Receive

  // Send an `Identify` request  to all required remote systems
  def sendIdentifyRequest(): Unit = {
    requiredDependencies.foreach { case (code, path) =>
      context.actorSelection(path) ! Identify(code)
    }

    import context.dispatcher
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  // When the actor start, try to acquire all remote systems
  override def preStart(): Unit =  {
    sendIdentifyRequest()
  }

  // Identifying behavior: wait for all remote system connected
  def identifying(deps: Map[String, Option[ActorRef]]): Actor.Receive = {
    case ActorIdentity(code: String, Some(actor)) =>
      log.info(s"[ApiActor] $code found at $actor")
      val newDeps = deps.updated(code, Some(actor))
      context.watch(actor)
      checkInitializationComplete(newDeps)

    case ActorIdentity(code: String, None) => println(s"$code Remote actor not available at ${requiredDependencies(code)}")

    case ReceiveTimeout              => sendIdentifyRequest()
    case _                           => println("Not ready yet")
  }

  // Check if we must switch to active bahavior, of if we are still waiting some system to connect
  def checkInitializationComplete(deps: Map[String, Option[ActorRef]]) = {
    val nextBehavior = (deps("auth"), deps("hotel"), deps("flight")) match {
      case (Some(auth), Some(hotel), Some(flight)) =>
        log.info(s"[ApiActor] ready with all remote system connected")
        active(RemoteSystems(auth, hotel, flight))
      case _ =>
        identifying(deps)
    }

    context.become(nextBehavior)
  }

  // When one or many system disconnect, try to re-acquire all systems
  def reacquireDependencies(terminatedActor: ActorRef) = {
    log.info(s"[ApiActor] Actor $terminatedActor terminated")
    sendIdentifyRequest()
    context.become(identifying(requiredDependencies.mapValues(_ => None)))
  }

  // Default behavior: Wait all required dependencies
  def receive = identifying(requiredDependencies.mapValues(_ => None))
}
