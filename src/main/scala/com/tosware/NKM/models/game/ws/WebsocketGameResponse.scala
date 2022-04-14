package com.tosware.NKM.models.game.ws

import com.tosware.NKM.models.WebsocketResponse

case class WebsocketGameResponse(gameResponseType: GameResponseType, statusCode: Int, body: String = "") extends WebsocketResponse
