package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.actors.Lobby._
import com.tosware.NKM.models.LobbyState
import helpers.NKMPersistenceTestKit

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LobbySpec extends NKMPersistenceTestKit(ActorSystem("LobbySpec"))
{
  "An Lobby actor" must {
    "not be created initially" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test"))
      within1000 {
        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        state.created() shouldEqual false
      }

    }

    "be able to create" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test1"))
      within1000 {
        val testName = "test name"
        val createFuture = lobby ? Create(testName)
        val response = Await.result(createFuture.mapTo[Event], atMost)
        response shouldBe CreateSuccess

        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        state.created() shouldEqual true
        state.name.get shouldEqual testName
      }
    }

    "not be able to create more than once" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test2"))
      within1000 {
        val testName = "test name2"
        val createCommand = Create(testName)
        Await.result((lobby ? createCommand).mapTo[Event], atMost) shouldBe CreateSuccess
        Await.result((lobby ? createCommand).mapTo[Event], atMost) shouldBe CreateFailure
        Await.result((lobby ? createCommand).mapTo[Event], atMost) shouldBe CreateFailure
        Await.result((lobby ? Create("otherName")).mapTo[Event], atMost) shouldBe CreateFailure
      }
    }
  }
}
