import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.tosware.NKM.Service
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class ApiSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with Service
{
  "API" must {
    "refuse incorrect login attempt" in {
      Post("/api/login") ~> Route.seal(routes) ~> check {
        status shouldEqual StatusCodes.BadRequest
      }
    }
  }
}
