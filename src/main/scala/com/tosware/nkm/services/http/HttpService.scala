package com.tosware.nkm.services.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.http.directives._
import com.tosware.nkm.services.http.routes._
import com.tosware.nkm.{CORSHandler, NkmDependencies, NkmTimeouts}

class HttpService(deps: NkmDependencies)
  extends CORSHandler
    with SprayJsonSupport
    with NkmJsonProtocol
    with NkmTimeouts
    with LoggingDirective
{
  val authRoutes = new AuthRoutes(deps)
  val lobbyRoutes = new LobbyRoutes(deps)
  val gameRoutes = new GameRoutes(deps)
  val nkmDataRoutes = new NkmDataRoutes(deps)
  val websocketRoutes = new WebsocketRoutes(deps)

  val routes: Route = {
    logRequestResponse {
      corsHandler {
        concat(
          pathPrefix("ws") {
            websocketRoutes.websocketRoutes
          },
          pathPrefix("api") {
            get {
              concat(
                lobbyRoutes.lobbyGetRoutes,
                gameRoutes.gameGetRoutes,
                nkmDataRoutes.nkmDataGetRoutes,
              )
            } ~
              post {
                concat(
                  authRoutes.authPostRoutes,
                )
              }
          }
        )
      }
    }
  }
}
