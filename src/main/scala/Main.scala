import Game.{GetState, PlaceCharacter}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App with NKMJsonProtocol with SprayJsonSupport {
  implicit val system: ActorSystem = ActorSystem("NKMServer")

  val game = system.actorOf(Game.props("1"))
  implicit val timeout: Timeout = Timeout(2 seconds)

  val skel =
    pathPrefix("api" / "state") {
      get {
        complete((game ? GetState).mapTo[GameState])
      }
    }
  Http().newServerAt("localhost", 8080).bindFlow(skel)

  game ! PlaceCharacter(HexCoordinates(4, 5), NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)))
  //  system.terminate()
}
