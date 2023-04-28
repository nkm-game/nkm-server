package integration.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User._
import helpers.NkmPersistenceTestKit

class JournalUserSpec extends NkmPersistenceTestKit(ActorSystem("UserSpec2")) {
  private val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  def createUser(email: String): Unit = {
    val user: ActorRef = system.actorOf(User.props(email))
    val registerFuture = user ? Register("password")
    val response = aw(registerFuture.mapTo[RegisterEvent])
    response shouldBe RegisterSuccess
  }

  "An User persistent query" must {
    "be able to read by persistence id" in {
      val email = "test1@example.com"
      createUser(email)

      val result = readJournal.
        currentEventsByPersistenceId(s"user-$email", 0, Long.MaxValue)
        .map(_.event).runWith(Sink.seq[Any])

      val seq = aw(result)
      seq.length shouldBe 1
      seq.head.asInstanceOf[RegisterSuccess].email shouldBe email
    }

    "be able to read by persistence tag" in {
      val email1 = "test1@example.com"
      val email2 = "test2@example.com"
      createUser(email1)
      createUser(email2)

      val result = readJournal.
        currentEventsByTag(User.registerTag, 0)
        .map(_.event).runWith(Sink.seq[Any])

      val seq = aw(result)
      seq.length shouldBe 2
      seq.head.asInstanceOf[RegisterSuccess].email shouldBe email1
      seq(1).asInstanceOf[RegisterSuccess].email shouldBe email2
    }
  }
}
