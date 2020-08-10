import Game.GetState
import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.PersistentActor

object Game {
  case object GetState
  def props(id: String): Props = Props(new Game(id, HexMap("test", Set[HexCell](HexCell(HexCoordinates(4, 5), Normal, None, Set(), None)))))
}

class Game(id: String, hexMap: HexMap) extends Actor with ActorLogging {
  val characters: Set[NKMCharacter] = Set[NKMCharacter](
    NKMCharacter("Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
    NKMCharacter("Dekomori Sanae", 14, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
    NKMCharacter("Aqua", 0, Stat(34), Stat(43), Stat(4), Stat(34), Stat(5))
  )
  var gameState: GameState = GameState(hexMap, characters)

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