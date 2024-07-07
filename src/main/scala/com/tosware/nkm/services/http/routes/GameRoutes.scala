package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import com.tosware.nkm.NkmDependencies
import com.tosware.nkm.services.GameService
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import akka.http.scaladsl.server.Route

class GameRoutes(deps: NkmDependencies) extends JwtDirective
    with SprayJsonSupport {
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val gameService: GameService = deps.gameService

  val getRoutes: Route = concat(
    path("state" / Segment) { ((lobbyId: String)) =>
      gameService.getGameStateViewOpt(lobbyId, None) match {
        case Some(gameStateView) => complete(gameStateView)
        case None                => complete(StatusCodes.NotFound)
      }
    }
  )
}
