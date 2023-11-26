package integration.api

import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Route
import com.tosware.nkm.models.*
import com.tosware.nkm.models.user.response.AuthResponse
import com.tosware.nkm.models.user.{UserSettings, UserStateView}
import com.tosware.nkm.services.http.routes.UserRequest
import helpers.ApiTrait
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}
import spray.json.*

import scala.util.Success

class UserAuthApiSpec extends ApiTrait {
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
      Post("/api/login", UserRequest.Login("userIdOpt@example.com", "wrong_password")) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }
    }
    "allow registration" in {
      Post("/api/register", UserRequest.Register("userIdOpt@example.com", "password")) ~> routes ~> check {
        status shouldEqual Created
      }
    }
    "return user state and token when valid credentials are provided" in {
      Post("/api/register", UserRequest.Register("test@example.com", "password")) ~> routes
      Post("/api/login", UserRequest.Login("test@example.com", "password")) ~> routes ~> check {
        status shouldBe OK

        val authResponse = responseAs[AuthResponse]
        val expectedUserState =
          UserStateView("test@example.com", Some("test@example.com"), isAdmin = false, UserSettings.default())

        authResponse.userState shouldEqual expectedUserState

        JwtSprayJson.decode(authResponse.token, deps.jwtSecretKey.value, Seq(JwtAlgorithm.HS256)) match {
          case Success(claim) =>
            claim.content.parseJson.convertTo[JwtContent] shouldEqual JwtContent(expectedUserState.toJson.toString)
          case _ => fail()
        }
      }
    }
    "disallow registration for taken login" in {
      Post("/api/register", UserRequest.Login("test_user@example.com", "password")) ~> routes
      Post("/api/register", UserRequest.Register("test_user@example.com", "password")) ~> routes ~> check {
        status shouldEqual Conflict
      }
    }
  }
}
