package com.tosware.nkm.services.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.http.directives._
import com.tosware.nkm.services.http.routes._
import com.tosware.nkm.{CORSHandler, NkmDependencies, NkmTimeouts}

class HttpService(deps: NkmDependencies, apiHost: String, port: Int)
  extends CORSHandler
    with SprayJsonSupport
    with NkmJsonProtocol
    with NkmTimeouts
    with LoggingDirective
    with SwaggerHttpService
{
  val authRoutes = new AuthRoutes(deps)
  val lobbyRoutes = new LobbyRoutes(deps)
  val gameRoutes = new GameRoutes(deps)
  val nkmDataRoutes = new NkmDataRoutes(deps)
  val websocketRoutes = new WebsocketRoutes(deps)

  override val apiClasses: Set[Class[_]] = Set(gameRoutes.getClass)
  override val host = s"$apiHost:$port"
  override val apiDocsPath = "api-docs"
  override val info = Info()

  override val routes: Route = {
    logRequestResponse {
      corsHandler {
        concat(
          super.routes,
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
