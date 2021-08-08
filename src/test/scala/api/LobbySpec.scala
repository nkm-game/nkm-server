package api

import akka.http.scaladsl.model.StatusCodes._
import com.tosware.NKM.DBManager
import com.tosware.NKM.models.game.PickType.AllRandom
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.lobby._
import helpers.LobbyApiTrait

import scala.language.postfixOps

class LobbySpec extends LobbyApiTrait
{
  "API" must {
    "allow joining and leaving lobbies" in {

      // this request should fail as it is not a lobby id, but lobby name
      Post("/api/join_lobby", LobbyJoinRequest(lobbyName)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)

      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List(usernames(0), usernames(1))
      }

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List(usernames(0))
      }

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

      // wait for CQRS Event Handler to persist
      Thread.sleep(DBManager.dbTimeout.toMillis)

      Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
        status shouldEqual OK
        val lobby = responseAs[LobbyState]
        lobby.userIds shouldEqual List()
      }
    }

    "disallow joining a lobby you are already in" in {
      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)

      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
    }

    "disallow leaving a lobby you are already in" in {
      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)

      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)

    }

    // TODO: move this somewhere else
    "allow inspecting empty game state" in {
      Get("/api/state/random_id") ~> routes ~> check {
        status shouldEqual OK
        val gameState = responseAs[GameState]
        gameState.gamePhase shouldEqual GamePhase.NotStarted
        gameState.players.length shouldEqual 0
      }
    }


    "allow setting map name in a lobby for a host" in {
      val hexMapNames = List("Linia", "1v1v1")

      hexMapNames.foreach { hexMapName =>
        Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

        // wait for CQRS Event Handler to persist
        Thread.sleep(DBManager.dbTimeout.toMillis)

        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
          status shouldEqual OK
          val lobby = responseAs[LobbyState]
          lobby.chosenHexMapName shouldEqual Some(hexMapName)
        }
      }
    }

    "disallow setting invalid map name" in {
      val hexMapName = "this map does not exist"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
    }

    "allow setting pick type in a lobby for a host" in {
      val pickTypes = PickType.values

      pickTypes.foreach { p =>
        Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, p)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

        // wait for CQRS Event Handler to persist
        Thread.sleep(DBManager.dbTimeout.toMillis)

        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
          status shouldEqual OK
          val lobby = responseAs[LobbyState]
          lobby.pickType shouldEqual p
        }
      }
    }

    "allow setting number of bans in a lobby for a host" in {
      val numberOfBansList = List(5,3,2,1,0,3)

      numberOfBansList.foreach { n =>
        Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

        // wait for CQRS Event Handler to persist
        Thread.sleep(DBManager.dbTimeout.toMillis)

        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
          status shouldEqual OK
          val lobby = responseAs[LobbyState]
          lobby.numberOfBans shouldEqual n
        }
      }
    }

    "disallow setting invalid number of bans in a lobby for a host" in {
      val numberOfBansList = List(-1, -4, -234)

      numberOfBansList.foreach { n =>
        Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
      }
    }

    "allow setting number of characters in a lobby for a host" in {
      val numberOfCharactersList = List(5,3,2,3,8)

      numberOfCharactersList.foreach { n =>
        Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

        // wait for CQRS Event Handler to persist
        Thread.sleep(DBManager.dbTimeout.toMillis)

        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
          status shouldEqual OK
          val lobby = responseAs[LobbyState]
          lobby.numberOfCharactersPerPlayer shouldEqual n
        }
      }
    }

    "disallow setting invalid number of characters in a lobby for a host" in {
      val numberOfCharactersList = List(-1, 0, -234)

      numberOfCharactersList.foreach { n =>
        Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
      }
    }

    "allow to start a game" in {
      val hexMapName = "Linia"
      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)

      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

      //TODO: check more in state
      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual OK
        val gameState = responseAs[GameState]
        gameState.gamePhase shouldEqual GamePhase.CharacterPick
        gameState.players.length shouldEqual 2
      }
    }

    "disallow starting game without hexmap" in {
      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
    }

    "disallow starting game with 1 or 0 players" in {
      val hexMapName = "Linia"

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
    }


    "disallow setting stuff in a lobby for non host persons" in {
      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, "Linia")) ~> routes ~> check(status shouldEqual Unauthorized)
      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, AllRandom)) ~> routes ~> check(status shouldEqual Unauthorized)
      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, 3)) ~> routes ~> check(status shouldEqual Unauthorized)
      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, 4)) ~> routes ~> check(status shouldEqual Unauthorized)

      Post("/api/set_hexmap", SetHexmapNameRequest(lobbyId, "Linia")).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, AllRandom)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, 3)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, 4)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
    }
  }
}
