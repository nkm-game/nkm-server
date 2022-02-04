package com.tosware.NKM.services.http.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.tosware.NKM.services.http.directives.JwtDirective

trait WebsocketRoutes extends JwtDirective
    with SprayJsonSupport
{
  implicit val system: ActorSystem
  def greeter = Flow[Message].mapConcat {
    case tm: TextMessage =>
      TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
    case bm: BinaryMessage =>
      // ignore binary messages but drain content to avoid the stream being clogged
      bm.dataStream.runWith(Sink.ignore)
      Nil
  }

  val websocketRoutes =
    path("greeter") {
      handleWebSocketMessages(greeter)
    }
}
