package api

import com.tosware.NKM.models.game.{GamePhase, GameState}
import com.tosware.NKM.models.game.PickType.{AllRandom, BlindPick}
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
        gameState.gamePhase shouldEqual GamePhase.Running
        gameState.players.forall(p => p.characters.length == 3) shouldEqual true
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
  }
}
