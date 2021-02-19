package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.actors.Lobby._
import com.tosware.NKM.models.LobbyState
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LobbySpec extends TestKit(ActorSystem("LobbySpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
{
//  TODO: cleanup of persistence
//  override def beforeEach(): Unit = {
//
//  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val timeout: Timeout = Timeout(500 millis)

  "An Lobby actor" must {
    "not be created initially" in {
      val lobby: ActorRef = system.actorOf(Lobby.props("test"))
      within(500 millis) {
        val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], 500 millis)
        state.created() shouldEqual false
      }

    }
  }

  "be able to create" in {
    val lobby: ActorRef = system.actorOf(Lobby.props("test1"))
    within(500 millis) {
      val testName = "test name"
      val createFuture = lobby ? Create(testName)
      val response = Await.result(createFuture.mapTo[Event], 500 millis)
      response shouldBe CreateSuccess

      val state: LobbyState = Await.result((lobby ? GetState).mapTo[LobbyState], 500 millis)
      state.created() shouldEqual true
      state.name.get shouldEqual testName
    }
  }

  "not be able to create more than once" in {
    val lobby: ActorRef = system.actorOf(Lobby.props("test2"))
    within(500 millis) {
      val testName = "test name2"
      val createCommand = Create(testName)
      Await.result((lobby ? createCommand).mapTo[Event], 500 millis) shouldBe CreateSuccess
      Await.result((lobby ? createCommand).mapTo[Event], 500 millis) shouldBe CreateFailure
      Await.result((lobby ? createCommand).mapTo[Event], 500 millis) shouldBe CreateFailure
      Await.result((lobby ? Create("otherName")).mapTo[Event], 500 millis) shouldBe CreateFailure
    }
  }
}
