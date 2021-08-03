package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.actors.Lobby._
import com.tosware.NKM.models.lobby.LobbyState
import helpers.NKMPersistenceTestKit

import scala.concurrent.Await
import scala.language.postfixOps

class LobbySpec extends NKMPersistenceTestKit(ActorSystem("LobbySpec"))
{
  "An Lobby actor" must {
    "not be created initially" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test"))
      within2000 {
        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        state.created() shouldEqual false
      }

    }

    "be able to create" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test1"))
      val hostUserID = "test_user"
      within2000 {
        val testName = "test name"
        val createFuture = lobby ? Create(testName, hostUserID)
        val response = Await.result(createFuture, atMost)
        response shouldBe Lobby.Success

        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        state.created() shouldEqual true
        state.name.get shouldEqual testName
      }
    }

    "not be able to create more than once" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test2"))
      val hostUserID = "test_user"
      within2000 {
        val testName = "test name2"
        val createCommand = Create(testName, hostUserID)
        Await.result((lobby ? createCommand).mapTo[CommandResponse], atMost) shouldBe Success
        Await.result((lobby ? createCommand).mapTo[CommandResponse], atMost) shouldBe Failure
        Await.result((lobby ? createCommand).mapTo[CommandResponse], atMost) shouldBe Failure
        Await.result((lobby ? Create("otherName", hostUserID)).mapTo[CommandResponse], atMost) shouldBe Failure
      }
    }

    "be able to join created lobby" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test"))
      val hostUserID = "test_user"
      val joinerID = "test_user2"
      within2000 {
        val testName = "test name"
        val createFuture = lobby ? Create(testName, hostUserID)
        val response = Await.result(createFuture.mapTo[CommandResponse], atMost)
        response shouldBe Success

        val joinFuture = lobby ? UserJoin(joinerID)
        val joinResponse = Await.result(joinFuture.mapTo[CommandResponse], atMost)
        joinResponse shouldBe Success

        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        state.userIds shouldEqual List(hostUserID, joinerID)
      }
    }

    "be able to leave lobby" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test"))
      val hostUserID = "test_user"
      val joinerID = "test_user2"
      within2000 {
        val testName = "test name"
        val createFuture = lobby ? Create(testName, hostUserID)
        val response = Await.result(createFuture.mapTo[CommandResponse], atMost)
        response shouldBe Success

        val joinFuture = lobby ? UserJoin(joinerID)
        val joinResponse = Await.result(joinFuture.mapTo[CommandResponse], atMost)
        joinResponse shouldBe Success

        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        state.userIds shouldEqual List(hostUserID, joinerID)

        val leaveFuture = lobby ? UserLeave(joinerID)
        val leaveResponse = Await.result(leaveFuture.mapTo[CommandResponse], atMost)
        leaveResponse shouldBe Success

        val leaveState: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], atMost)
        leaveState.userIds shouldEqual List(hostUserID)
      }
    }
  }
}
