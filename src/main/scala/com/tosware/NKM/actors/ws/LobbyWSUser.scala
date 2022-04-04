package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtSecretKey

class LobbyWSUser(val session: ActorRef)(implicit val lobbyService: LobbyService, implicit val jwtSecretKey: JwtSecretKey)
  extends WebsocketUser
    with LobbyWebsocketUserBehaviour
