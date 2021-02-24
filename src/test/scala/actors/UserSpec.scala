package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import com.tosware.NKM.models.UserState
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class UserSpec extends TestKit(ActorSystem("UserSpec"))
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

  "An User actor" must {
    "not be registered initially" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within(500 millis) {
        val future = user ? GetState
        val state: UserState = Await.result(future.mapTo[UserState], 500 millis)
        state.login shouldEqual "test"
        state.registered shouldEqual false
      }
    }
    "be able to register" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within(500 millis) {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val future = user ? GetState
        val state: UserState = Await.result(future.mapTo[UserState], 500 millis)

        state.login shouldEqual "test"
        state.registered shouldEqual true
      }
    }

    "not be able to register a second time" in {
      val user: ActorRef = system.actorOf(User.props("test2"))
      within(500 millis) {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val registerFuture2 = user ? Register("test@example.com","password")
        val response2 = Await.result(registerFuture2.mapTo[RegisterEvent], 500 millis)
        response2 shouldBe RegisterFailure
      }
    }

    "be able to login with correct credentials" in {
      val user: ActorRef = system.actorOf(User.props("test3"))
      within(500 millis) {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], 500 millis)
        loginCheckResponse shouldBe LoginSuccess
      }
    }

    "not be able to login with incorrect credentials" in {
      val user: ActorRef = system.actorOf(User.props("test4"))
      within(500 millis) {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password1")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], 500 millis)
        loginCheckResponse shouldBe LoginFailure
      }
    }

    "not be able to login without registering" in {
      val user: ActorRef = system.actorOf(User.props("test5"))
      within(500 millis) {
        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], 500 millis)
        loginCheckResponse shouldBe LoginFailure
      }
    }

    "be able to create a lobby" in {
      val user: ActorRef = system.actorOf(User.props("test6"))
      within(500 millis) {
        val registerFuture = user ? Register("test6@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val createLobbyFuture = user ? CreateLobby("test lobby name")
        val createLobbyResponse = Await.result(createLobbyFuture.mapTo[Event], 500 millis)
        createLobbyResponse match {
          case LobbyCreated(lobbyId) => println(s"Created lobby with id $lobbyId")
          case other => fail(other.toString)
        }
      }
    }
  }
}
