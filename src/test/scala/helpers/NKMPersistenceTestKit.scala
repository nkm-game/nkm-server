package helpers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.services.{LobbyService, NKMDataService, UserService}
import com.tosware.NKM.{DBManager, NKMTimeouts}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.duration._
import scala.language.postfixOps

class NKMPersistenceTestKit (_system: ActorSystem) extends TestKit(_system)
  with NKMTimeouts
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
{
  override def beforeAll(): Unit = {
    // spawn CQRS Event Handler
    system.actorOf(CQRSEventHandler.props(db))
  }
  implicit val db: JdbcBackend.Database = Database.forConfig("slick.db")
  implicit val NKMDataService: NKMDataService = new NKMDataService()
  implicit val userService: UserService = new UserService()
  implicit val lobbyService: LobbyService = new LobbyService()


  // Clean up persistence before each test
  override def beforeEach(): Unit = {
    DBManager.dropAllTables(db)
    DBManager.createNeededTables(db)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    db.close()
  }

  def within2000[T](f: => T): T = within(2000 millis)(f)

}
