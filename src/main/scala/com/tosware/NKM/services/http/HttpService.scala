package com.tosware.NKM.services.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.http.directives._
import com.tosware.NKM.services.http.routes._
import com.tosware.NKM.{CORSHandler, NKMTimeouts}

trait HttpService
  extends CORSHandler
    with SprayJsonSupport
    with NKMJsonProtocol
    with NKMTimeouts
    with JwtDirective
    with LoggingDirective
    with WebsocketRoutes
    with AuthRoutes
    with LobbyRoutes
    with GameRoutes
    with NKMDataRoutes
{
  val routes: Route = {
    logRequestResponse {
      corsHandler {
        concat(
          pathPrefix("ws") {
            websocketRoutes
          },
          pathPrefix("api") {
            get {
              concat(
                lobbyGetRoutes,
                gameGetRoutes,
                nkmDataGetRoutes,
              )
            } ~
              post {
                concat(
                  authPostRoutes,
                )
              }
          }
        )
      }
    }
  }

}
