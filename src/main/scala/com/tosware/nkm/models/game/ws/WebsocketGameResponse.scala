package com.tosware.nkm.models.game.ws

import com.tosware.nkm.models.WebsocketResponse

case class WebsocketGameResponse(gameResponseType: GameResponseType, statusCode: Int, body: String = "") extends WebsocketResponse
