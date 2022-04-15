package helpers

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.testkit.TestKit
import com.tosware.NKM.{DBManager, NKMTimeouts}
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.{GameService, LobbyService, NKMDataService, UserService}
import com.tosware.NKM.services.http.directives.JwtSecretKey
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait NKMTestTrait
  extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with NKMJsonProtocol
    with NKMTimeouts
    with SprayJsonSupport {
  implicit val db: JdbcBackend.Database = Database.forConfig("slick.db")
  implicit val jwtSecretKey: JwtSecretKey = JwtSecretKey("jwt_test_key")

  override def beforeAll(): Unit = {
    val config: Config = ConfigFactory.load()
    val dbName = config.getString("slick.db.dbName")
    DBManager.createDbIfNotExists(dbName)
  }

  // Clean up persistence before each test
  override def beforeEach(): Unit = {
    DBManager.dropAllTables(db)
    DBManager.createNeededTables(db)
  }

  override def afterAll(): Unit = {
    db.close()
  }
}
