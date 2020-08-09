import Game.GetState
import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.PersistentActor

object Game {
  case object GetState
  def props(id: String): Props = Props(new Game(id, HexMap(Set[HexCell]())))
}

class Game(id: String, hexMap: HexMap) extends Actor with ActorLogging {
  var gameState: GameState = GameState(hexMap, Set[NKMCharacter]())

//  override def persistenceId: String = "game-$id"
  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender ! gameState
  }

//  override def receiveRecover: Receive = {
//    case _ =>
//  }
//
//  override def receiveCommand: Receive = {
//    case _ =>
//  }
}