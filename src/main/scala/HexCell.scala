import akka.actor.{Actor, ActorLogging}

trait HexEffect
case class HexCoordinates(x: Int, y: Int)

object HexCell {
  case object GetNKMCharacter
  case object GetEffects
  case object GetCoordinates
}

case class HexCell(coordinates: HexCoordinates, character: Option[NKMCharacter], effects: Set[HexEffect]) extends Actor with ActorLogging {

  override def receive: Receive = ???
}
