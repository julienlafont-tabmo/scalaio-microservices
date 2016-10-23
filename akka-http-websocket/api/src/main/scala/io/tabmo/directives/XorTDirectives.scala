package io.tabmo.directives

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, StandardRoute}
import akka.http.scaladsl.model.StatusCodes._

import cats.data.XorT
import cats.implicits._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.Json

trait XorTDirectives extends Directives with CirceSupport {
  def complete(in: XorT[Future, ToResponseMarshallable, Json]): StandardRoute = {
    complete {
      in.fold(
        identity,
        success => OK -> success: ToResponseMarshallable
      )
    }
  }
}
