package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.NKMDependencies
import com.tosware.NKM.models.lobby.ws._
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.{JwtDirective, JwtSecretKey}


class LobbyRoutes(deps: NKMDependencies) extends JwtDirective
  with SprayJsonSupport
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val lobbyService: LobbyService = deps.lobbyService

  val lobbyGetRoutes = concat(
    path(LobbyRoute.Lobbies.value) {
      val lobbies = lobbyService.getAllLobbies()
      complete(lobbies)
    },
    path(LobbyRoute.Lobby.value / Segment) { (lobbyId: String) =>
      val lobby = lobbyService.getLobbyState(lobbyId)
      complete(lobby)
    },
  )
}
