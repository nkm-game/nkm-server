package ws

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.models.game.{GamePhase, PickType, VictoryStatus}
import com.tosware.NKM.models.game.ws._
import helpers.WSTrait

class WSGameSpec extends WSTrait
{
  def sendRequest(request: WebsocketGameRequest)(implicit wsClient: WSProbe): Unit = sendRequestG(request)
  def fetchResponse()(implicit wsClient: WSProbe): WebsocketGameResponse = fetchResponseG()
  def sendWSRequest(route: GameRoute, requestJson: String = "")(implicit wsClient: WSProbe): WebsocketGameResponse = sendWSRequestG(route, requestJson)
  def auth(tokenId: Int)(implicit wsClient: WSProbe): WebsocketGameResponse = authG(tokenId)
  def observe(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse = observeG(lobbyId)

  "WS" must {
    "respond to invalid requests" in {
      withGameWS {
        wsClient.sendMessage("invalid request")
        val response = fetchResponse()
        response.statusCode shouldBe 500
        response.gameResponseType shouldBe GameResponseType.Error
      }
    }

    "allow authenticating" in {
      withGameWS {
        auth(0).statusCode shouldBe StatusCodes.OK.intValue
      }
    }

    "allow observing" in {
      val lobbyName = "lobby_name"
      val hexMapName = "Linia"
      val pickType = PickType.DraftPick
      val numberOfBans = 1
      val numberOfCharacters = 4
      var gameId = ""
      withLobbyWS {
        authL(0)
        gameId = createLobby(lobbyName).body
        setHexMap(gameId, hexMapName)
        setPickType(gameId, pickType)
        setNumberOfBans(gameId, numberOfBans)
        setNumberOfCharacters(gameId, numberOfCharacters)
        authL(1)
        joinLobby(gameId)
        authL(0)
        startGame(gameId).statusCode shouldBe StatusCodes.OK.intValue
      }

      withGameWS {
        auth(0)
        observe(gameId).statusCode shouldBe StatusCodes.OK.intValue
        surrender(gameId).statusCode shouldBe StatusCodes.OK.intValue

        val observedResponse = fetchResponse()
        observedResponse.gameResponseType shouldBe GameResponseType.State
        observedResponse.statusCode shouldBe StatusCodes.OK.intValue
      }
    }

    "allow surrendering during draft pick" in {
      val lobbyName = "lobby_name"
      val hexMapName = "1v1v1"
      val pickType = PickType.DraftPick
      val numberOfBans = 1
      val numberOfCharacters = 4
      var gameId = ""
      withLobbyWS {
        authL(0)
        gameId = createLobby(lobbyName).body
        setHexMap(gameId, hexMapName)
        setPickType(gameId, pickType)
        setNumberOfBans(gameId, numberOfBans)
        setNumberOfCharacters(gameId, numberOfCharacters)
        authL(1)
        joinLobby(gameId)
        authL(2)
        joinLobby(gameId)
        authL(0)
        startGame(gameId).statusCode shouldBe StatusCodes.OK.intValue
      }

      withGameWS {
        auth(0)

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.gamePhase shouldBe GamePhase.CharacterPick
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        surrender(gameId).statusCode shouldBe StatusCodes.OK.intValue

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.gamePhase shouldBe GamePhase.Finished
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Drawn
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Drawn
        }
        surrender(gameId).statusCode shouldBe StatusCodes.InternalServerError.intValue
        auth(1)
        surrender(gameId).statusCode shouldBe StatusCodes.InternalServerError.intValue
      }
    }

    "allow surrendering during game" in {
      val lobbyName = "lobby_name"
      val hexMapName = "1v1v1"
      val pickType = PickType.AllRandom
      val numberOfBans = 0
      val numberOfCharacters = 4
      var gameId = ""
      withLobbyWS {
        authL(0)
        gameId = createLobby(lobbyName).body
        setHexMap(gameId, hexMapName)
        setPickType(gameId, pickType)
        setNumberOfBans(gameId, numberOfBans)
        setNumberOfCharacters(gameId, numberOfCharacters)
        authL(1)
        joinLobby(gameId)
        authL(2)
        joinLobby(gameId)
        authL(0)
        startGame(gameId).statusCode shouldBe StatusCodes.OK.intValue
      }

      withGameWS {
        auth(0)

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.gamePhase shouldBe GamePhase.CharacterPlacing
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        surrender(gameId).statusCode shouldBe StatusCodes.OK.intValue

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        auth(1)
        surrender(gameId).statusCode shouldBe StatusCodes.OK.intValue

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Won
        }
      }
    }
  }
}
