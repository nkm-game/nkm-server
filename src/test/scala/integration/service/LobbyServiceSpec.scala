package integration.service

import com.tosware.nkm.*
import com.tosware.nkm.models.CommandResponse
import com.tosware.nkm.models.game.ClockConfig
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.lobby.ws.LobbyRequest
import helpers.ApiTrait

class LobbyServiceSpec extends ApiTrait {
  val hostUserName = "host"

  def createLobby(): GameId =
    deps.lobbyService.createLobby(hostUserName, LobbyRequest.CreateLobby("test_lobby")) match {
      case CommandResponse.Success(lobbyId) => lobbyId
      case CommandResponse.Failure(msg)     => fail(msg)
    }

  "LobbyService" must {
    "be able to create lobbies" in {
      createLobby()
    }
    "return all lobbies" in {
      deps.lobbyService.getAllLobbies() map { lobbies => lobbies should be(empty) }
      createLobby()
      deps.lobbyService.getAllLobbies() map { lobbies => lobbies.size should be(1) }
      createLobby()
      createLobby()
      deps.lobbyService.getAllLobbies() map { lobbies => lobbies.size should be(3) }
    }
    "allow setting clock config in a lobby" in {
      val gameId = createLobby()

      val validClockConfig = ClockConfig.defaultForPickType(PickType.DraftPick)

      deps.lobbyService.setClockConfig(
        "host",
        LobbyRequest.SetClockConfig(gameId, validClockConfig),
      ).toBoolean should be(true)
    }
    "disallow setting too big clock configs in a lobby" in {
      val gameId = createLobby()

      val invalidClockConfig =
        ClockConfig
          .defaultForPickType(PickType.DraftPick).copy()
          .copy(initialTimeMillis = 99999999999999999L)

      deps.lobbyService.setClockConfig(
        "host",
        LobbyRequest.SetClockConfig(gameId, invalidClockConfig),
      ).toBoolean should be(false)
    }
  }
}
