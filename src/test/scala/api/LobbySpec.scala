package api

import akka.http.scaladsl.model.StatusCodes._
import com.tosware.NKM.DBManager
import com.tosware.NKM.models.lobby._
import helpers.LobbyApiTrait

import scala.language.postfixOps

class LobbySpec extends LobbyApiTrait
{
  "API" must {
    "allow joining and leaving lobbies" in {

      // this request should fail as it is not a lobby id, but lobby name
      Post("/api/join_lobby", LobbyJoinRequest(lobbyName)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual InternalServerError
      }

      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List(usernames(0), usernames(1))
      }

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List(usernames(0))
      }

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
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
      val hexMapName = "Linia"
      val hexMapName2 = "1v1v1"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual OK
      }

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.chosenHexMapName shouldEqual Some(hexMapName)
      }

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName2)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
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
      val hexMapName = "this map does not exist"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual InternalServerError
      }
    }

    "disallow setting map name in a lobby for non host persons" in {
      val hexMapName = "Linia"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual InternalServerError
      }
    }

    "allow to start a game" in {
      val hexMapName = "Linia"
      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual OK
      }

      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual OK
      }

      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual OK
      }
    }

    "disallow starting game without hexmap" in {
      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual OK
      }

      Post("/api/start_game", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual InternalServerError
      }
    }

    "disallow starting game with 1 or 0 players" in {
      val hexMapName = "Linia"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        status shouldEqual InternalServerError
      }
    }
  }
}
