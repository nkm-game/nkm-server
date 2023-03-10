package helpers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.tosware.nkm._
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait NkmIntegrationTestTrait
  extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with NkmJsonProtocol
    with NkmTimeouts
    with SprayJsonSupport
    with Logging
{
  implicit val db: JdbcBackend.Database = Database.forConfig("slick.db")

  override def beforeAll(): Unit = {
    val config: Config = ConfigFactory.load()
    val dbName = config.getString("slick.db.dbName")
    DBManager.createDbIfNotExists(dbName)
  }

  // Clean up persistence before each test
  override def beforeEach(): Unit = {
    DBManager.dropAllTables(db)
    DBManager.createNeededTables(db)
    //    logger.info("FATAL: tables created")
  }
}
