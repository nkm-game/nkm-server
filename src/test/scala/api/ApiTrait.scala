package api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.tosware.NKM.DBManager
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.{HttpService, LobbyService, UserService}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait ApiTrait
    extends AnyWordSpec
      with Matchers
      with ScalatestRouteTest
      with HttpService
      with BeforeAndAfterAll
      with BeforeAndAfterEach
      with NKMJsonProtocol
      with SprayJsonSupport
  {
    implicit val db: JdbcBackend.Database = Database.forConfig("slick.db")
    implicit val userService: UserService = new UserService()
    implicit val lobbyService: LobbyService = new LobbyService()

    override def beforeAll(): Unit = {
      // spawn CQRS Event Handler
      system.actorOf(CQRSEventHandler.props(db))
    }

    // Clean up persistence before each test
    override def beforeEach(): Unit = {
      DBManager.dropAllTables(db)
      DBManager.createNeededTables(db)
    }

    override def afterAll(): Unit = {
      TestKit.shutdownActorSystem(system)
      db.close()
    }
}
