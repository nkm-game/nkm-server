package com.tosware.nkm.services

import akka.actor.ActorRef
import akka.pattern.ask
import com.tosware.nkm.actors.*
import com.tosware.nkm.*
import com.tosware.nkm.models.bugreport.BugReport

import scala.concurrent.Future

class BugReportService(bugReportActor: ActorRef) extends NkmTimeouts {
  import com.tosware.nkm.models.CommandResponse.*

  def fetchBugReports(): Future[Seq[BugReport]] =
    (bugReportActor ? BugReportActor.GetBugReports).mapTo[Seq[BugReport]]

  def create(creatorIdOpt: Option[UserId], description: String, gameId: Option[GameId]): Future[CommandResponse] =
    (bugReportActor ? BugReportActor.Create(creatorIdOpt, description, gameId)).mapTo[CommandResponse]

  def setResolved(id: BugReportId, resolved: Boolean): Future[CommandResponse] =
    (bugReportActor ? BugReportActor.SetResolved(id, resolved)).mapTo[CommandResponse]
}
