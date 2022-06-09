package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.NKMDependencies
import com.tosware.NKM.services.GameService
import com.tosware.NKM.services.http.directives.{JwtDirective, JwtSecretKey}

class GameRoutes(deps: NKMDependencies) extends JwtDirective
  with SprayJsonSupport
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val gameService: GameService = deps.gameService

  val gameGetRoutes = concat(
    path("state"/ Segment) { (gameId: String) =>
      complete(gameService.getGameState(gameId))
    },
  )
}
