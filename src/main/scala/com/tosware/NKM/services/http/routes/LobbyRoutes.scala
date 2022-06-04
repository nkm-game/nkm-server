package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.models.lobby.ws._
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective


trait LobbyRoutes extends JwtDirective
  with SprayJsonSupport
{
  implicit val lobbyService: LobbyService

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
