package com.tosware.nkm.actors.ws

import akka.actor.{Actor, ActorRef}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.http.directives.JwtHelper
import com.tosware.nkm.{Logging, NkmTimeouts}

trait WebsocketUserBehaviour
    extends Actor
    with Logging
    with SprayJsonSupport
    with NkmJsonProtocol
    with NkmTimeouts
    with JwtHelper {
  def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit
}
