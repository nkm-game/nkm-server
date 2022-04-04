package com.tosware.NKM.models.lobby.ws

case class WebsocketLobbyResponse(lobbyResponseType: LobbyResponseType, statusCode: Int, body: String = "")
