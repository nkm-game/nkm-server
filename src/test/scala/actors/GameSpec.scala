package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.actors.Game._
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.actors.{Game, NKMData}
import com.tosware.NKM.models._
import helpers.NKMPersistenceTestKit

import java.util.UUID.randomUUID
import scala.concurrent.Await
import scala.language.postfixOps

class GameSpec extends NKMPersistenceTestKit(ActorSystem("GameSpec"))
{
  val nkmData: ActorRef = system.actorOf(NKMData.props())
  "An Game actor" must {
    "works" in {
      within2000 {
        val hexMaps = Await.result((nkmData ? GetHexMaps).mapTo[List[HexMap]], atMost)
        val game = system.actorOf(Game.props("1"))

        val playerNames = List("Ryszard", "Ania", "Ola")
        val characters: List[NKMCharacter] = List[NKMCharacter](
          NKMCharacter(randomUUID().toString, "Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
          NKMCharacter(randomUUID().toString, "Dekomori Sanae", 14, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
          NKMCharacter(randomUUID().toString, "Touka", 0, Stat(34), Stat(43), Stat(4), Stat(34), Stat(5))
        )
        val touka = characters.find(_.name == "Touka").get

        playerNames.foreach(n => game ! AddPlayer(n))
        //    val players = Await.result((game ? GetState).mapTo[GameState].map(s => s.players), 2 seconds)

        characters.foreach(c => game ! AddCharacter("Ola", c))

        game ! SetMap(hexMaps.head)
        game ! PlaceCharacter(HexCoordinates(4, 5), touka.id)

        val state = Await.result((game ? GetState).mapTo[GameState], atMost)

        state.players.length shouldEqual 3
        state.hexMap.cells.find(_.coordinates == HexCoordinates(4, 5)).get.characterId.get shouldEqual touka.id
        state.hexMap.cells.find(_.coordinates == HexCoordinates(0, 0)).get.characterId shouldEqual None

        game ! MoveCharacter(HexCoordinates(0, 0), touka.id)

        val state2 = Await.result((game ? GetState).mapTo[GameState], atMost)
        state2.hexMap.cells.find(_.coordinates == HexCoordinates(4, 5)).get.characterId shouldEqual None
        state2.hexMap.cells.find(_.coordinates == HexCoordinates(0, 0)).get.characterId.get shouldEqual touka.id
      }
    }
  }
}
