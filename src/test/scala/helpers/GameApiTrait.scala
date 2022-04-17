package helpers

import akka.http.scaladsl.model.StatusCodes.OK
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.lobby.ws._

trait GameApiTrait extends LobbyApiTrait {
  def initGame(numberOfPlayers: Int = 2, hexMapName: String = "Linia", pickType: PickType = PickType.AllRandom, numberOfBans: Int = 0, numberOfCharacters: Int = 1) = {
    Post("/api/set_hexmap", SetHexMapNameRequest(lobbyId, hexMapName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
    Post("/api/set_pick_type", SetPickTypeRequest(lobbyId, pickType)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
    Post("/api/set_number_of_bans", SetNumberOfBansRequest(lobbyId, numberOfBans)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)
    Post("/api/set_number_of_characters", SetNumberOfCharactersPerPlayerRequest(lobbyId, numberOfCharacters)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

    for (i <- 1 until numberOfPlayers) {
      Post("/api/join_lobby", LobbyJoinRequest(lobbyId)).addHeader(getAuthHeader(tokens(i))) ~> routes ~> check(status shouldEqual OK)
    }

    Post("/api/start_game", StartGameRequest(lobbyId)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check(status shouldEqual OK)

    Get(s"/api/state/$lobbyId").addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
      val gameState = responseAs[GameState]
      gameState.gamePhase shouldEqual (if (pickType == PickType.AllRandom) GamePhase.CharacterPlacing else GamePhase.CharacterPick)
      gameState.players.length shouldEqual numberOfPlayers
      gameState.hexMap.get.name shouldEqual hexMapName
      gameState.numberOfBans shouldEqual numberOfBans
      gameState.numberOfCharactersPerPlayers shouldEqual numberOfCharacters
      gameState.pickType shouldEqual pickType
    }
  }
}
