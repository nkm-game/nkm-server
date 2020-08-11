package NKM

import akka.pattern.ask
import Actors._
import NKM.Actors.Game.{GetState, PlaceCharacter}
import NKM.Actors.NKMData.GetHexMaps
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App with NKMJsonProtocol with SprayJsonSupport {
  implicit val system: ActorSystem = ActorSystem("NKMServer")

  val game = system.actorOf(Game.props("1"))
  val nkmData = system.actorOf(NKMData.props())
  implicit val timeout: Timeout = Timeout(2 seconds)

  val skeleton =
    pathPrefix("api" / "state") {
      get {
        complete((game ? GetState).mapTo[GameState])
      }
    } ~
    pathPrefix("api" / "maps") {
      get {
        complete((nkmData ? GetHexMaps).mapTo[List[HexMap]])
      }
    }
  Http().newServerAt("localhost", 8080).bindFlow(skeleton)

  game ! PlaceCharacter(HexCoordinates(4, 5), NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)))
  //  system.terminate()
}
