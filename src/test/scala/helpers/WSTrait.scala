package helpers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.ws._
import com.tosware.NKM.models.lobby.LobbyState
import com.tosware.NKM.models.lobby.ws._
import spray.json._

trait WSTrait extends UserApiTrait {
  implicit var wsClient: WSProbe = WSProbe()

  val wsPrefix = "/ws"
  val wsLobbyUri = s"$wsPrefix/lobby"
  val wsGameUri = s"$wsPrefix/game"

  val ok = StatusCodes.OK.intValue
  val nok = StatusCodes.InternalServerError.intValue
  val unauthorized = StatusCodes.Unauthorized.intValue

  def withWS[T](wsUri: String, body: => T): T = {
    wsClient = WSProbe()
    WS(wsUri, wsClient.flow) ~> routes ~> check(body)
  }

  def withLobbyWS[T](body: => T): T = withWS(wsLobbyUri, body)

  def withGameWS[T](body: => T): T = withWS(wsGameUri, body)

  def sendRequestL(request: WebsocketLobbyRequest): Unit = {
    println(request.toJson.toString)
    wsClient.sendMessage(request.toJson.toString)
  }

  def sendRequestG(request: WebsocketGameRequest): Unit = {
    println(request.toJson.toString)
    wsClient.sendMessage(request.toJson.toString)
  }

  def fetchResponseL(): WebsocketLobbyResponse =
    wsClient.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[WebsocketLobbyResponse]

  def fetchResponseG(): WebsocketGameResponse = {
    wsClient.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[WebsocketGameResponse]
  }

  def sendWSRequestL(route: LobbyRoute, requestJson: String = ""): WebsocketLobbyResponse = {
    sendRequestL(WebsocketLobbyRequest(route, requestJson))
    fetchResponseL()
  }


  def sendWSRequestG(route: GameRoute, requestJson: String = ""): WebsocketGameResponse = {
    sendRequestG(WebsocketGameRequest(route, requestJson))
    fetchResponseG()
  }

  def authL(tokenId: Int): WebsocketLobbyResponse = {
    import com.tosware.NKM.models.lobby.ws.AuthRequest
    val response = sendWSRequestL(LobbyRoute.Auth, AuthRequest(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def authG(tokenId: Int): WebsocketGameResponse = {
    import com.tosware.NKM.models.game.ws.AuthRequest
    val response = sendWSRequestG(GameRoute.Auth, AuthRequest(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def observeL(lobbyId: String): WebsocketLobbyResponse = {
    import com.tosware.NKM.models.lobby.ws.ObserveRequest
    sendWSRequestL(LobbyRoute.Observe, ObserveRequest(lobbyId).toJson.toString)
  }

  def observeG(gameId: String): WebsocketGameResponse = {
    import com.tosware.NKM.models.game.ws.ObserveRequest
    sendWSRequestG(GameRoute.Observe, ObserveRequest(gameId).toJson.toString)
  }

  def createLobby(lobbyName: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.CreateLobby, LobbyCreationRequest(lobbyName).toJson.toString)

  def fetchLobbies(): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.Lobbies)

  def fetchLobby(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.Lobby, GetLobbyRequest(lobbyId).toJson.toString)

  def joinLobby(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.JoinLobby, LobbyJoinRequest(lobbyId).toJson.toString)

  def leaveLobby(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.LeaveLobby, LobbyLeaveRequest(lobbyId).toJson.toString)

  def setHexMap(lobbyId: String, hexMapName: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetHexMap, SetHexMapNameRequest(lobbyId, hexMapName).toJson.toString)

  def setPickType(lobbyId: String, pickType: PickType): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetPickType, SetPickTypeRequest(lobbyId, pickType).toJson.toString)

  def setNumberOfBans(lobbyId: String, numberOfBans: Int): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetNumberOfBans, SetNumberOfBansRequest(lobbyId, numberOfBans).toJson.toString)

  def setNumberOfCharacters(lobbyId: String, numberOfCharacters: Int): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetNumberOfCharacters, SetNumberOfCharactersPerPlayerRequest(lobbyId, numberOfCharacters).toJson.toString)

  def setLobbyName(lobbyId: String, newName: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetLobbyName, SetLobbyNameRequest(lobbyId, newName).toJson.toString)

  def startGame(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.StartGame, StartGameRequest(lobbyId).toJson.toString)

  def fetchAndParseLobby(lobbyId: String): LobbyState = {
    val lobbyResponse = fetchLobby(lobbyId)
    lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
    lobbyResponse.body.parseJson.convertTo[LobbyState]
  }

  def surrender(gameId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Surrender, SurrenderRequest(gameId).toJson.toString)

  def ban(gameId: String, characterIds: Set[CharacterMetadataId]): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BanCharacters, BanCharactersRequest(gameId, characterIds).toJson.toString)

  def pick(gameId: String, characterId: CharacterMetadataId): WebsocketGameResponse =
    sendWSRequestG(GameRoute.PickCharacter, PickCharacterRequest(gameId, characterId).toJson.toString)

  def blindPick(gameId: String, characterIds: Set[CharacterMetadataId]): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BlindPickCharacters, BlindPickCharactersRequest(gameId, characterIds).toJson.toString)

  def fetchGame(gameId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.GetState, GetStateRequest(gameId).toJson.toString)

  def fetchAndParseGame(gameId: String): GameState = {
    val gameResponse = fetchGame(gameId)
    gameResponse.statusCode shouldBe StatusCodes.OK.intValue
    gameResponse.body.parseJson.convertTo[GameState]
  }
}
