package ws

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.DBManager
import com.tosware.NKM.actors.WebsocketUser.{WebsocketLobbyRequest, WebsocketLobbyResponse}
import com.tosware.NKM.models.game.PickType
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.services.http.routes.LobbyRoute
import helpers.UserApiTrait
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


  def sendWSRequest(route: LobbyRoute, requestJson: String = "")(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    sendRequest(WebsocketLobbyRequest(route, requestJson))
    fetchResponse()
  }

  def createLobby(lobbyName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.CreateLobby, LobbyCreationRequest(lobbyName).toJson.toString)

  def fetchLobbies()(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.Lobbies)

  def fetchLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.Lobby, GetLobbyRequest(lobbyId).toJson.toString)

  def joinLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.JoinLobby, LobbyJoinRequest(lobbyId).toJson.toString)

  def leaveLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.LeaveLobby, LobbyLeaveRequest(lobbyId).toJson.toString)

  def setHexMap(lobbyId: String, hexMapName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.SetHexMap, SetHexMapNameRequest(lobbyId, hexMapName).toJson.toString)

  def setPickType(lobbyId: String, pickType: PickType)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.SetPickType, SetPickTypeRequest(lobbyId, pickType).toJson.toString)

  def setNumberOfBans(lobbyId: String, numberOfBans: Int)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.SetNumberOfBans, SetNumberOfBansRequest(lobbyId, numberOfBans).toJson.toString)

  def setNumberOfCharacters(lobbyId: String, numberOfCharacters: Int)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.SetNumberOfCharacters, SetNumberOfCharactersPerPlayerRequest(lobbyId, numberOfCharacters).toJson.toString)

  def startGame(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequest(LobbyRoute.StartGame, StartGameRequest(lobbyId).toJson.toString)

  def fetchAndParseLobby(lobbyId: String)(implicit wsClient: WSProbe): LobbyState = {
    // wait for CQRS Event Handler to persist
    Thread.sleep(DBManager.dbTimeout.toMillis)
    val lobbyResponse = fetchLobby(lobbyId)
    lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
    lobbyResponse.body.parseJson.convertTo[LobbyState]
  }



  "WS" must {
    "respond to invalid requests" in {
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~>
        check {
          wsClient.sendMessage("invalid request")
          fetchResponse().statusCode shouldBe 500

          wsClient.sendCompletion()
          //          wsClient.expectCompletion()
        }
    }

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

        fetchAndParseLobby(lobbyId).userIds shouldEqual List(usernames(0), usernames(1))

        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue

        fetchAndParseLobby(lobbyId).userIds shouldEqual List(usernames(0))

        auth(0)
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue

        fetchAndParseLobby(lobbyId).userIds shouldEqual List()

        wsClient.sendCompletion()
      }
    }

    "disallow joining a lobby you are already in" in {
      val lobbyName = "lobby_name"
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        // user that creates the lobby joins it automatically
        joinLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue
        auth(1)
        joinLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        joinLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue
      }
    }

    "disallow leaving a lobby you are already in" in {
      val lobbyName = "lobby_name"
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        // user that creates the lobby joins it automatically
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue
        auth(1)
        leaveLobby(lobbyId).statusCode shouldBe StatusCodes.InternalServerError.intValue
      }
    }

    "allow setting map name in a lobby for a host" in {
      val lobbyName = "lobby_name"
      val hexMapNames = List("Linia", "1v1v1")
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {

        auth(0)
        val lobbyId = createLobby(lobbyName).body

        hexMapNames.foreach { hexMapName =>
          setHexMap(lobbyId, hexMapName).statusCode shouldBe StatusCodes.OK.intValue
          fetchAndParseLobby(lobbyId).chosenHexMapName shouldEqual Some(hexMapName)
        }
      }
    }

    "disallow setting invalid map name" in {
      val lobbyName = "lobby_name"
      val hexMapName = "this map does not exist"
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body
        setHexMap(lobbyId, hexMapName).statusCode shouldBe StatusCodes.InternalServerError.intValue
      }
    }

    "allow setting pick type in a lobby for a host" in {
      val lobbyName = "lobby_name"
      val pickTypes = PickType.values
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        pickTypes.foreach { p =>
          setPickType(lobbyId, p)
          fetchAndParseLobby(lobbyId).pickType shouldBe p
        }
      }
    }

    "allow setting number of bans in a lobby for a host" in {
      val lobbyName = "lobby_name"
      val numberOfBansList = List(5,3,2,1,0,3)
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        numberOfBansList.foreach { b =>
          setNumberOfBans(lobbyId, b)
          fetchAndParseLobby(lobbyId).numberOfBans shouldBe b
        }
      }
    }

    "disallow setting invalid number of bans in a lobby for a host" in {
      val lobbyName = "lobby_name"
      val numberOfBansList = List(-1, -4, -234)
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        numberOfBansList.foreach { b =>
          setNumberOfBans(lobbyId, b).statusCode shouldBe StatusCodes.InternalServerError.intValue
        }
      }
    }

    "allow setting number of characters in a lobby for a host" in {
      val lobbyName = "lobby_name"
      val numberOfCharactersList = List(5,3,2,3,8)
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        numberOfCharactersList.foreach { n =>
          setNumberOfCharacters(lobbyId, n)
          fetchAndParseLobby(lobbyId).numberOfCharactersPerPlayer shouldBe n
        }
      }
    }

    "disallow setting invalid number of characters in a lobby for a host" in {
      val lobbyName = "lobby_name"
      val numberOfCharactersList = List(-1, 0, -234)
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body

        numberOfCharactersList.foreach { n =>
          setNumberOfCharacters(lobbyId, n).statusCode shouldBe StatusCodes.InternalServerError.intValue
        }
      }
    }

    "allow to start a game" in {
      val lobbyName = "lobby_name"
      val hexMapName = "Linia"
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body
        setHexMap(lobbyId, hexMapName)
        auth(1)
        joinLobby(lobbyId)
        auth(0)
        startGame(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        fail() // TODO: fetch and validate game state
//        val gameState = responseAs[GameState]
//        gameState.gamePhase shouldEqual GamePhase.CharacterPlacing
//        gameState.players.length shouldEqual 2
//        gameState.hexMap.get.name shouldEqual hexMapName
//        gameState.numberOfBans shouldEqual 0
//        gameState.numberOfCharactersPerPlayers shouldEqual 1
//        gameState.pickType shouldEqual AllRandom

      }
    }

    "allow to start a game with all things changed" in {
      val lobbyName = "A very interesting lobby name ;)"
      val hexMapName = "1v1v1"
      val pickType = PickType.DraftPick
      val numberOfBans = 4
      val numberOfCharacters = 5
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0)
        val lobbyId = createLobby(lobbyName).body
        setHexMap(lobbyId, hexMapName)
        setPickType(lobbyId, pickType)
        setNumberOfBans(lobbyId, numberOfBans)
        setNumberOfCharacters(lobbyId, numberOfCharacters)
        auth(1)
        joinLobby(lobbyId)
        auth(0)
        startGame(lobbyId).statusCode shouldBe StatusCodes.OK.intValue
        fail() // TODO: fetch and validate game state
        //        val gameState = responseAs[GameState]
        //        gameState.gamePhase shouldEqual GamePhase.CharacterPick
        //        gameState.players.length shouldEqual 2
        //        gameState.hexMap.get.name shouldEqual hexMapName
        //        gameState.numberOfBans shouldEqual numberOfBans
        //        gameState.numberOfCharactersPerPlayers shouldEqual numberOfCharacters
        //        gameState.pickType shouldEqual DraftPick
      }
    }

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
