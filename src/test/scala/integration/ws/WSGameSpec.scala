package integration.ws

import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.models.game.blindpick.BlindPickPhase
import com.tosware.NKM.models.game.draftpick.DraftPickPhase
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.models.game.{ClockConfig, GamePhase, PickType, VictoryStatus}
import helpers.WSTrait

class WSGameSpec extends WSTrait {
  def sendRequest(request: WebsocketGameRequest)(implicit wsClient: WSProbe): Unit = sendRequestG(request)

  def fetchResponse()(implicit wsClient: WSProbe): WebsocketGameResponse = fetchResponseG()

  def sendWSRequest(route: GameRoute, requestJson: String = "")(implicit wsClient: WSProbe): WebsocketGameResponse = sendWSRequestG(route, requestJson)

  def auth(tokenId: Int)(implicit wsClient: WSProbe): WebsocketGameResponse = authG(tokenId)

  def observe(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse = observeG(lobbyId)

  def createLobbyForGame(lobbyName: String = "lobby_name",
                  hexMapName: String = "1v1v1",
                  pickType: PickType = PickType.AllRandom,
                  numberOfPlayers: Int = 3,
                  numberOfBans: Int = 0,
                  numberOfCharacters: Int = 2,
                  clockConfig: ClockConfig = ClockConfig.emptyDraftPickConfig,
  ): String = {
    var gameId = ""
    withLobbyWS {
      authL(0)
      gameId = createLobby(lobbyName).body
      setHexMap(gameId, hexMapName)
      setPickType(gameId, pickType)
      setNumberOfBans(gameId, numberOfBans)
      setNumberOfCharacters(gameId, numberOfCharacters)
      setClockConfig(gameId, clockConfig)
      for (i <- 1 until numberOfPlayers) {
        authL(i)
        joinLobby(gameId)
      }
      authL(0)
      startGame(gameId).statusCode shouldBe ok
    }
    gameId
  }


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
        auth(0).statusCode shouldBe ok
      }
    }

    "allow observing" in {
      val gameId = createLobbyForGame()

      withGameWS {
        auth(0)
        observe(gameId).statusCode shouldBe ok
        surrender(gameId).statusCode shouldBe ok

        val observedResponse = fetchResponse()
        observedResponse.gameResponseType shouldBe GameResponseType.State
        observedResponse.statusCode shouldBe ok
      }
    }

    "allow surrendering during draft pick" in {
      val gameId = createLobbyForGame(
        pickType = PickType.DraftPick,
      )

      withGameWS {
        auth(0)

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.gamePhase shouldBe GamePhase.CharacterPick
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        surrender(gameId).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.gamePhase shouldBe GamePhase.Finished
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Drawn
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Drawn
        }
        surrender(gameId).statusCode shouldBe nok
        auth(1)
        surrender(gameId).statusCode shouldBe nok
      }
    }

    "allow surrendering during game" in {
      val gameId = createLobbyForGame(
        pickType = PickType.AllRandom,
        clockConfig = ClockConfig.emptyDraftPickConfig.copy(timeAfterPickMillis = 1)
      )

      withGameWS {
        auth(0)

        Thread.sleep(150)

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.gamePhase shouldBe GamePhase.Running
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        surrender(gameId).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        auth(1)
        surrender(gameId).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Won
        }
      }
    }

    "allow banning during draft pick" in {
      val gameId = createLobbyForGame(
        pickType = PickType.DraftPick,
        numberOfPlayers = 3,
        numberOfBans = 2,
        numberOfCharacters = 2,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(gameId).draftPickState.get.config.availableCharacters

        ban(gameId, availableCharacters).statusCode shouldBe nok
        ban(gameId, Set()).statusCode shouldBe ok
        ban(gameId, Set()).statusCode shouldBe nok

        auth(1)
        ban(gameId, Set(availableCharacters.head, availableCharacters.tail.head)).statusCode shouldBe ok
        ban(gameId, Set()).statusCode shouldBe nok

        auth(2)
        ban(gameId, Set(availableCharacters.head)).statusCode shouldBe ok

        fetchAndParseGame(gameId).draftPickState.get.pickPhase shouldBe DraftPickPhase.Picking
      }
    }

    "allow picking during draft pick" in {
      val gameId = createLobbyForGame(
        pickType = PickType.DraftPick,
        numberOfPlayers = 3,
        numberOfBans = 0,
        numberOfCharacters = 2,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(gameId).draftPickState.get.config.availableCharacters.toSeq

        pick(gameId, availableCharacters(0)).statusCode shouldBe ok
        pick(gameId, availableCharacters(0)).statusCode shouldBe nok
        pick(gameId, availableCharacters(1)).statusCode shouldBe nok

        auth(1)
        pick(gameId, availableCharacters(0)).statusCode shouldBe nok
        pick(gameId, availableCharacters(1)).statusCode shouldBe ok

        auth(2)
        pick(gameId, availableCharacters(2)).statusCode shouldBe ok
        pick(gameId, availableCharacters(3)).statusCode shouldBe ok

        auth(1)
        pick(gameId, availableCharacters(4)).statusCode shouldBe ok

        auth(0)
        pick(gameId, availableCharacters(5)).statusCode shouldBe ok
        pick(gameId, availableCharacters(6)).statusCode shouldBe nok

        val gameState = fetchAndParseGame(gameId)
        gameState.draftPickState.get.pickPhase shouldBe DraftPickPhase.Finished
        gameState.gamePhase shouldBe GamePhase.CharacterPicked
      }
    }

    "allow picking during blind pick" in {
      val numberOfCharacters = 2

      val gameId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfPlayers = 3,
        numberOfCharacters = numberOfCharacters,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(gameId).blindPickState.get.config.availableCharacters.toSeq

        blindPick(gameId, availableCharacters.take(numberOfCharacters).toSet).statusCode shouldBe ok
        blindPick(gameId, availableCharacters.take(numberOfCharacters).toSet).statusCode shouldBe nok

        auth(2)
        blindPick(gameId, availableCharacters.take(numberOfCharacters).toSet).statusCode shouldBe ok

        auth(1)
        blindPick(gameId, availableCharacters.take(numberOfCharacters).toSet).statusCode shouldBe ok

        val gameState = fetchAndParseGame(gameId)
        gameState.blindPickState.get.pickPhase shouldBe BlindPickPhase.Finished
        gameState.gamePhase shouldBe GamePhase.CharacterPicked
      }
    }

    "handle blind pick timeout when nobody picks" in {
      val maxPickTimeMillis = 1
      val gameId = createLobbyForGame(
        pickType = PickType.BlindPick,
        clockConfig = ClockConfig.emptyDraftPickConfig.copy(maxPickTimeMillis = maxPickTimeMillis)
      )

      withGameWS {
        auth(0)

        Thread.sleep(maxPickTimeMillis)

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.clock.config.maxPickTimeMillis shouldBe maxPickTimeMillis
          gameState.blindPickState.get.pickPhase shouldBe BlindPickPhase.Picking
          gameState.players.forall(_.victoryStatus == VictoryStatus.Lost) shouldBe true
        }
      }
    }

    "handle blind pick timeout when one player does not pick" in {
      val maxPickTimeMillis = 500
      val numberOfCharacters = 3
      val gameId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfCharacters = numberOfCharacters,
        clockConfig = ClockConfig.emptyDraftPickConfig.copy(maxPickTimeMillis = maxPickTimeMillis),
      )

      withGameWS {
        val availableCharacters = fetchAndParseGame(gameId).blindPickState.get.config.availableCharacters.toSeq
        auth(0)
        blindPick(gameId, availableCharacters.take(numberOfCharacters).toSet)

        auth(2)
        blindPick(gameId, availableCharacters.take(numberOfCharacters).toSet)

        Thread.sleep(maxPickTimeMillis)

        {
          val gameState = fetchAndParseGame(gameId)
          gameState.players.map(_.victoryStatus) shouldBe Seq(VictoryStatus.Drawn, VictoryStatus.Lost, VictoryStatus.Drawn)
        }
      }
    }
  }
}
