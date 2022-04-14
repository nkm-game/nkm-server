package com.tosware.NKM.models.lobby.ws

import com.tosware.NKM.models.WebsocketResponse

case class WebsocketLobbyResponse(lobbyResponseType: LobbyResponseType, statusCode: Int, body: String = "") extends WebsocketResponse
