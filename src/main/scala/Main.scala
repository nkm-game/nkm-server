import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

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

import scala.io.Source

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

  val Pattern = """(.*):(.*);(.*)""".r
  val SpawnPointPattern = """SpawnPoint(\d*)""".r
  var set = Set[HexCell]()
  val map = "TestMap"
  Source.fromResource(s"HexMaps/$map.hexmap").getLines().drop(2).foreach {
    case Pattern(x, z, cellType) =>
      val sn: (HexCellType, Option[Int]) = cellType match {
        case SpawnPointPattern(number) => (SpawnPoint, Some(number.toInt))
        case "Normal" => (Normal, None)
        case "Wall" => (Wall, None)

      }
      set = set + HexCell(HexCoordinates(x.toInt, z.toInt), sn._1, None, Set(), sn._2)
  }
  Files write(Paths get s"$map.json", HexMap(map, set).toJson.prettyPrint getBytes StandardCharsets.UTF_8)//  Http().newServerAt("localhost", 8080).bindFlow(skel)
//   (game ? GetState).mapTo[GameState] onComplete {
//     case Success(value) => println(value.toJson.convertTo[GameState])
//     case _ =>
//   }
  system.terminate()
}
