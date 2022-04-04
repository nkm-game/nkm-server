package com.tosware.NKM.actors.ws

import akka.actor.ActorRef
import com.tosware.NKM.services.GameService
import com.tosware.NKM.services.http.directives.JwtSecretKey

class GameWSUser(val session: ActorRef)(implicit val gameService: GameService, implicit val jwtSecretKey: JwtSecretKey)
  extends WebsocketUser
    with GameWebsocketUserBehaviour
