package com.tosware.nkm.models.bugreport

import com.tosware.nkm.{BugReportId, GameId, UserId}

import java.time.ZonedDateTime

case class BugReport(
  id: BugReportId,
  creatorIdOpt: Option[UserId],
  description: String,
  gameId: Option[GameId],
  creationDate: ZonedDateTime = ZonedDateTime.now(),
  resolved: Boolean = false
)
