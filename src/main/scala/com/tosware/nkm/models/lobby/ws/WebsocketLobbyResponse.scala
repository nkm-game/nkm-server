package com.tosware.nkm.models.lobby.ws

import com.tosware.nkm.models.WebsocketResponse

case class WebsocketLobbyResponse(lobbyResponseType: LobbyResponse, statusCode: Int, body: String = "")
    extends WebsocketResponse
