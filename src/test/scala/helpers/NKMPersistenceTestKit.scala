package helpers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.NKM.NKMTimeouts
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
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
  def recreateJournal = DBIO.seq(
    sqlu"""DROP TABLE IF EXISTS journal""",
    sqlu"""
    CREATE TABLE IF NOT EXISTS journal (
      ordering SERIAL,
      persistence_id VARCHAR(255) NOT NULL,
      sequence_number BIGINT NOT NULL,
      deleted BOOLEAN DEFAULT FALSE,
      tags VARCHAR(255) DEFAULT NULL,
      message BLOB NOT NULL,
      PRIMARY KEY(persistence_id, sequence_number)
    )
    """,
    sqlu"""
    CREATE UNIQUE INDEX journal_ordering_idx ON journal(ordering);
    """,
  )
  def recreateSnapshot = DBIO.seq(
    sqlu"""DROP TABLE IF EXISTS snapshot""",
    sqlu"""
        CREATE TABLE IF NOT EXISTS snapshot (
          persistence_id VARCHAR(255) NOT NULL,
          sequence_number BIGINT NOT NULL,
          created BIGINT NOT NULL,
          snapshot BLOB NOT NULL,
          PRIMARY KEY (persistence_id, sequence_number)
        )
      """,
  )
  val db = Database.forConfig("slick.db")

  // Clean up persistence before each test
  override def beforeEach(): Unit = {
    val setupAction: DBIO[Unit] = DBIO.seq(
      recreateJournal,
      recreateSnapshot,
    )
    val setupFuture = db.run(setupAction)
    Await.result(setupFuture, 5000 millis)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def within1000[T](f: => T): T = within(1000 millis)(f)

}
