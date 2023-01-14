package integration.ws

import akka.http.scaladsl.testkit.WSProbe
import com.tosware.nkm.NkmUtils
import com.tosware.nkm.models.game.blindpick.BlindPickPhase
import com.tosware.nkm.models.game.draftpick.DraftPickPhase
import com.tosware.nkm.models.game.hex.{HexCellType, HexCoordinates}
import com.tosware.nkm.models.game.ws._
import com.tosware.nkm.models.game._
import helpers.WSTrait

class WSGameSpec extends WSTrait {
  def sendRequest(request: WebsocketGameRequest)(implicit wsClient: WSProbe): Unit = sendRequestG(request)

  def fetchResponse()(implicit wsClient: WSProbe): WebsocketGameResponse = fetchResponseG()

  def sendWSRequest(route: GameRoute, requestJson: String = "")(implicit wsClient: WSProbe): WebsocketGameResponse = sendWSRequestG(route, requestJson)

  def auth(tokenId: Int)(implicit wsClient: WSProbe): WebsocketGameResponse = authG(tokenId)

  def observe(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse = observeG(lobbyId)

  def createLobbyForGame(lobbyName: String = "lobby_name",
                  hexMapName: String = "1v1v1",
                  pickType: PickType = PickType.DraftPick,
                  numberOfPlayers: Int = 3,
                  numberOfBans: Int = 0,
                  numberOfCharacters: Int = 2,
                  clockConfigOpt: Option[ClockConfig] = None,
  ): String = {
    var lobbyId = ""
    val clockConfig = clockConfigOpt.getOrElse(ClockConfig.defaultForPickType(pickType))
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
      val startGameResponse = startGame(lobbyId)
      if(startGameResponse.statusCode != ok) {
        fail(startGameResponse.body)
      }
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
        observedResponse.gameResponseType shouldBe GameResponseType.Event
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

    "use different randomness in different games" in {
      val lobbyId1 = createLobbyForGame(
        pickType = PickType.AllRandom,
        hexMapName = "Shuriken",
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.AllRandom).copy(timeAfterPickMillis = 1)),
        numberOfCharacters = 2,
        numberOfPlayers = 4,
      )

      val lobbyId2 = createLobbyForGame(
        lobbyName = "other lobby",
        pickType = PickType.AllRandom,
        hexMapName = "Shuriken",
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.AllRandom).copy(timeAfterPickMillis = 1)),
        numberOfCharacters = 2,
        numberOfPlayers = 4,
      )

      withGameWS {
        auth(0)

        Thread.sleep(150)
        def getPlayersWithCharacters(gameView: GameStateView) = {
          gameView.players.map(p => p.name -> p.characterIds.map(c => gameView.characters.find(_.id == c).get.metadataId))
        }

        val x1 = getPlayersWithCharacters(fetchAndParseGame(lobbyId1))
        val x2 = getPlayersWithCharacters(fetchAndParseGame(lobbyId2))

        x1 should not be x2
      }
    }

    "allow surrendering during game" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.AllRandom,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.AllRandom).copy(timeAfterPickMillis = 1)),
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
        val player0Bans = Set.empty[CharacterMetadata.CharacterMetadataId]
        val player1Bans = Set(availableCharacters.head, availableCharacters.tail.head)
        val player2Bans = Set(availableCharacters.head)

        auth(0)
        ban(lobbyId, player0Bans).statusCode shouldBe ok

        auth(1)
        ban(lobbyId, player1Bans).statusCode shouldBe ok

        // check
        auth(0)

        {
          val game = fetchAndParseGame(lobbyId)
          game.draftPickState.get.bannedCharacters shouldBe player0Bans
          game.draftPickState.get.bans shouldBe
            Map(usernames(0) -> Some(player0Bans), usernames(1) -> None, usernames(2) -> None)
        }

        auth(1)

        {
          val game = fetchAndParseGame(lobbyId)
          game.draftPickState.get.bannedCharacters shouldBe player1Bans
          game.draftPickState.get.bans shouldBe
            Map(usernames(0) -> None, usernames(1) -> Some(player1Bans), usernames(2) -> None)
        }

        auth(2)

        {
          val game = fetchAndParseGame(lobbyId)
          game.draftPickState.get.bannedCharacters shouldBe Set()
          game.draftPickState.get.bans shouldBe
            Map(usernames(0) -> None, usernames(1) -> None, usernames(2) -> None)
        }

        auth(2)
        ban(lobbyId, Set(availableCharacters.head)).statusCode shouldBe ok

        // check
        val finalBans = Map(usernames(0) -> Some(player0Bans), usernames(1) -> Some(player1Bans), usernames(2) -> Some(player2Bans))
        val allFinalBans = player0Bans ++ player1Bans ++ player2Bans
        auth(0)

        {
          val game = fetchAndParseGame(lobbyId)
          game.draftPickState.get.bannedCharacters shouldBe allFinalBans
          game.draftPickState.get.bans shouldBe finalBans
        }

        auth(1)

        {
          val game = fetchAndParseGame(lobbyId)
          game.draftPickState.get.bannedCharacters shouldBe allFinalBans
          game.draftPickState.get.bans shouldBe finalBans
        }

        auth(2)

        {
          val game = fetchAndParseGame(lobbyId)
          game.draftPickState.get.bannedCharacters shouldBe allFinalBans
          game.draftPickState.get.bans shouldBe finalBans
        }

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
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(maxPickTimeMillis = maxPickTimeMillis)),
      )

      withGameWS {
        auth(0)

        Thread.sleep(150)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clockConfig.maxPickTimeMillis shouldBe maxPickTimeMillis
          gameState.blindPickState.get.pickPhase shouldBe BlindPickPhase.Picking
          logger.info(gameState.players.toString())
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
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(maxPickTimeMillis = maxPickTimeMillis)),
      )

      withGameWS {
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        auth(0)
        blindPick(lobbyId, availableCharacters.take(numberOfCharacters).toSet)

        auth(2)
        blindPick(lobbyId, availableCharacters.take(numberOfCharacters).toSet)

        Thread.sleep(500)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.players.map(_.victoryStatus) shouldBe Seq(VictoryStatus.Drawn, VictoryStatus.Lost, VictoryStatus.Drawn)
        }
      }
    }

    "allow pausing with blind pick" in {
      val numberOfCharacters = 2

      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfPlayers = 3,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 500)),
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        val charactersToPick = availableCharacters.take(numberOfCharacters).toSet

        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        pause(lobbyId)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.isRunning shouldBe false

          val playerTimes = (0 to 2).map(i => gameState.clock.playerTimes(usernames(i)))
          playerTimes.toSet should have size 1 // times elapsed should be the same for all players in blind pick

          fetchAndParseGame(lobbyId).clock.playerTimes(usernames(0)) shouldBe playerTimes(0)
        }

        pause(lobbyId)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.isRunning shouldBe true
          val playerTimes = (0 to 2).map(i => gameState.clock.playerTimes(usernames(i)))
          playerTimes.toSet should have size 1 // times elapsed should be the same for all players in blind pick
        }


        auth(2)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(1)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.gameStatus shouldBe GameStatus.CharacterPicked
        }

        auth(0)
        pause(lobbyId)

        Thread.sleep(1000)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.isRunning shouldBe false
          gameState.gameStatus shouldBe GameStatus.CharacterPicked

          val playerTimes = (0 to 2).map(i => gameState.clock.playerTimes(usernames(i)))
          playerTimes.toSet should have size 1 // times elapsed should be the same for all players after pick
        }

        pause(lobbyId)
        Thread.sleep(1000)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.isRunning shouldBe true
          gameState.gameStatus shouldBe GameStatus.CharacterPlacing
          val playerTimes = (0 to 2).map(i => gameState.clock.playerTimes(usernames(i)))
          playerTimes.toSet should have size 1 // times elapsed should be the same for all players in characterOpt placing
        }
      }
    }

    "allow pausing with draft pick" in {
      val lobbyId = createLobbyForGame(
        pickType = PickType.DraftPick,
        numberOfPlayers = 3,
        numberOfBans = 2,
        numberOfCharacters = 2,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.DraftPick).copy(timeAfterPickMillis = 500)),
      )

      withGameWS {
        val availableCharacters = fetchAndParseGame(lobbyId).draftPickState.get.config.availableCharacters.toSeq

        auth(0)
        ban(lobbyId, Set()).statusCode shouldBe ok

        auth(1)
        ban(lobbyId, Set(availableCharacters(8), availableCharacters(9))).statusCode shouldBe ok

        auth(0)
        pause(lobbyId)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.isRunning shouldBe false

          val playerTimes = (0 to 2).map(i => gameState.clock.playerTimes(usernames(i)))
          playerTimes.toSet should have size 1 // times elapsed should be the same for all players in ban phase

          fetchAndParseGame(lobbyId).clock.playerTimes(usernames(0)) shouldBe playerTimes(0)
        }

        pause(lobbyId)

        auth(2)
        ban(lobbyId, Set(availableCharacters(10))).statusCode shouldBe ok

        auth(0)

        pause(lobbyId)

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.clock.isRunning shouldBe false

          val playerTimes = (0 to 2).map(i => gameState.clock.playerTimes(usernames(i)))

          fetchAndParseGame(lobbyId).clock.playerTimes(usernames(0)) shouldBe playerTimes(0)
          playerTimes(0) should be < playerTimes(1) // each player pick time is individual
        }

        pause(lobbyId)

        pick(lobbyId, availableCharacters(0)).statusCode shouldBe ok

        auth(1)
        pick(lobbyId, availableCharacters(1)).statusCode shouldBe ok

        auth(2)
        pick(lobbyId, availableCharacters(2)).statusCode shouldBe ok
        pick(lobbyId, availableCharacters(3)).statusCode shouldBe ok

        auth(1)
        pick(lobbyId, availableCharacters(4)).statusCode shouldBe ok

        auth(0)
        pick(lobbyId, availableCharacters(5)).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.draftPickState.get.pickPhase shouldBe DraftPickPhase.Finished
          gameState.gameStatus shouldBe GameStatus.CharacterPicked
        }
      }
    }

    "allow placing characters" in {
      val numberOfPlayers = 3
      val numberOfCharacters = 2

      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfPlayers = numberOfPlayers,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 1)),
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        val charactersToPick = availableCharacters.take(numberOfCharacters).toSet

        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(1)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(2)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        Thread.sleep(150)

        val (hexMap, players) = {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.gameStatus shouldBe GameStatus.CharacterPlacing
          (gameState.hexMap, gameState.players)
        }

        (0 until numberOfPlayers) foreach { i =>
          auth(i)
          val spawnPoints = hexMap.getSpawnPointsByNumber(i)
          val characterIds = players(i).characterIds
          val coordinatesToCharacterIdMap = spawnPoints.map(_.coordinates).zip(characterIds).toMap
          placeCharacters(lobbyId, coordinatesToCharacterIdMap).statusCode shouldBe ok
          placeCharacters(lobbyId, coordinatesToCharacterIdMap).statusCode shouldBe nok
        }

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.gameStatus shouldBe GameStatus.Running
        }
      }
    }

    "place all characters in all random pick" in {
      val gameId = createLobbyForGame(
        pickType = PickType.AllRandom,
        hexMapName = "Shuriken",
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.AllRandom).copy(timeAfterPickMillis = 1)),
        numberOfCharacters = 2,
        numberOfPlayers = 2,
      )


      withGameWS {
        auth(0)

        Thread.sleep(150)

        val gs = fetchAndParseGame(gameId)

        gs.characterIdsOutsideMap.size should be (0)
        gs.hexMap.getSpawnPoints.flatMap(_.characterId).size should be (4)
        gs.gameStatus should be (GameStatus.Running)
      }
    }

    "allow moving characters" in {
      val numberOfPlayers = 3
      val numberOfCharacters = 2

      val lobbyId = createLobbyForGame(
        pickType = PickType.BlindPick,
        numberOfPlayers = numberOfPlayers,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 1)),
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        val charactersToPick = availableCharacters.take(numberOfCharacters).toSet

        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(1)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        auth(2)
        blindPick(lobbyId, charactersToPick).statusCode shouldBe ok

        Thread.sleep(150)

        {
          val (hexMap, players) = {
            val gameState = fetchAndParseGame(lobbyId)
            gameState.gameStatus shouldBe GameStatus.CharacterPlacing
            (gameState.hexMap, gameState.players)
          }

          (0 until numberOfPlayers) foreach { i =>
            auth(i)
            val spawnPoints = hexMap.getSpawnPointsByNumber(i)
            val characterIds = players(i).characterIds
            val coordinatesToCharacterIdMap = spawnPoints.map(_.coordinates).zip(characterIds).toMap
            placeCharacters(lobbyId, coordinatesToCharacterIdMap).statusCode shouldBe ok
            placeCharacters(lobbyId, coordinatesToCharacterIdMap).statusCode shouldBe nok
          }
        }

        auth(0)

        val gameState = fetchAndParseGame(lobbyId)

        val characterToMove = gameState.players(0).characterIds.head
        val characterCell = gameState.hexMap.getCellOfCharacter(characterToMove).get
        val targetCell = NkmUtils.getAdjacentCells(gameState.hexMap.cells, characterCell.coordinates)
          .filter(c => c.cellType == HexCellType.Normal).head
        moveCharacter(lobbyId, Seq(characterCell, targetCell).map(_.coordinates), characterToMove).statusCode shouldBe ok
        moveCharacter(lobbyId, Seq(characterCell, targetCell).map(_.coordinates), characterToMove).statusCode shouldBe nok
      }
    }

    "allow basic attacking with characters" in {
      val numberOfPlayers = 2
      val numberOfCharacters = 1

      val lobbyId = createLobbyForGame(
        hexMapName = "TestMap",
        pickType = PickType.BlindPick,
        numberOfPlayers = numberOfPlayers,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 1)),
      )

      withGameWS {
        auth(0)
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        // reverse hack to get someone with sufficient attack range
        val charactersToPick = availableCharacters.reverse.take(numberOfCharacters).toSet

        for (i <- 0 until numberOfPlayers) {
          auth(i)
          blindPick(lobbyId, charactersToPick).statusCode shouldBe ok
        }

        Thread.sleep(150)

        {
          val players = fetchAndParseGame(lobbyId).players

          val characterId0 = players(0).characterIds.head
          val characterId1 = players(1).characterIds.head

          auth(0)
          placeCharacters(lobbyId, Map(HexCoordinates(3, 10) -> characterId0)).statusCode shouldBe ok
          auth(1)
          placeCharacters(lobbyId, Map(HexCoordinates(6, 10) -> characterId1)).statusCode shouldBe ok
        }

        auth(0)

        val gameState = fetchAndParseGame(lobbyId)

        val characterToMove = gameState.players(0).characterIds.head
        val characterToAttack = gameState.players(1).characterIds.head

        basicAttackCharacter(lobbyId, characterToMove, characterToAttack).statusCode shouldBe ok
      }
    }

    "allow turn ending" in {
      val numberOfPlayers = 2
      val numberOfCharacters = 1

      val lobbyId = createLobbyForGame(
        hexMapName = "TestMap",
        pickType = PickType.BlindPick,
        numberOfPlayers = numberOfPlayers,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 1)),
      )

      withGameWS {
        endTurn(lobbyId).statusCode shouldBe unauthorized
        auth(0)
        endTurn(lobbyId).statusCode shouldBe nok
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        // reverse hack to get someone with sufficient attack range
        val charactersToPick = availableCharacters.reverse.take(numberOfCharacters).toSet

        for (i <- 0 until numberOfPlayers) {
          auth(i)
          blindPick(lobbyId, charactersToPick).statusCode shouldBe ok
        }
        endTurn(lobbyId).statusCode shouldBe nok

        Thread.sleep(150)

        val (character0, character1) = {
          val gameState = fetchAndParseGame(lobbyId)

          val character0 = gameState.players(0).characterIds.head
          val character1 = gameState.players(1).characterIds.head
          (character0, character1)
        }

        {
          auth(0)
          placeCharacters(lobbyId, Map(HexCoordinates(3, 10) -> character0)).statusCode shouldBe ok
          endTurn(lobbyId).statusCode shouldBe nok
          auth(1)
          placeCharacters(lobbyId, Map(HexCoordinates(6, 10) -> character1)).statusCode shouldBe ok
          endTurn(lobbyId).statusCode shouldBe nok
        }

        auth(0)

        {
          val gameState = fetchAndParseGame(lobbyId)

          gameState.turn.number shouldBe 0
          gameState.phase.number shouldBe 0
        }

        endTurn(lobbyId).statusCode shouldBe nok
        basicAttackCharacter(lobbyId, character0, character1).statusCode shouldBe ok
        endTurn(lobbyId).statusCode shouldBe ok
        endTurn(lobbyId).statusCode shouldBe nok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.turn.number shouldBe 1
          gameState.phase.number shouldBe 0
        }

        auth(1)
        endTurn(lobbyId).statusCode shouldBe nok
        basicAttackCharacter(lobbyId, character1, character0).statusCode shouldBe ok
        endTurn(lobbyId).statusCode shouldBe ok
        endTurn(lobbyId).statusCode shouldBe nok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.turn.number shouldBe 2
          gameState.phase.number shouldBe 1
        }
      }
    }

    "allow turn passing" in {
      val numberOfPlayers = 2
      val numberOfCharacters = 1

      val lobbyId = createLobbyForGame(
        hexMapName = "TestMap",
        pickType = PickType.BlindPick,
        numberOfPlayers = numberOfPlayers,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 1)),
      )

      withGameWS {
        passTurn(lobbyId, "random_id").statusCode shouldBe unauthorized
        auth(0)
        passTurn(lobbyId, "random_id").statusCode shouldBe nok
        val availableCharacters = fetchAndParseGame(lobbyId).blindPickState.get.config.availableCharacters.toSeq
        val charactersToPick = availableCharacters.take(numberOfCharacters).toSet

        for (i <- 0 until numberOfPlayers) {
          auth(i)
          blindPick(lobbyId, charactersToPick).statusCode shouldBe ok
        }
        passTurn(lobbyId, "random_id").statusCode shouldBe nok

        Thread.sleep(150)

        val (character0, character1) = {
          val gameState = fetchAndParseGame(lobbyId)

          val character0 = gameState.players(0).characterIds.head
          val character1 = gameState.players(1).characterIds.head
          (character0, character1)
        }

        {
          auth(0)
          placeCharacters(lobbyId, Map(HexCoordinates(3, 10) -> character0)).statusCode shouldBe ok
          passTurn(lobbyId, character0).statusCode shouldBe nok
          auth(1)
          placeCharacters(lobbyId, Map(HexCoordinates(6, 10) -> character1)).statusCode shouldBe ok
          passTurn(lobbyId, character1).statusCode shouldBe nok
        }

        auth(0)

        {
          val gameState = fetchAndParseGame(lobbyId)

          gameState.turn.number shouldBe 0
          gameState.phase.number shouldBe 0
        }

        passTurn(lobbyId, character1).statusCode shouldBe nok
        passTurn(lobbyId, character0).statusCode shouldBe ok
        passTurn(lobbyId, character0).statusCode shouldBe nok
        passTurn(lobbyId, character1).statusCode shouldBe nok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.turn.number shouldBe 1
          gameState.phase.number shouldBe 0
        }

        auth(1)
        passTurn(lobbyId, character0).statusCode shouldBe nok
        passTurn(lobbyId, character1).statusCode shouldBe ok

        {
          val gameState = fetchAndParseGame(lobbyId)
          gameState.turn.number shouldBe 2
          gameState.phase.number shouldBe 1
        }
      }
    }

    "allow using abilities on characters" in {
      val numberOfPlayers = 2
      val numberOfCharacters = 1

      val lobbyId = createLobbyForGame(
        hexMapName = "TestMap",
        pickType = PickType.BlindPick,
        numberOfPlayers = numberOfPlayers,
        numberOfCharacters = numberOfCharacters,
        clockConfigOpt = Some(ClockConfig.defaultForPickType(PickType.BlindPick).copy(timeAfterPickMillis = 1)),
      )

      withGameWS {
        auth(0)
        val charactersToPick = Set("Roronoa Zoro")

        for (i <- 0 until numberOfPlayers) {
          auth(i)
          blindPick(lobbyId, charactersToPick).statusCode shouldBe ok
        }

        Thread.sleep(150)

        val (ability0, character0, character1) = {
          val gameState = fetchAndParseGame(lobbyId)

          val character0 = gameState.players(0).characterIds.head
          val character1 = gameState.players(1).characterIds.head
          val ability0 = gameState.abilities.find(a => a.parentCharacterId == character0 && a.metadataId == "Ogre Cutter").get.id
          (ability0, character0, character1)
        }

        {
          auth(0)
          placeCharacters(lobbyId, Map(HexCoordinates(3, 10) -> character0)).statusCode shouldBe ok
          endTurn(lobbyId).statusCode shouldBe nok
          auth(1)
          placeCharacters(lobbyId, Map(HexCoordinates(6, 10) -> character1)).statusCode shouldBe ok
          endTurn(lobbyId).statusCode shouldBe nok
        }

        auth(0)

        useAbilityOnCharacter(lobbyId, ability0, character1).statusCode shouldBe ok
      }
    }
  }
}
