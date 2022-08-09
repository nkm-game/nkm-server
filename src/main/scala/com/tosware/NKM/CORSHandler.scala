package com.tosware.NKM

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

trait CORSHandler {

  private def corsResponseHeaders(origin: Option[Origin]) = List(
    origin match {
      case Some(origin) => `Access-Control-Allow-Origin`(origin.origins.head)
      case None => `Access-Control-Allow-Origin`.*
    },

    `Access-Control-Allow-Credentials`(true),

    `Access-Control-Allow-Headers`("Authorization",

      "Content-Type", "X-Requested-With")

  )

  //this directive adds access control headers to normal responses

  private def addAccessControlHeaders(origin: Option[Origin]): Directive0 = respondWithHeaders(corsResponseHeaders(origin))

  //this handles preflight OPTIONS requests.

  private def preflightRequestHandler = options {

    complete(HttpResponse(StatusCodes.OK).

      withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))

  }

  // Wrap the Route with this method to enable adding of CORS headers

  def corsHandler(r: Route): Route = extractRequest { request =>
      addAccessControlHeaders(request.header[Origin]) {
        preflightRequestHandler ~ r
      }
    }

  // Helper method to add CORS headers to HttpResponse

  // preventing duplication of CORS headers across code

//  def addCORSHeaders(response: HttpResponse): HttpResponse =
//    response.withHeaders(corsResponseHeaders)

}
