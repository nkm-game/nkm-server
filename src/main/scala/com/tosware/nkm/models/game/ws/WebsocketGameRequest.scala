package com.tosware.nkm.models.game.ws

import com.tosware.nkm.models.WebsocketRequest

case class WebsocketGameRequest(requestPath: GameRoute, requestJson: String = "") extends WebsocketRequest
