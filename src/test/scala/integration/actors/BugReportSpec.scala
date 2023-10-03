package integration.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.nkm.actors.BugReportActor
import com.tosware.nkm.actors.BugReportActor.*
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.models.bugreport.BugReport
import helpers.NkmPersistenceTestKit

class BugReportSpec extends NkmPersistenceTestKit(ActorSystem("BugReportSpec")) {
  "A bug report actor" must {
    "work and return bug reports" in {
      val actor: ActorRef = system.actorOf(BugReportActor.props())

      {
        val reports = aw(actor ? GetBugReports).asInstanceOf[Seq[BugReport]]
        reports should be(empty)
      }

      aw(actor ? Create(None, "nkm is bad", None)).asInstanceOf[CommandResponse] should be(Success())
      aw(actor ? Create(None, "pay the developers", None)).asInstanceOf[CommandResponse] should be(Success())

      var reportId = ""

      {
        val reports = aw(actor ? GetBugReports).asInstanceOf[Seq[BugReport]]
        reports(0).description should be("nkm is bad")
        reports(0).resolved should be(false)
        reports(1).resolved should be(false)

        reportId = reports(0).id
      }

      aw(actor ? SetResolved(reportId, resolved = true)).asInstanceOf[CommandResponse] should be(Success())

      {
        val reports = aw(actor ? GetBugReports).asInstanceOf[Seq[BugReport]]
        reports(0).description should be("nkm is bad")
        reports(0).resolved should be(true)
        reports(1).resolved should be(false)

        println(reports)
      }
    }
  }
}
