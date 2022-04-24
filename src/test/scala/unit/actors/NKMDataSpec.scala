package unit.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.actors.NKMData
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.models.game.HexMap
import helpers.NKMPersistenceTestKit

import scala.concurrent.Await
import scala.language.postfixOps

class NKMDataSpec extends NKMPersistenceTestKit(ActorSystem("NKMDataSpec"))
{
  val nkmData: ActorRef = system.actorOf(NKMData.props())

  "An NKMData actor" must {
    "send HexMaps" in {
      within2000 {
        val future = nkmData ? GetHexMaps
        val hexMaps = Await.result(future.mapTo[List[HexMap]], atMost)
        assert(hexMaps.nonEmpty)
      }
    }
  }
}
