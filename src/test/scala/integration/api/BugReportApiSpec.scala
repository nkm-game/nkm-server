package integration.api

import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Route
import com.tosware.nkm.models.bugreport.BugReport
import com.tosware.nkm.services.http.routes.BugReportRequest
import helpers.UserApiTrait

class BugReportApiSpec extends UserApiTrait {
  def fetchBugReports(): Seq[BugReport] =
    Get("/api/bug_reports/fetch").addAuthHeader(adminToken)
      ~> Route.seal(routes) ~> check {
        status shouldEqual OK
        responseAs[Seq[BugReport]]
      }

  "Bug Report API" must {
    "disallow non admins to fetch bug reports" in {
      Get("/api/bug_reports/fetch") ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
        responseAs[String] should be("Authentication is possible but has failed or not yet been provided.")
      }

      Get("/api/bug_reports/fetch").addAuthHeader(0) ~> Route.seal(
        routes
      ) ~> check {
        status shouldEqual Forbidden
        responseAs[String] should be("The supplied authentication is not authorized to access this resource")
      }
    }

    "allow admins to fetch bug reports" in {
      Get("/api/bug_reports/fetch").addAuthHeader(adminToken)
        ~> Route.seal(routes) ~> check {
          status shouldEqual OK
          responseAs[Seq[BugReport]] should be(Seq.empty)
        }
    }
    "allow bug report creation for logged in users" in {
      Post("/api/bug_reports/create", BugReportRequest.Create("nkm is bad", None))
        .addAuthHeader(0) ~> Route.seal(routes) ~> check {
        status shouldEqual Created
      }

      val reports = fetchBugReports()

      reports.size should be(1)
      reports.head.description should be("nkm is bad")
      reports.head.creatorIdOpt should be(Some(emails(0)))
    }

    "allow bug report creation for anonymous users" in {
      Post("/api/bug_reports/create", BugReportRequest.Create("nkm is bad", None)) ~> Route.seal(routes) ~> check {
        status shouldEqual Created
      }

      val reports = fetchBugReports()

      reports.size should be(1)
      reports.head.description should be("nkm is bad")
      reports.head.creatorIdOpt should be(None)
    }

    "disallow non admins to mark bug report as resolved" in {
      Post("/api/bug_reports/create", BugReportRequest.Create("nkm is bad", None)) ~> Route.seal(routes) ~> check {
        status shouldEqual Created
      }

      val reports = fetchBugReports()
      reports.head.resolved should be(false)

      Post(
        "/api/bug_reports/set_resolved",
        BugReportRequest.SetResolved(reports.head.id, resolved = true),
      ) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }

      Post("/api/bug_reports/set_resolved", BugReportRequest.SetResolved(reports.head.id, resolved = true))
        .addAuthHeader(0)
        ~> Route.seal(routes) ~> check {
          status shouldEqual Forbidden
        }

      fetchBugReports().head.resolved should be(false)
    }

    "allow admins to mark bug report as resolved" in {
      Post("/api/bug_reports/create", BugReportRequest.Create("nkm is bad", None)) ~> Route.seal(routes) ~> check {
        status shouldEqual Created
      }

      val reports = fetchBugReports()
      reports.head.resolved should be(false)

      Post("/api/bug_reports/set_resolved", BugReportRequest.SetResolved(reports.head.id, resolved = true))
        .addAuthHeader(adminToken)
        ~> Route.seal(routes) ~> check {
          status shouldEqual OK
        }

      fetchBugReports().head.resolved should be(true)
    }
  }
}
