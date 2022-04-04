package com.tosware.NKM.models.game.ws

case class WebsocketGameResponse(gameResponseType: GameResponseType, statusCode: Int, body: String = "")
