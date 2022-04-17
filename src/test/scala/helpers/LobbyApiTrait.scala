package helpers

import com.tosware.NKM.models.lobby.ws.LobbyCreationRequest

trait LobbyApiTrait extends UserApiTrait
  {
    val lobbyName = "lobby_name"
    var lobbyId: String = ""
    override def beforeEach(): Unit = {
      super.beforeEach()

      Post("/api/create_lobby", LobbyCreationRequest(lobbyName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        lobbyId = responseAs[String]
      }
    }
  }
