import Game.GetState
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

import spray.json._

object Main extends App with NKMJsonProtocol with SprayJsonSupport {
  implicit val system: ActorSystem = ActorSystem("NKMServer")

  val game = system.actorOf(Game.props("1"))
  implicit val timeout: Timeout = Timeout(2 seconds)
  import system.dispatcher

  val skel =
    pathPrefix("api" / "state") {
      get {
        complete((game ? GetState).mapTo[GameState])
      }
    }

//  Source.fromResource("HexMaps/1v1v1.hexmap").getLines().foreach(println)
//  Http().newServerAt("localhost", 8080).bindFlow(skel)
   (game ? GetState).mapTo[GameState] onComplete {
     case Success(value) => println(value.toJson.convertTo[GameState])
     case _ =>
   }
}
