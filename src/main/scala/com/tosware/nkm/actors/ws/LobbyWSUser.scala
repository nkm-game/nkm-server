package com.tosware.nkm.actors.ws

import akka.actor.ActorRef
import com.tosware.nkm.services.LobbyService
import com.tosware.nkm.services.http.directives.JwtSecretKey

class LobbyWSUser(val session: ActorRef)(
    implicit val lobbyService: LobbyService,
    implicit val jwtSecretKey: JwtSecretKey,
) extends WebsocketUser
    with LobbyWebsocketUserBehaviour
