package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.*

import com.tosware.nkm.NkmDependencies
import com.tosware.nkm.services.GameService
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}

class GameRoutes(deps: NkmDependencies) extends JwtDirective
  with SprayJsonSupport
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val gameService: GameService = deps.gameService

  val gameGetRoutes = concat(
    path("state"/ Segment) { (lobbyId: String) =>
      complete(gameService.getGameStateView(lobbyId, None))
    },
  )
}
