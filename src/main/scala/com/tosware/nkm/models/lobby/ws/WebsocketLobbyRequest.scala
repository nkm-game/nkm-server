package com.tosware.nkm.models.lobby.ws

import com.tosware.nkm.models.WebsocketRequest

case class WebsocketLobbyRequest(requestPath: LobbyRoute, requestJson: String = "") extends WebsocketRequest
