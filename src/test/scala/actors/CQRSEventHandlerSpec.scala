package actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.DBManager
import com.tosware.NKM.actors.User._
import com.tosware.NKM.actors.{CQRSEventHandler, User}
import com.tosware.NKM.models.UserState
import helpers.NKMPersistenceTestKit
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.language.postfixOps


class CQRSEventHandlerSpec extends NKMPersistenceTestKit(ActorSystem("CQRSEventHandlerSpec"))
{
  "A CQRSEventHandler actor" must {
    "persist register request in a relational database" in {
      val username = "test"
      val email = s"$username@example.com"
      val password = "password"
      system.actorOf(CQRSEventHandler.props(db))
      val user: ActorRef = system.actorOf(User.props(username))
      within2000 {
        val registerFuture = user ? Register(email,password)
        val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
        response shouldBe RegisterSuccess


        import system.dispatcher

        db.run(DBManager.users.result).map(_.foreach {
          case UserState(login, emailResult, _) =>
            login shouldBe username
            emailResult shouldBe email
          }
        )
      }
    }

  }
}
