package io.tabmo.microservices.api.models

import akka.actor.ActorRef

// References all connected remote systems
case class RemoteSystems(auth: ActorRef, hotel: ActorRef, flight: ActorRef)

