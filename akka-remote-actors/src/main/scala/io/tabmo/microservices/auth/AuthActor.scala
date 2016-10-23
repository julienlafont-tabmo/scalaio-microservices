package io.tabmo.microservices.auth

import akka.actor.{Actor, ActorLogging, Props}
import io.tabmo.microservices.auth.AuthActor._

import cats.data.Xor
import io.circe.Json

class AuthActor extends Actor with ActorLogging {

  val accreditedUsers = Seq("pierre", "sarah")

  override def receive: Receive = {
    case GetIdentity(token) => {
      val response = token.cursor.get[String]("sub").fold(
        decodingFailure => Xor.left("Invalid token"),
        {
          case user if accreditedUsers.contains(user) => Xor.right(user)
          case user => Xor.left(s"Not accredited user $user")
        }
      )

      sender() ! GetIdentityResponse(response)
    }
  }
}

object AuthActor {
  def props() = Props(new AuthActor())

  case class GetIdentity(token: Json)
  case class GetIdentityResponse(response: Xor[String, String])
}
