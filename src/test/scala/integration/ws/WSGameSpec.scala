package integration.ws

import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.models.game.blindpick.BlindPickPhase
import com.tosware.NKM.models.game.draftpick.DraftPickPhase
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.models.game.{ClockConfig, GameStatus, NKMCharacterMetadata, PickType, VictoryStatus}
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
    var lobbyId = ""
    withLobbyWS {
      authL(0)
      lobbyId = createLobby(lobbyName).body
      setHexMap(lobbyId, hexMapName)
      setPickType(lobbyId, pickType)
      setNumberOfBans(lobbyId, numberOfBans)
      setNumberOfCharacters(lobbyId, numberOfCharacters)
      setClockConfig(lobbyId, clockConfig)
      for (i <- 1 until numberOfPlayers) {
        authL(i)
        joinLobby(lobbyId)
      }
      authL(0)
      startGame(lobbyId).statusCode shouldBe ok
    }
    lobbyId
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
      val lobbyId = createLobbyForGame()

      withGameWS {
        auth(0)
        observe(lobbyId).statusCode shouldBe ok
        surrender(lobbyId).statusCode shouldBe ok

        val observedResponse = fetchResponse()
        observedResponse.gameResponseType shouldBe GameResponseType.State
        observedResponse.statusCode shouldBe ok
      }
    }

    "allow surrendering during draft pick" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.DraftPick,
      )

      withGameWS {
        auth(0)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.gameStatus shouldBe GameStatus.CharacterPick
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        surrender(lobbyId).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.gameStatus shouldBe GameStatus.Finished
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Drawn
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Drawn
        }
        surrender(lobbyId).statusCode shouldBe nok
        auth(1)
        surrender(lobbyId).statusCode shouldBe nok
      }
    }

    "allow surrendering during game" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.AllRandom,
        clockConfig = ClockConfig.emptyDraftPickConfig.copy(timeAfterPickMillis = 1)
      )

      withGameWS {
        auth(0)

        Thread.sleep(150)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.gameStatus shouldBe GameStatus.Running
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        surrender(lobbyId).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Pending
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Pending
        }

        auth(1)
        surrender(lobbyId).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.players(0).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(1).victoryStatus shouldBe VictoryStatus.Lost
          gameState.players(2).victoryStatus shouldBe VictoryStatus.Won
        }
      }
    }

    "allow banning during draft pick" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.DraftPick,
        numberOfPlayers = 3,
        numberOfBans = 2,
        numberOfCharacters = 2,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).draftPickState.get.config.availableCharacters

        ban(lobbyId, availableCharacters).statusCode shouldBe nok
        ban(lobbyId, Set()).statusCode shouldBe ok
        ban(lobbyId, Set()).statusCode shouldBe nok

        auth(1)
        ban(lobbyId, Set(availableCharacters.head, availableCharacters.tail.head)).statusCode shouldBe ok
        ban(lobbyId, Set()).statusCode shouldBe nok

        auth(2)
        ban(lobbyId, Set(availableCharacters.head)).statusCode shouldBe ok

        fetchAndParseGame(lobbyId).draftPickState.get.pickPhase shouldBe DraftPickPhase.Picking
      }
    }

    "allow picking during draft pick" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.DraftPick,
        numberOfPlayers = 3,
        numberOfBans = 0,
        numberOfCharacters = 2,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).draftPickState.get.config.availableCharacters.toSeq

        pick(lobbyId, availableCharacters(0)).statusCode shouldBe ok
        pick(lobbyId, availableCharacters(0)).statusCode shouldBe nok
        pick(lobbyId, availableCharacters(1)).statusCode shouldBe nok

        auth(1)
        pick(lobbyId, availableCharacters(0)).statusCode shouldBe nok
        pick(lobbyId, availableCharacters(1)).statusCode shouldBe ok

        auth(2)
        pick(lobbyId, availableCharacters(2)).statusCode shouldBe ok
        pick(lobbyId, availableCharacters(3)).statusCode shouldBe ok

        auth(1)
        pick(lobbyId, availableCharacters(4)).statusCode shouldBe ok

        auth(0)
        pick(lobbyId, availableCharacters(5)).statusCode shouldBe ok
        pick(lobbyId, availableCharacters(6)).statusCode shouldBe nok

        val gameState = fetchAndParseGame(lobbyId)
        gameState.draftPickState.get.pickPhase shouldBe DraftPickPhase.Finished
        gameState.gameStatus shouldBe GameStatus.CharacterPicked
      }
    }

    "hide draft pick information during ban phase" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.DraftPick,
        numberOfPlayers = 3,
        numberOfBans = 2,
        numberOfCharacters = 2,
      )

      withGameWS {
        val availableCharacters = fetchAndParseGame(lobbyId).draftPickState.get.config.availableCharacters
        val player0Bans = Set.empty[NKMCharacterMetadata.CharacterMetadataId]
        val player1Bans = Set(availableCharacters.head, availableCharacters.tail.head)
        val player2Bans = Set(availableCharacters.head)

        auth(0)
        ban(lobbyId, player0Bans).statusCode shouldBe ok

        auth(1)
        ban(lobbyId, player1Bans).statusCode shouldBe ok

        // check
        auth(0)
        fetchAndParseGame(lobbyId).draftPickState.get.bans shouldBe
          Map(usernames(0) -> Some(player0Bans), usernames(1) -> None, usernames(2) -> None)
        auth(1)
        fetchAndParseGame(lobbyId).draftPickState.get.bans shouldBe
          Map(usernames(0) -> None, usernames(1) -> Some(player1Bans), usernames(2) -> None)
        auth(2)
        fetchAndParseGame(lobbyId).draftPickState.get.bans shouldBe
          Map(usernames(0) -> None, usernames(1) -> None, usernames(2) -> None)

        auth(2)
        ban(lobbyId, Set(availableCharacters.head)).statusCode shouldBe ok

        // check
        val finalBans = Map(usernames(0) -> Some(player0Bans), usernames(1) -> Some(player1Bans), usernames(2) -> Some(player2Bans))
        auth(0)
        fetchAndParseGame(lobbyId).draftPickState.get.bans shouldBe finalBans
        auth(1)
        fetchAndParseGame(lobbyId).draftPickState.get.bans shouldBe finalBans
        auth(2)
        fetchAndParseGame(lobbyId).draftPickState.get.bans shouldBe finalBans
      }
    }

    "allow picking during blind pick" in {
      val numberOfCharacters = 2

      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfPlayers = 3,
        numberOfCharacters = numberOfCharacters,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        val charactersToPick = availableCharacters.take(numberOfCharacters).toSet

        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok
        blindPick(lobbyId, charactersToPick).statusCode shouldBe nok

        auth(2)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(1)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        val gameState = fetchAndParseGame(lobbyId)
        gameState.blindPickState.get.pickPhase shouldBe BlindPickPhase.Finished
        gameState.gameStatus shouldBe GameStatus.CharacterPicked
      }
    }

    "hide blind pick information of other players picks during picking" in {
      val numberOfCharacters = 2

      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfPlayers = 3,
        numberOfCharacters = numberOfCharacters,
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        val charactersToPick = availableCharacters.take(numberOfCharacters).toSet

        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(2)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok


        // check
        auth(0)
        fetchAndParseGame(lobbyId).blindPickState.get.characterSelection shouldBe
          Map(usernames(0) -> charactersToPick, usernames(1) -> Set(), usernames(2) -> Set())

        auth(1)
        fetchAndParseGame(lobbyId).blindPickState.get.characterSelection shouldBe
          Map(usernames(0) -> Set(), usernames(1) -> Set(), usernames(2) -> Set())

        auth(2)
        fetchAndParseGame(lobbyId).blindPickState.get.characterSelection shouldBe
          Map(usernames(0) -> Set(), usernames(1) -> Set(), usernames(2) -> charactersToPick)

        // finish picking
        auth(1)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        // check after picking
        val finalMap = Map(usernames(0) -> charactersToPick, usernames(1) -> charactersToPick, usernames(2) -> charactersToPick)
        auth(0)
        fetchAndParseGame(lobbyId).blindPickState.get.characterSelection shouldBe finalMap
        auth(1)
        fetchAndParseGame(lobbyId).blindPickState.get.characterSelection shouldBe finalMap
        auth(2)
        fetchAndParseGame(lobbyId).blindPickState.get.characterSelection shouldBe finalMap
      }
    }

    "handle blind pick timeout when nobody picks" in {
      val maxPickTimeMillis = 1
      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        clockConfig = ClockConfig.emptyDraftPickConfig.copy(maxPickTimeMillis = maxPickTimeMillis)
      )

      withGameWS {
        auth(0)

        Thread.sleep(maxPickTimeMillis)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.config.maxPickTimeMillis shouldBe maxPickTimeMillis
          gameState.blindPickState.get.pickPhase shouldBe BlindPickPhase.Picking
          println(gameState.players)
          gameState.players.forall(_.victoryStatus == VictoryStatus.Lost) shouldBe true
        }
      }
    }

    "handle blind pick timeout when one player does not pick" in {
      val maxPickTimeMillis = 500
      val numberOfCharacters = 3
      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfCharacters = numberOfCharacters,
        clockConfig = ClockConfig.emptyDraftPickConfig.copy(maxPickTimeMillis = maxPickTimeMillis),
      )

      withGameWS {
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        auth(0)
        blindPick(lobbyId, availableCharacters.take(numberOfCharacters).toSet)

        auth(2)
        blindPick(lobbyId, availableCharacters.take(numberOfCharacters).toSet)

        Thread.sleep(maxPickTimeMillis)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.players.map(_.victoryStatus) shouldBe Seq(VictoryStatus.Drawn, VictoryStatus.Lost, VictoryStatus.Drawn)
        }
      }
    }
  }
}
