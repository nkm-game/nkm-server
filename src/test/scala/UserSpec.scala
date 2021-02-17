import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.testkit.scaladsl.PersistenceTestKit
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import com.tosware.NKM.models.UserState
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

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

  "An User actor" must {
    "not be registered initially" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within(500 millis) {
        implicit val timeout: Timeout = Timeout(500 millis)
        val future = user ? GetState
        val state: UserState = Await.result(future.mapTo[UserState], 500 millis)
        state.login shouldEqual "test"
        state.registered shouldEqual false
      }
    }
    "be able to register" in {
      val user: ActorRef = system.actorOf(User.props("test"))
      within(500 millis) {
        implicit val timeout: Timeout = Timeout(500 millis)
        val registerFuture = user ? Register("password")
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
        implicit val timeout: Timeout = Timeout(500 millis)

        val registerFuture = user ? Register("password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val registerFuture2 = user ? Register("password")
        val response2 = Await.result(registerFuture2.mapTo[RegisterEvent], 500 millis)
        response2 shouldBe RegisterFailure
      }
    }

    "can login with correct credentials" in {
      val user: ActorRef = system.actorOf(User.props("test3"))
      within(500 millis) {
        implicit val timeout: Timeout = Timeout(500 millis)
        val registerFuture = user ? Register("password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], 500 millis)
        loginCheckResponse shouldBe LoginSuccess
      }
    }

    "cannot login with incorrect credentials" in {
      val user: ActorRef = system.actorOf(User.props("test4"))
      within(500 millis) {
        implicit val timeout: Timeout = Timeout(500 millis)
        val registerFuture = user ? Register("password")
        val response = Await.result(registerFuture.mapTo[RegisterEvent], 500 millis)
        response shouldBe RegisterSuccess

        val loginCheckFuture = user ? CheckLogin("password1")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], 500 millis)
        loginCheckResponse shouldBe LoginFailure
      }
    }

    "cannot login without registering" in {
      val user: ActorRef = system.actorOf(User.props("test5"))
      within(500 millis) {
        implicit val timeout: Timeout = Timeout(500 millis)
        val loginCheckFuture = user ? CheckLogin("password")
        val loginCheckResponse = Await.result(loginCheckFuture.mapTo[LoginEvent], 500 millis)
        loginCheckResponse shouldBe LoginFailure
      }
    }
  }
}
