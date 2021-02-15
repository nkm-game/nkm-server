import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.tosware.NKM.Service
import com.tosware.NKM.models.{Credentials, JwtContent, JwtToken}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}

import scala.language.postfixOps
import scala.util.Success
import spray.json._

class ApiSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with Service
{
  "API" must {
    "refuse incorrect login attempt" in {
      Post("/api/login") ~> Route.seal(routes) ~> check {
        status shouldEqual BadRequest
      }
    }
    "refuse invalid credentials" in {
      Post("/api/login", Credentials("username", "wrong_password")) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }
    }
    "return token when valid credentials are provided" in {
      Post("/api/login", Credentials("tojatos", "password")) ~> Route.seal(routes) ~> check {
        status shouldBe OK

        val token = responseAs[String]
        JwtSprayJson.decode(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match {
          case Success(claim) => claim.content.parseJson.convertTo[JwtContent] shouldEqual JwtContent("tojatos")
          case _ => fail
        }
      }
    }
  }
}
