package helpers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.services.{GameService, LobbyService, NKMDataService, UserService}
import com.tosware.NKM.{DBManager, NKMTimeouts}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.duration._
import scala.language.postfixOps

class NKMPersistenceTestKit (_system: ActorSystem) extends TestKit(_system)
  with NKMTestTrait
  with ImplicitSender
  with AnyWordSpecLike
{
  implicit val NKMDataService: NKMDataService = new NKMDataService()
  implicit val userService: UserService = new UserService()
  implicit val lobbyService: LobbyService = new LobbyService()
  implicit val gameService: GameService = new GameService()
  def within2000[T](f: => T): T = within(2000 millis)(f)

  override def beforeAll(): Unit = {
    super.beforeAll()
    // spawn CQRS Event Handler
    system.actorOf(CQRSEventHandler.props(db))
  }
}
