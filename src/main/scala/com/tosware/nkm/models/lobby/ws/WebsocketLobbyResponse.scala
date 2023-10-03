package com.tosware.nkm.models.lobby.ws

import com.tosware.nkm.models.WebsocketResponse

case class WebsocketLobbyResponse(lobbyResponseType: LobbyResponseType, statusCode: Int, body: String = "")
    extends WebsocketResponse
