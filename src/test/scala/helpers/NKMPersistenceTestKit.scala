package helpers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.NKM.{DBManager, NKMTimeouts}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
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
  val db = Database.forConfig("slick.db")

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
