package com.tosware.nkm.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.http.directives.JwtHelper

trait WebsocketUserBehaviour
    extends Actor
    with ActorLogging
    with SprayJsonSupport
    with NkmJsonProtocol
    with NkmTimeouts
    with JwtHelper {
  def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit
}
