package api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.RawHeader
import com.tosware.NKM.DBManager
import com.tosware.NKM.models._
import com.tosware.NKM.models.lobby._

import scala.language.postfixOps

class ApiSpec extends UserApiTrait
{
  "API" must {
    "allow creating lobby" in {
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Created
      }
    }

    "disallow creating lobby with wrong token" in {
      val token: String = "random_token"
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Unauthorized
      }
    }

    "allow getting created lobbies" in {
      Get("/api/lobbies") ~> routes ~> check {
        status shouldEqual OK
        val lobbies = responseAs[Seq[LobbyState]]
        lobbies.length shouldEqual 0
      }

      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Created
      }

      Get("/api/lobbies") ~> routes ~> check {
        status shouldEqual OK
        val lobbies = responseAs[Seq[LobbyState]]
        lobbies.length shouldEqual 1
      }
    }

    "allow joining and leaving lobbies" in {
      var token: String = ""
      var token2: String = ""
      var lobbyId: String = ""
      Post("/api/register", RegisterRequest("test", "test@example.com", "password")) ~> routes
      Post("/api/register", RegisterRequest("test2", "test2@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test", "password")) ~> routes ~> check {
        status shouldEqual OK
        token = responseAs[String]
      }

      Post("/api/login", Credentials("test2", "password")) ~> routes ~> check {
        status shouldEqual OK
        token2 = responseAs[String]
      }

      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Created
        lobbyId = responseAs[String]
      }

      // this request should fail as it is not a lobby id, but lobby name
      Post("/api/join_lobby", LobbyJoinRequest("lobby_name")).addHeader(getAuthHeader(token2)) ~> routes ~> check {
        status shouldEqual InternalServerError
      }

      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(token2)) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List("test", "test2")
      }

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(token2)) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List("test")
      }

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List()
      }
    }

    "allow setting map name in a lobby for a host" in {
      var lobbyId: String = ""
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Created
        lobbyId = responseAs[String]
      }
      val hexMapName = "Linia"
      val hexMapName2 = "1v1v1"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.chosenHexMapName shouldEqual Some(hexMapName)
      }

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName2)).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.chosenHexMapName shouldEqual Some(hexMapName2)
      }
    }

    "disallow setting invalid map name" in {
      var token: String = ""
      var lobbyId: String = ""
      Post("/api/register", RegisterRequest("test", "test@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test", "password")) ~> routes ~> check {
        status shouldEqual OK
        token = responseAs[String]
      }
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Created
        lobbyId = responseAs[String]
      }
      val hexMapName = "this map does not exist"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual InternalServerError
      }
    }

    "disallow setting map name in a lobby for non host persons" in {
      var token: String = ""
      var token2: String = ""
      var lobbyId: String = ""
      Post("/api/register", RegisterRequest("test", "test@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test", "password")) ~> routes ~> check {
        status shouldEqual OK
        token = responseAs[String]
      }

      Post("/api/register", RegisterRequest("test2", "test2@example.com", "password")) ~> routes
      Post("/api/login", Credentials("test2", "password")) ~> routes ~> check {
        status shouldEqual OK
        token2 = responseAs[String]
      }
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Created
        lobbyId = responseAs[String]
      }
      val hexMapName = "Linia"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(token2)) ~> routes ~> check {
        status shouldEqual InternalServerError
      }
    }
  }
}
