package integration.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User._
import com.tosware.nkm.models.UserState
import helpers.NkmPersistenceTestKit


class UserSpec extends NkmPersistenceTestKit(ActorSystem("UserSpec"))
{
  "An User actor" must {
    "not be registered initially" in {
      val email = "test@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val future = user ? GetState
        val state: UserState = aw(future.mapTo[UserState])
        state.email shouldEqual email
        state.registered shouldEqual false
      }
    }
    "be able to register" in {
      val email = "test@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val registerFuture = user ? Register("password")
        val response = aw(registerFuture.mapTo[RegisterEvent])
        response shouldBe RegisterSuccess

        val future = user ? GetState
        val state: UserState = aw(future.mapTo[UserState])

        state.email shouldEqual "test@example.com"
        state.registered shouldEqual true
      }
    }

    "not be able to register a second time" in {
      val email = "test2@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val registerFuture = user ? Register("password")
        val response = aw(registerFuture.mapTo[RegisterEvent])
        response shouldBe RegisterSuccess

        val registerFuture2 = user ? Register("password")
        val response2 = aw(registerFuture2.mapTo[RegisterEvent])
        response2 shouldBe RegisterFailure
      }
    }

    "be able to email with correct credentials" in {
      val email = "test3@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val registerFuture = user ? Register("password")
        val response = aw(registerFuture.mapTo[RegisterEvent])
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = aw(loginCheckFuture.mapTo[LoginEvent])
        loginCheckResponse shouldBe LoginSuccess
      }
    }

    "not be able to email with incorrect credentials" in {
      val email = "test4@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
        within2000 {
        val registerFuture = user ? Register("password")
        val response = aw(registerFuture.mapTo[RegisterEvent])
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password1")
        val loginCheckResponse = aw(loginCheckFuture.mapTo[LoginEvent])
        loginCheckResponse shouldBe LoginFailure
      }
    }

    "not be able to email without registering" in {
      val email = "test5@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = aw(loginCheckFuture.mapTo[LoginEvent])
        loginCheckResponse shouldBe LoginFailure
      }
    }
  }
}
