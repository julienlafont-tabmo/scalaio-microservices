package io.tabmo.api.directives

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, StandardRoute}
import akka.http.scaladsl.model.StatusCodes._

import cats.data.XorT
import cats.implicits._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.Json

trait XorTDirectives extends Directives with CirceSupport {

  def complete(in: XorT[Future, Error, Json])(implicit ec: ExecutionContext): StandardRoute = {
    complete {
      in.fold[ToResponseMarshallable](
        error => InternalServerError -> error.getMessage,
        success => OK -> success
      )
    }
  }
}
