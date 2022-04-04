package com.tosware.NKM.actors.ws

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.http.directives.JwtHelper

trait WebsocketUserBehaviour
  extends Actor
    with ActorLogging
    with SprayJsonSupport
    with NKMJsonProtocol
    with NKMTimeouts
    with JwtHelper {
  def parseIncomingMessage(outgoing: ActorRef, username: Option[String], text: String): Unit
}
