package helpers

import com.tosware.nkm.models.lobby.ws.LobbyRequest.LobbyCreation

trait LobbyApiTrait extends UserApiTrait {
  val lobbyName = "lobby_name"
  var lobbyId: String = ""
  override def beforeEach(): Unit = {
    super.beforeEach()

    Post("/api/create_lobby", LobbyCreation(lobbyName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
      lobbyId = responseAs[String]
    }
  }
}
