import Game.GetState
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

  Http().bindAndHandle(skel, "localhost", 8080)
}
