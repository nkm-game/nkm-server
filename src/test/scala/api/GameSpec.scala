package api

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.PickType._
import helpers.GameApiTrait

import scala.language.postfixOps

class GameSpec extends GameApiTrait
{
  "API" must {
    "allow initializing game" in {
      initGame()
    }

    "initialize characters randomly in all random pick" in {
      initGame(pickType = AllRandom, numberOfCharacters = 3)

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        val gameState = responseAs[GameState]
        gameState.gamePhase shouldEqual GamePhase.CharacterPlacing
        gameState.players.forall(p => p.characters.length == 3) shouldEqual true
        gameState.characterIdsOutsideMap.length shouldEqual 6
        val allCharacterNames = gameState.players.flatMap(p => p.characters).map(c => c.state.name)
        val allCharacterNamesUnique = allCharacterNames.toSet
        allCharacterNames.length shouldEqual allCharacterNamesUnique.toList.length
      }
    }

    "allow to blind pick characters" in {
      initGame(pickType = BlindPick)
      // TODO: get list of available characters to pick
      fail()

//      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
//        val gameState = responseAs[GameState]
//        gameState.gamePhase shouldEqual GamePhase.Running
//      }
    }

    "allow to place characters" in {
      initGame()
      var gameState: GameState = GameState.empty("null")

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        gameState = responseAs[GameState]
      }

      val firstPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(0)
      val targetCellCoordinates = firstPlayerSpawnPoints(0).coordinates
      val characterToPlace = gameState.players(0).characters(0).id

      Post("/api/place_character", PlaceCharacterRequest(lobbyId, targetCellCoordinates, characterToPlace)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
    }

    "disallow to place characters on other cells than spawnpoint" in {
      initGame()
      var gameState: GameState = GameState.empty("null")

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        gameState = responseAs[GameState]
      }
      val characterToPlace = gameState.players(0).characters(0).id

      val secondPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(1)
      val someHexCoordinates = HexCoordinates(1, 1)
      val candidateCoordinates = List(secondPlayerSpawnPoints(0).coordinates, someHexCoordinates)

      candidateCoordinates.foreach { c =>
        checkPostRequest("/api/place_character", PlaceCharacterRequest(lobbyId, c, characterToPlace), InternalServerError, 0)
      }
    }

    "disallow to place other characters" in {
      initGame()
      var gameState: GameState = GameState.empty("null")

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        gameState = responseAs[GameState]
      }
      val characterToPlace = gameState.players(1).characters(0).id

      val secondPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(0)
      val firstPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(0)

      val candidateCoordinates = List(secondPlayerSpawnPoints(0).coordinates, firstPlayerSpawnPoints(0).coordinates)

      candidateCoordinates.foreach { c =>
        checkPostRequest("/api/place_character", PlaceCharacterRequest(lobbyId, c, characterToPlace), InternalServerError, 0)
      }
    }

    "disallow to place characters outside your turn" in {
      initGame()
      var gameState: GameState = GameState.empty("null")

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(1))) ~> routes ~> check {
        gameState = responseAs[GameState]
      }
      val characterToPlace1 = gameState.players(0).characters(0).id
      val characterToPlace2 = gameState.players(1).characters(0).id
      val candidateCharactersToPlace = List(characterToPlace1, characterToPlace2)

      val secondPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(0)
      val firstPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(0)

      val candidateCoordinates = List(secondPlayerSpawnPoints(0).coordinates, firstPlayerSpawnPoints(0).coordinates)

      candidateCharactersToPlace.foreach { ca =>
        candidateCoordinates.foreach { c =>
          checkPostRequest("/api/place_character", PlaceCharacterRequest(lobbyId, c, ca), InternalServerError, 1)
        }
      }
    }

    "make turn change after placing a character" in {
      initGame()
      var gameState: GameState = GameState.empty("null")

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        gameState = responseAs[GameState]
      }

      val firstPlayerSpawnPoints = gameState.hexMap.get.getSpawnPointsByNumber(0)
      val targetCellCoordinates = firstPlayerSpawnPoints(0).coordinates
      val characterToPlace = gameState.players(0).characters(0).id

      Post("/api/place_character", PlaceCharacterRequest(lobbyId, targetCellCoordinates, characterToPlace)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

      Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        gameState = responseAs[GameState]
      }
      gameState.getCurrentPlayerNumber shouldEqual 1
    }
  }
}
