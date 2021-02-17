package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.tosware.NKM.actors.NKMData
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.models.HexMap
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class NKMDataSpec extends TestKit(ActorSystem("NKMDataSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
{
  val nkmData: ActorRef = system.actorOf(NKMData.props())

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An NKMData actor" must {
    "send HexMaps" in {
      within(500 millis) {
        implicit val timeout: Timeout = Timeout(500 millis)
        val future = nkmData ? GetHexMaps
        val hexMaps = Await.result(future.mapTo[List[HexMap]], 500 millis)
        assert(hexMaps.nonEmpty)
      }
    }
  }
}
