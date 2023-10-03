package com.tosware.nkm.actors.ws

import akka.actor.ActorRef
import com.tosware.nkm.services.GameService
import com.tosware.nkm.services.http.directives.JwtSecretKey

class GameWSUser(val session: ActorRef)(implicit val gameService: GameService, implicit val jwtSecretKey: JwtSecretKey)
    extends WebsocketUser
    with GameWebsocketUserBehaviour
