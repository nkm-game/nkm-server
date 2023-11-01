package integration.api

import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Route
import helpers.ApiTrait

class GameApiSpec extends ApiTrait {
  "Game API" must {
    "return NotFound when state does not exist" in {
      Get("/api/state/does_not_exist") ~> Route.seal(routes) ~> check {
        status should not equal InternalServerError
        status shouldEqual NotFound
      }
    }
  }
}
