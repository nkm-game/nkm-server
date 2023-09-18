package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{entity, *}
import com.tosware.nkm.models.CommandResponse
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.BugReportService
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import com.tosware.nkm.{BugReportId, GameId, Logging, NkmDependencies}

object BugReportRequest {
  case class Create(description: String, gameId: Option[GameId])

  case class SetResolved(id: BugReportId, resolved: Boolean)
}

class BugReportRoutes(deps: NkmDependencies) extends JwtDirective
  with SprayJsonSupport
  with Logging
  with NkmJsonProtocol {
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val bugReportService: BugReportService = deps.bugReportService

  val bugReportPrefix = "bug_reports"

  val bugReportGetRoutes = concat(
    pathPrefix(bugReportPrefix) {
      requireAdmin {
        path("fetch") {
          complete(bugReportService.fetchBugReports())
        }
      }
    }
  )


  val bugReportPostRoutes = concat(
    pathPrefix(bugReportPrefix) {
      concat(
        path("create") {
          authenticatedOpt { userStateViewOpt =>
            entity(as[BugReportRequest.Create]) { entity =>
              val creationResponseFuture = bugReportService.create(userStateViewOpt.flatMap(_.userId), entity.description, entity.gameId)
              onSuccess(creationResponseFuture) {
                case CommandResponse.Success(_) => complete(StatusCodes.Created)
                case CommandResponse.Failure(msg) => complete(StatusCodes.InternalServerError -> msg)
              }
            }
          }
        },
        path("set_resolved") {
          requireAdmin {
              entity(as[BugReportRequest.SetResolved]) { entity =>
                val creationResponseFuture = bugReportService.setResolved(entity.id, entity.resolved)
                onSuccess(creationResponseFuture) {
                  case CommandResponse.Success(_) => complete(StatusCodes.OK)
                  case CommandResponse.Failure(msg) => complete(StatusCodes.InternalServerError -> msg)
                }
              }
          }
        }
      )
    }
  )
}
