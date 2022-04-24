package unit.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import com.tosware.NKM.models.UserState
import helpers.NKMPersistenceTestKit

import scala.concurrent.Await
import scala.language.postfixOps


class UserSpec extends NKMPersistenceTestKit(ActorSystem("UserSpec"))
{
  "An User actor" must {
    "not be registered initially" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within2000 {
        val future = user ? GetState
        val state: UserState = Await.result(future.mapTo[UserState], atMost)
        state.login shouldEqual "test"
        state.registered shouldEqual false
      }
    }
    "be able to register" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within2000 {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
        response shouldBe RegisterSuccess

        val future = user ? GetState
        val state: UserState = Await.result(future.mapTo[UserState], atMost)

        state.login shouldEqual "test"
        state.registered shouldEqual true
      }
    }

    "not be able to register a second time" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within2000 {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
        response shouldBe RegisterSuccess

        val registerFuture2 = user ? Register("test@example.com","password")
        val response2 = Await.result(registerFuture2.mapTo[RegisterEvent], atMost)
        response2 shouldBe RegisterFailure
      }
    }

    "be able to login with correct credentials" in {
      val user: ActorRef = system.actorOf(User.props("test3"))
      within2000 {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], atMost)
        loginCheckResponse shouldBe LoginSuccess
      }
    }

    "not be able to login with incorrect credentials" in {
      val user: ActorRef = system.actorOf(User.props("test4"))
        within2000 {
        val registerFuture = user ? Register("test@example.com","password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password1")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], atMost)
        loginCheckResponse shouldBe LoginFailure
      }
    }

    "not be able to login without registering" in {
      val user: ActorRef = system.actorOf(User.props("test5"))
      within2000 {
        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], atMost)
        loginCheckResponse shouldBe LoginFailure
      }
    }

//    "be able to create a lobby" in {
//      val user: ActorRef = system.actorOf(User.props("test6"))
//      within2000 {
//        val registerFuture = user ? Register("test6@example.com","password")
//        val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
//        response shouldBe RegisterSuccess
//
//        val createLobbyFuture = user ? CreateLobby("test lobby name")
//        val createLobbyResponse = Await.result(createLobbyFuture.mapTo[Event], atMost)
//        createLobbyResponse match {
//          case LobbyCreated(lobbyId) => println(s"Created lobby with id $lobbyId")
//          case other => fail(other.toString)
//        }
//      }
//    }
  }
}
