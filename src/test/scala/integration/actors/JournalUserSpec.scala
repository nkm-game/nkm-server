package integration.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import helpers.NKMPersistenceTestKit

class JournalUserSpec extends NKMPersistenceTestKit(ActorSystem("UserSpec2")) {
  private val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  def createUser(username: String): Unit = {
    val user: ActorRef = system.actorOf(User.props(username))
    val registerFuture = user ? Register(s"$username@example.com", "password")
    val response = aw(registerFuture.mapTo[RegisterEvent])
    response shouldBe RegisterSuccess
  }

  "An User persistent query" must {
    "be able to read by persistence id" in {
      val username = "test1"
      createUser(username)

      val result = readJournal.
        currentEventsByPersistenceId(s"user-$username", 0, Long.MaxValue)
        .map(_.event).runWith(Sink.seq[Any])

      val seq = aw(result)
      seq.length shouldBe 1
      seq.head.asInstanceOf[RegisterSuccess].login shouldBe username
    }

    "be able to read by persistence tag" in {
      val username1 = "test1"
      val username2 = "test2"
      createUser(username1)
      createUser(username2)

      val result = readJournal.
        currentEventsByTag("register", 0)
        .map(_.event).runWith(Sink.seq[Any])

      val seq = aw(result)
      seq.length shouldBe 2
      seq.head.asInstanceOf[RegisterSuccess].login shouldBe username1
      seq(1).asInstanceOf[RegisterSuccess].login shouldBe username2
    }
  }
}
