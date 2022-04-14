package ws

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.models.lobby.ws.{LobbyCreationRequest, LobbyRoute, WebsocketLobbyRequest}
import helpers.WSTrait

class WSGameSpec extends WSTrait
{
  import spray.json._

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
      var gameId = ""
      withLobbyWS {
        authL(0)
        gameId = createLobby(lobbyName).body
      }

      withGameWS {
        auth(0)
        observe(gameId).statusCode shouldBe StatusCodes.OK.intValue
        // TODO
//        setLobbyName(gameId, "hi") shouldBe WebsocketLobbyResponse(LobbyResponseType.SetLobbyName, StatusCodes.OK.intValue)

        val observedResponse = fetchResponse()
        // TODO
//        observedResponse.lobbyResponseType shouldBe LobbyResponseType.Lobby
        observedResponse.statusCode shouldBe StatusCodes.OK.intValue
      }
    }
  }
}
