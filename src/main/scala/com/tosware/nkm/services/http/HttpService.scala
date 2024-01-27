package com.tosware.nkm.services.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.SwaggerDocService
import com.tosware.nkm.services.http.directives.*
import com.tosware.nkm.services.http.routes.*
import com.tosware.nkm.{CORSHandler, NkmDependencies, NkmTimeouts}

class HttpService(deps: NkmDependencies, port: Int)
    extends CORSHandler
    with SprayJsonSupport
    with NkmJsonProtocol
    with NkmTimeouts
    with LoggingDirective {
  val userRoutes = new UserRoutes(deps)
  val lobbyRoutes = new LobbyRoutes(deps)
  val gameRoutes = new GameRoutes(deps)
  val nkmDataRoutes = new NkmDataRoutes(deps)
  val websocketRoutes = new WebsocketRoutes(deps)
  val bugReportRoutes = new BugReportRoutes(deps)

  val routes: Route =
    logRequestResponse {
      corsHandler {
        concat(
          pathPrefix("ws") {
            websocketRoutes.websocketRoutes
          },
          pathPrefix("api") {
            concat(
              userRoutes.userRoutes,
              get {
                concat(
                  lobbyRoutes.getRoutes,
                  gameRoutes.getRoutes,
                  nkmDataRoutes.getRoutes,
                  bugReportRoutes.getRoutes,
                )
              } ~
                post {
                  concat(
                    userRoutes.authPostRoutes,
                    bugReportRoutes.postRoutes,
                  )
                },
            )
          },
          new SwaggerDocService(port).routes,
          pathPrefix("swagger") {
            pathEndOrSingleSlash {
              getFromResource("swagger-ui/index.html")
            } ~
              getFromResourceDirectory("swagger-ui")
          },
        )
      }
    }
}
