package com.tosware.NKM

import akka.pattern.ask
import actors._
import com.tosware.NKM.actors.Game._
import com.tosware.NKM.actors.NKMData.GetHexMaps
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App with NKMJsonProtocol with SprayJsonSupport {
  import system.dispatcher

  implicit val system: ActorSystem = ActorSystem("NKMServer")

  val nkmData = system.actorOf(NKMData.props())
  implicit val timeout: Timeout = Timeout(2 seconds)
  val hexMaps = (nkmData ? GetHexMaps).mapTo[List[HexMap]]
  val oneHexMap = hexMaps.map(maps => maps.head)
  for {
    hexMaps <- (nkmData ? GetHexMaps).mapTo[List[HexMap]]
  }{
    val game = system.actorOf(Game.props("1", hexMaps.head))
    val skeleton =
      pathPrefix("api") {
        get {
          path("state") {
            complete((game ? GetState).mapTo[GameState])
          } ~
          path("maps") {
            complete((nkmData ? GetHexMaps).mapTo[List[HexMap]])
          }
        }
      }
    Http().newServerAt("localhost", 8080).bindFlow(skeleton)

//    game ! PlaceCharacter(HexCoordinates(4, 5), NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)))
//    game ! MoveCharacter(HexCoordinates(0, 0), NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)))
  }
}
