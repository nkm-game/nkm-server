package com.tosware.NKM.services.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.http.directives._
import com.tosware.NKM.services.http.routes._
import com.tosware.NKM.{CORSHandler, NKMDependencies, NKMTimeouts}

class HttpService(deps: NKMDependencies)
  extends CORSHandler
    with SprayJsonSupport
    with NKMJsonProtocol
    with NKMTimeouts
    with LoggingDirective
{
  val authRoutes = new AuthRoutes(deps)
  val lobbyRoutes = new LobbyRoutes(deps)
  val gameRoutes = new GameRoutes(deps)
  val nkmDataRoutes = new NKMDataRoutes(deps)
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
