package com.tosware.NKM.models.game.ws

import com.tosware.NKM.models.WebsocketRequest

case class WebsocketGameRequest(requestPath: GameRoute, requestJson: String = "") extends WebsocketRequest
