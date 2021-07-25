package api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.tosware.NKM.DBManager
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.models._
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}
import slick.jdbc.JdbcBackend.Database
import spray.json._

import scala.language.postfixOps
import scala.util.Success

class ApiSpec
  extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with HttpService
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with NKMJsonProtocol
    with SprayJsonSupport
{
  implicit val db = Database.forConfig("slick.db")
  implicit val userService: UserService = new UserService()
  implicit val lobbyService: LobbyService = new LobbyService()

  override def beforeAll(): Unit = {
    // spawn CQRS Event Handler
    system.actorOf(CQRSEventHandler.props(db))
  }

  // Clean up persistence before each test
  override def beforeEach(): Unit = {
    DBManager.dropAllTables(db)
    DBManager.createNeededTables(db)
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    db.close()
  }

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
      Post("/api/login", Credentials("username", "wrong_password")) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }
    }
    "allow registration" in {
      Post("/api/register", RegisterRequest("username", "username@example.com", "password")) ~> routes ~> check {
        status shouldEqual Created
      }
    }
    "return token when valid credentials are provided" in {
      Post("/api/register", RegisterRequest("test", "test@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test", "password")) ~> routes ~> check {
        status shouldBe OK

        val token = responseAs[String]
        JwtSprayJson.decode(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match {
          case Success(claim) => claim.content.parseJson.convertTo[JwtContent] shouldEqual JwtContent("test")
          case _ => fail
        }
      }
    }
    "disallow registration for taken username" in {
      Post("/api/register", RegisterRequest("test_user", "test_user@example.com", "password")) ~> routes
      Post("/api/register", RegisterRequest("test_user", "test_user2@example.com", "password")) ~> routes ~> check {
        status shouldEqual Conflict
      }
    }
    "disallow registration for taken email" in {
      Post("/api/register", RegisterRequest("test_user", "test_user@example.com", "password")) ~> routes
      Post("/api/register", RegisterRequest("test_user2", "test_user@example.com", "password")) ~> routes ~> check {
        status shouldEqual Conflict
      }
    }
    "allow creating lobby" in {
      var token: String = ""
      Post("/api/register", RegisterRequest("test", "test@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test", "password")) ~> routes ~> check {
        status shouldEqual OK
        token = responseAs[String]
      }
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(RawHeader("Authorization", s"Bearer $token")) ~> routes ~> check {
        status shouldEqual OK
      }
    }

    "disallow creating lobby with wrong token" in {
      var token: String = "random_token"
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(RawHeader("Authorization", s"Bearer $token")) ~> routes ~> check {
        status shouldEqual Unauthorized
      }
    }

    "allow getting created lobbies" in {
      Get("/api/lobbies") ~> routes ~> check {
        status shouldEqual OK
        val lobbies = responseAs[Seq[LobbyState]]
        lobbies.length shouldEqual 0
      }

      var token: String = ""
      Post("/api/register", RegisterRequest("test", "test@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test", "password")) ~> routes ~> check {
        status shouldEqual OK
        token = responseAs[String]
      }
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(RawHeader("Authorization", s"Bearer $token")) ~> routes ~> check {
        status shouldEqual OK
      }

      Get("/api/lobbies") ~> routes ~> check {
        status shouldEqual OK
        val lobbies = responseAs[Seq[LobbyState]]
        lobbies.length shouldEqual 1
      }
    }
  }
}
