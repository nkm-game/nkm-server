import NKMCharacter.GetStat
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("NKMServer")
  import system.dispatcher

  val aqua = system.actorOf(NKMCharacter.props("Aqua", 12, 53, 12, 4, 3, 4))
  implicit val timeout: Timeout = Timeout(2 seconds)
  val future = (aqua ? GetStat(HealthPoints)).mapTo[Stat]
  future.onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(s"Oh no, $exception")
  }
}
