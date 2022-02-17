package ws

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.DBManager
import com.tosware.NKM.actors.WebsocketUser.{WebsocketLobbyRequest, WebsocketLobbyResponse}
import com.tosware.NKM.models.lobby.{AuthRequest, GetLobbyRequest, LobbyCreationRequest, LobbyJoinRequest, LobbyLeaveRequest, LobbyState}
import com.tosware.NKM.services.http.routes.LobbyRoute
import helpers.{ApiTrait, UserApiTrait}
import spray.json._

class WSLobbySpec extends UserApiTrait
{
  val wsPrefix = "/ws"
  val wsUri = s"$wsPrefix/lobby"

  def sendRequest(request: WebsocketLobbyRequest)(implicit wsClient: WSProbe): Unit =
    wsClient.sendMessage(request.toJson.toString)

  def fetchResponse()(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val response = wsClient.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[WebsocketLobbyResponse]
    response
  }

  def auth(tokenId: Int)(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val authRequest = AuthRequest(tokens(tokenId)).toJson.toString

    val request = WebsocketLobbyRequest(LobbyRoute.Auth, authRequest)
    val expectedResponse = WebsocketLobbyResponse(body = usernames(tokenId), statusCode = 200)

    sendRequest(request)

    val response = fetchResponse()
    response shouldBe expectedResponse
    response
  }

  def createLobby(lobbyName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val createLobbyRequest = LobbyCreationRequest(lobbyName).toJson.toString
    val request = WebsocketLobbyRequest(LobbyRoute.CreateLobby, createLobbyRequest)

    sendRequest(request)

    val response = fetchResponse()
    response
  }

  def fetchLobbies()(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val request = WebsocketLobbyRequest(LobbyRoute.Lobbies)
    sendRequest(request)

    val response = fetchResponse()
    response
  }

  def fetchLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val getLobbyRequest = GetLobbyRequest(lobbyId).toJson.toString
    val request = WebsocketLobbyRequest(LobbyRoute.Lobby, getLobbyRequest)
    sendRequest(request)

    val response = fetchResponse()
    response
  }

  def joinLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val joinLobbyRequest = LobbyJoinRequest(lobbyId).toJson.toString
    val request = WebsocketLobbyRequest(LobbyRoute.JoinLobby, joinLobbyRequest)

    sendRequest(request)

    val response = fetchResponse()
    response
  }

  def leaveLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val leaveLobbyRequest = LobbyLeaveRequest(lobbyId).toJson.toString
    val request = WebsocketLobbyRequest(LobbyRoute.LeaveLobby, leaveLobbyRequest)

    sendRequest(request)

    val response = fetchResponse()
    response
  }


  "WS" must {
    "fetch empty lobbies state" in {
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~>
        check {
          fetchLobbies().body shouldBe "[]"

          wsClient.sendCompletion()
//          wsClient.expectCompletion()
        }
    }

    "authenticate" in {
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0).statusCode shouldBe StatusCodes.OK.intValue

        wsClient.sendCompletion()
//        wsClient.expectCompletion()
      }
    }

    "allow creating lobbies" in {
      val lobbyName = "lobby_name"
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)

        val createLobbyRequest = LobbyCreationRequest(lobbyName).toJson.toString
        val wsRequest = WebsocketLobbyRequest(LobbyRoute.CreateLobby, createLobbyRequest)

        sendRequest(wsRequest)

        val response = fetchResponse()
        response.statusCode shouldBe StatusCodes.Created.intValue

        wsClient.sendCompletion()
      }
    }
    "allow joining and leaving lobbies" in {
      val lobbyName = "lobby_name"
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        // this request should fail as it is not a lobby id, but lobby name
        joinLobby(lobbyName).statusCode shouldBe StatusCodes.InternalServerError.intValue
        // user that creates the lobby joins it automatically
        joinLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue
        auth(1)
        joinLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue

        // wait for CQRS Event Handler to persist
        Thread.sleep(DBManager.dbTimeout.toMillis)

        {
          val lobbyResponse = fetchLobby(lobbyId)
          lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
          val lobby = lobbyResponse.body.parseJson.convertTo[LobbyState]
          lobby.userIds shouldEqual List(usernames(0), usernames(1))
        }

        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue

        {
          val lobbyResponse = fetchLobby(lobbyId)
          lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
          val lobby = lobbyResponse.body.parseJson.convertTo[LobbyState]
          lobby.userIds shouldEqual List(usernames(0))
        }

        auth(0)
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue

        {
          val lobbyResponse = fetchLobby(lobbyId)
          lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
          val lobby = lobbyResponse.body.parseJson.convertTo[LobbyState]
          lobby.userIds shouldEqual List()
        }

        wsClient.sendCompletion()
      }
    }
