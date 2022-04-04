package ws

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.DBManager
import com.tosware.NKM.models.game.GameState
import com.tosware.NKM.models.game.ws._
import helpers.UserApiTrait
import spray.json._

class WSGameSpec extends UserApiTrait
{
  val wsPrefix = "/ws"
  val wsUri = s"$wsPrefix/game"

  def sendRequest(request: WebsocketGameRequest)(implicit wsClient: WSProbe): Unit = {
    println(request.toJson.toString)
    wsClient.sendMessage(request.toJson.toString)
  }

  def fetchResponse()(implicit wsClient: WSProbe): WebsocketGameResponse = {
    val response = wsClient.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[WebsocketGameResponse]
    response
  }

  def sendWSRequest(route: GameRoute, requestJson: String = "")(implicit wsClient: WSProbe): WebsocketGameResponse = {
    sendRequest(WebsocketGameRequest(route, requestJson))
    fetchResponse()
  }

  def auth(tokenId: Int)(implicit wsClient: WSProbe): WebsocketGameResponse = {
    val response = sendWSRequest(GameRoute.Auth, AuthRequest(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def observe(gameId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequest(GameRoute.Observe, ObserveRequest(gameId).toJson.toString)

  def fetchGame(gameId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequest(GameRoute.GetState, GetStateRequest(gameId).toJson.toString)

  def fetchAndParseGame(gameId: String)(implicit wsClient: WSProbe): GameState = {
    // wait for CQRS Event Handler to persist
    Thread.sleep(DBManager.dbTimeout.toMillis)
    val gameResponse = fetchGame(gameId)
    gameResponse.statusCode shouldBe StatusCodes.OK.intValue
    gameResponse.body.parseJson.convertTo[GameState]
  }

  "WS" must {
    "respond to invalid requests" in {
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~>
        check {
          wsClient.sendMessage("invalid request")
          val response = fetchResponse()
          response.statusCode shouldBe 500
          response.gameResponseType shouldBe GameResponseType.Error

          wsClient.sendCompletion()
          //          wsClient.expectCompletion()
        }
    }

    "allow authenticating" in {
      implicit val wsClient: WSProbe = WSProbe()
      WS(wsUri, wsClient.flow) ~> routes ~> check {
        auth(0).statusCode shouldBe StatusCodes.OK.intValue

        wsClient.sendCompletion()
//        wsClient.expectCompletion()
      }
    }
  }
}
