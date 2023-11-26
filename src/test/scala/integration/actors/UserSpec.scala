package integration.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User.*
import com.tosware.nkm.models.user.{UserSettings, UserState, UserStateView}
import helpers.NkmPersistenceTestKit

class UserSpec extends NkmPersistenceTestKit(ActorSystem("UserSpec")) {
  "An User actor" must {
    "not be registered initially" in {
      val email = "test@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val state: UserState = aw(user ? GetState).asInstanceOf[UserState]
        state.email shouldEqual email
        state.isRegistered shouldEqual false
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
        state.isRegistered shouldEqual true
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
        loginCheckResponse shouldBe LoginSuccess(UserStateView(
          "test3@example.com",
          Some("test3@example.com"),
          isAdmin = false,
          UserSettings.default(),
        ))
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
        loginCheckResponse shouldBe LoginFailure("Invalid credentials.")
      }
    }

    "not be able to email without registering" in {
      val email = "test5@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = aw(loginCheckFuture.mapTo[LoginEvent])
        loginCheckResponse shouldBe LoginFailure("User does not exist.")
      }
    }

    "not be admin initially" in {
      val email = "test@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        val future = user ? GetState
        val state: UserState = aw(future.mapTo[UserState])
        state.isAdmin shouldEqual false
      }
    }

    "be admin after granting it" in {
      val email = "test@example.com"
      val user: ActorRef = system.actorOf(User.props(email))
      within2000 {
        user ! GrantAdmin
        val state: UserState = aw((user ? GetState).mapTo[UserState])
        state.isAdmin shouldEqual true
      }
    }
  }
}