//
//    "disallow joining a lobby you are already in" in {
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//    }
//
//    "disallow leaving a lobby you are already in" in {
//      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//
//      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//
//    }
//
//    // TODO: move this somewhere else
//    "allow inspecting empty game state" in {
//      Get("/api/state/random_id") ~> routes ~> check {
//        status shouldEqual OK
//        val gameState = responseAs[GameState]
//        gameState.gamePhase shouldEqual GamePhase.NotStarted
//        gameState.players.length shouldEqual 0
//      }
//    }
//
//
//    "allow setting map name in a lobby for a host" in {
//      val hexMapNames = List("Linia", "1v1v1")
//
//      hexMapNames.foreach { hexMapName =>
//        Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//        // wait for CQRS Event Handler to persist
//        Thread.sleep(DBManager.dbTimeout.toMillis)
//
//        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
//          status shouldEqual OK
//          val lobby = responseAs[LobbyState]
//          lobby.chosenHexMapName shouldEqual Some(hexMapName)
//        }
//      }
//    }
//
//    "disallow setting invalid map name" in {
//      val hexMapName = "this map does not exist"
//
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//    }
//
//    "allow setting pick type in a lobby for a host" in {
//      val pickTypes = PickType.values
//
//      pickTypes.foreach { p =>
//        Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, p)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//        // wait for CQRS Event Handler to persist
//        Thread.sleep(DBManager.dbTimeout.toMillis)
//
//        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
//          status shouldEqual OK
//          val lobby = responseAs[LobbyState]
//          lobby.pickType shouldEqual p
//        }
//      }
//    }
//
//    "allow setting number of bans in a lobby for a host" in {
//      val numberOfBansList = List(5,3,2,1,0,3)
//
//      numberOfBansList.foreach { n =>
//        Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//        // wait for CQRS Event Handler to persist
//        Thread.sleep(DBManager.dbTimeout.toMillis)
//
//        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
//          status shouldEqual OK
//          val lobby = responseAs[LobbyState]
//          lobby.numberOfBans shouldEqual n
//        }
//      }
//    }
//
//    "disallow setting invalid number of bans in a lobby for a host" in {
//      val numberOfBansList = List(-1, -4, -234)
//
//      numberOfBansList.foreach { n =>
//        Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//      }
//    }
//
//    "allow setting number of characters in a lobby for a host" in {
//      val numberOfCharactersList = List(5,3,2,3,8)
//
//      numberOfCharactersList.foreach { n =>
//        Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//        // wait for CQRS Event Handler to persist
//        Thread.sleep(DBManager.dbTimeout.toMillis)
//
//        Get(s"/api/lobby/$lobbyId") ~> routes ~> check {
//          status shouldEqual OK
//          val lobby = responseAs[LobbyState]
//          lobby.numberOfCharactersPerPlayer shouldEqual n
//        }
//      }
//    }
//
//    "disallow setting invalid number of characters in a lobby for a host" in {
//      val numberOfCharactersList = List(-1, 0, -234)
//
//      numberOfCharactersList.foreach { n =>
//        Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, n)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//      }
//    }
//
//    "allow to start a game" in {
//      val hexMapName = "Linia"
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
//
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
//        status shouldEqual OK
//        val gameState = responseAs[GameState]
//        gameState.gamePhase shouldEqual GamePhase.CharacterPlacing
//        gameState.players.length shouldEqual 2
//        gameState.hexMap.get.name shouldEqual hexMapName
//        gameState.numberOfBans shouldEqual 0
//        gameState.numberOfCharactersPerPlayers shouldEqual 1
//        gameState.pickType shouldEqual AllRandom
//      }
//    }
//
//    "allow to start a game with all things changed" in {
//      val hexMapName = "1v1v1"
//      val pickType = DraftPick
//      val numberOfBans = 4
//      val numberOfCharacters = 5
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, pickType)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, numberOfBans)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, numberOfCharacters)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
//
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
//        status shouldEqual OK
//        val gameState = responseAs[GameState]
//        gameState.gamePhase shouldEqual GamePhase.CharacterPick
//        gameState.players.length shouldEqual 2
//        gameState.hexMap.get.name shouldEqual hexMapName
//        gameState.numberOfBans shouldEqual numberOfBans
//        gameState.numberOfCharactersPerPlayers shouldEqual numberOfCharacters
//        gameState.pickType shouldEqual DraftPick
//      }
//    }
//
//    "disallow starting game without hexmap" in {
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//    }
//
//    "disallow starting game with 1 or 0 players" in {
//      val hexMapName = "Linia"
//
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/leave_lobby", LobbyLeaveRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//    }
//
//
//    "disallow setting stuff in a lobby for non host persons" in {
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, "Linia")) ~> routes ~> check(status shouldEqual Unauthorized)
//      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, AllRandom)) ~> routes ~> check(status shouldEqual Unauthorized)
//      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, 3)) ~> routes ~> check(status shouldEqual Unauthorized)
//      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, 4)) ~> routes ~> check(status shouldEqual Unauthorized)
//
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, "Linia")).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, AllRandom)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, 3)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, 4)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual InternalServerError)
//    }
//
//    "disallow setting stuff in a lobby after game start" in {
//      val hexMapName = "Linia"
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(1))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, "Linia")).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, AllRandom)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, 3)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, 4)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual InternalServerError)
//    }
//
//    "disallow starting a game with more players than the map allows" in {
//      val numberOfPlayers = 4
//      val hexMapName = "1v1v1"
//      val pickType = DraftPick
//      val numberOfBans = 4
//      val numberOfCharacters = 5
//
//      Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, pickType)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, numberOfBans)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//      Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, numberOfCharacters)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//
//      for (i <- 1 until numberOfPlayers) {
//        Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(i))) ~> routes ~> check(status shouldEqual OK)
//      }
//
//      Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
//    }
//  }
  }
}
