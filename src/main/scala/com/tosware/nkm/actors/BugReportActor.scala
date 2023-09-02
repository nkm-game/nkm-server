package com.tosware.nkm.actors

import akka.actor.{ActorLogging, Props}
import akka.event.LoggingAdapter
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.nkm.*
import com.tosware.nkm.models.BugReport
import com.tosware.nkm.models.CommandResponse.*

import java.time.ZonedDateTime
import scala.util.Random

object BugReportActor {
  sealed trait Query

  case object GetBugReports extends Query

  sealed trait Command

  case class Create(creatorIdOpt: Option[UserId], description: String, gameId: Option[GameId]) extends Command

  case class SetResolved(id: BugReportId, resolved: Boolean) extends Command

  sealed trait Event {
    val id: BugReportId
  }

  case class Created(id: BugReportId, creatorIdOpt: Option[UserId], description: String, gameId: Option[GameId], creationDate: ZonedDateTime) extends Event

  case class ResolvedSet(id: BugReportId, resolved: Boolean) extends Event

  def props(): Props = Props(new BugReportActor)
}

class BugReportActor
  extends PersistentActor
    with ActorLogging
    with NkmTimeouts {

  import BugReportActor.*

  override def persistenceId: String = s"bug-report"

  override def log: LoggingAdapter = {
    akka.event.Logging(context.system, s"${this.getClass}($persistenceId)")
  }

  implicit val random: Random = new Random(persistenceId.hashCode)

  private var bugReports: Seq[BugReport] = Seq.empty

  def create(id: BugReportId, creatorIdOpt: Option[UserId], description: String, gameId: Option[GameId], creationDate: ZonedDateTime): Unit = {
    val newReport = BugReport(id, creatorIdOpt, description, gameId, creationDate)
    bugReports = bugReports.appended(newReport)
  }

  def setResolved(id: BugReportId, resolved: Boolean) = {
    bugReports = bugReports.collect {
      case bugReport if bugReport.id == id => bugReport.copy(resolved = resolved)
      case bugReport => bugReport
    }
  }

  override def receive: Receive = {
    case GetBugReports =>
      sender() ! bugReports
    case Create(creatorIdOpt: Option[UserId], description: String, gameId: Option[GameId]) =>
      val id = randomUUID()
      val creationDate = ZonedDateTime.now()
      val e = Created(id, creatorIdOpt, description, gameId, creationDate)
      persist(e) { _ =>
        create(id, creatorIdOpt, description, gameId, creationDate)
        log.debug(s"Created bug report with id $id")
        sender() ! Success()
      }
    case SetResolved(id, resolved) =>
      val e = ResolvedSet(id, resolved)
      persist(e) { _ =>
        setResolved(id, resolved)
        log.debug(s"Set resolved bug report with id $id to $resolved")
        sender() ! Success()
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case Created(id, creatorIdOpt, description, gameId, creationDate) =>
      create(id, creatorIdOpt, description, gameId, creationDate)
      log.debug(s"Recovered create")
    case ResolvedSet(id, resolved) =>
      setResolved(id, resolved)
      log.debug(s"Recovered resolved set")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}