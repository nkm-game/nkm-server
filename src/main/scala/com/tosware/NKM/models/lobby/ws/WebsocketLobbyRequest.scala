package com.tosware.NKM.models.lobby.ws

import com.tosware.NKM.models.WebsocketRequest

case class WebsocketLobbyRequest(requestPath: LobbyRoute, requestJson: String = "") extends WebsocketRequest
