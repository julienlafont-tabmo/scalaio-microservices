package io.tabmo.webserver.directives

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.{MalformedHeaderRejection, _}
import akka.http.scaladsl.server.directives.HeaderDirectives
import akka.http.scaladsl.server.directives.RouteDirectives._

import io.circe.Json

trait HeaderJsonDirectives extends HeaderDirectives {

  def headerAsJson(name: String): Directive1[Json] = {
    def optionalJsonValue(lowerCaseName: String): HttpHeader => Option[Json] = {
      case HttpHeader(`lowerCaseName`, value) => io.circe.parser.parse(value).toOption
      case _ => None
    }

    headerValue(optionalJsonValue(name)) | reject(MalformedHeaderRejection(name, "JSON expected"))
  }
}
