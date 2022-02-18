package com.tosware.NKM.models.lobby

case class WebsocketLobbyResponse(lobbyResponseType: LobbyResponseType, statusCode: Int, body: String = "")
