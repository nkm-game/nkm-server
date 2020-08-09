import Game.GetState
import akka.actor.{Actor, ActorLogging}
import akka.persistence.PersistentActor

object Game {
  case object GetState
}

class Game(id: String, hexMap: HexMap) extends PersistentActor with ActorLogging {
  var gameState: GameState = GameState(hexMap, Set[NKMCharacter]())

  override def persistenceId: String = "game-$id"
  override def receive: Receive = {
    case GetState => {
      log.info("Received state request")
      sender ! gameState
    }
  }

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}