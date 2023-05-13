package integration.api

import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Route
import com.tosware.nkm.models.*
import helpers.ApiTrait
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}
import spray.json.*

import scala.util.Success

class AuthSpec extends ApiTrait
{
  "API" must {
    "refuse incorrect register attempt" in {
      Post("/api/register") ~> Route.seal(routes) ~> check {
        status shouldEqual BadRequest
      }
    }
    "refuse incorrect login attempt" in {
      Post("/api/login") ~> Route.seal(routes) ~> check {
        status shouldEqual BadRequest
      }
    }
    "refuse invalid credentials" in {
      Post("/api/login", Credentials("userIdOpt@example.com", "wrong_password")) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }
    }
    "allow registration" in {
      Post("/api/register", RegisterRequest("userIdOpt@example.com", "password")) ~> routes ~> check {
        status shouldEqual Created
      }
    }
    "return token when valid credentials are provided" in {
      Post("/api/register", RegisterRequest("test@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test@example.com", "password")) ~> routes ~> check {
        status shouldBe OK

        val token = responseAs[String]
        JwtSprayJson.decode(token, deps.jwtSecretKey.value, Seq(JwtAlgorithm.HS256)) match {
          case Success(claim) => claim.content.parseJson.convertTo[JwtContent] shouldEqual JwtContent("test@example.com")
          case _ => fail()
        }
      }
    }
    "disallow registration for taken login" in {
      Post("/api/register", RegisterRequest("test_user@example.com", "password")) ~> routes
      Post("/api/register", RegisterRequest("test_user@example.com", "password")) ~> routes ~> check {
        status shouldEqual Conflict
      }
    }
  }
}
