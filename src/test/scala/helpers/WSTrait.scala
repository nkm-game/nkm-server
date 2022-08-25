package helpers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.nkm.models.game.{ClockConfig, GameStateView, PickType}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.ws.GameRequest.Action.{BasicAttack, EndTurn}
//import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ws.GameRequest.Action.{Move, PlaceCharacters}
import com.tosware.nkm.models.game.ws.GameRequest.CharacterSelect._
import com.tosware.nkm.models.game.ws.GameRequest.General._
import com.tosware.nkm.models.game.ws._
import com.tosware.nkm.models.lobby.LobbyState
import com.tosware.nkm.models.lobby.ws._
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
    logger.info(request.toJson.toString)
    wsClient.sendMessage(request.toJson.toString)
  }

  def sendRequestG(request: WebsocketGameRequest): Unit = {
    logger.info(request.toJson.toString)
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
    val response = sendWSRequestL(LobbyRoute.Auth, LobbyRequest.Auth(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def authG(tokenId: Int): WebsocketGameResponse = {
    val response = sendWSRequestG(GameRoute.Auth, GameRequest.General.Auth(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def observeL(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.Observe, LobbyRequest.Observe(lobbyId).toJson.toString)

  def observeG(lobbyId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Observe, GameRequest.General.Observe(lobbyId).toJson.toString)

  def createLobby(lobbyName: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.CreateLobby, LobbyRequest.LobbyCreation(lobbyName).toJson.toString)

  def fetchLobbies(): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.Lobbies)

  def fetchLobby(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.Lobby, LobbyRequest.GetLobby(lobbyId).toJson.toString)

  def joinLobby(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.JoinLobby, LobbyRequest.LobbyJoin(lobbyId).toJson.toString)

  def leaveLobby(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.LeaveLobby, LobbyRequest.LobbyLeave(lobbyId).toJson.toString)

  def setHexMap(lobbyId: String, hexMapName: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetHexMap, LobbyRequest.SetHexMapName(lobbyId, hexMapName).toJson.toString)

  def setPickType(lobbyId: String, pickType: PickType): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetPickType, LobbyRequest.SetPickType(lobbyId, pickType).toJson.toString)

  def setNumberOfBans(lobbyId: String, numberOfBans: Int): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetNumberOfBans, LobbyRequest.SetNumberOfBans(lobbyId, numberOfBans).toJson.toString)

  def setNumberOfCharacters(lobbyId: String, numberOfCharacters: Int): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetNumberOfCharacters, LobbyRequest.SetNumberOfCharactersPerPlayer(lobbyId, numberOfCharacters).toJson.toString)

  def setLobbyName(lobbyId: String, newName: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetLobbyName, LobbyRequest.SetLobbyName(lobbyId, newName).toJson.toString)

  def setClockConfig(lobbyId: String, newConfig: ClockConfig): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetClockConfig, LobbyRequest.SetClockConfig(lobbyId, newConfig).toJson.toString)

  def startGame(lobbyId: String): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.StartGame, LobbyRequest.StartGame(lobbyId).toJson.toString)

  def fetchAndParseLobby(lobbyId: String): LobbyState = {
    val lobbyResponse = fetchLobby(lobbyId)
    lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
    lobbyResponse.body.parseJson.convertTo[LobbyState]
  }

  def pause(lobbyId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Pause, Pause(lobbyId).toJson.toString)

  def surrender(lobbyId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Surrender, Surrender(lobbyId).toJson.toString)

  def ban(lobbyId: String, characterIds: Set[CharacterMetadataId]): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BanCharacters, BanCharacters(lobbyId, characterIds).toJson.toString)

  def pick(lobbyId: String, characterId: CharacterMetadataId): WebsocketGameResponse =
    sendWSRequestG(GameRoute.PickCharacter, PickCharacter(lobbyId, characterId).toJson.toString)

  def blindPick(lobbyId: String, characterIds: Set[CharacterMetadataId]): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BlindPickCharacters, BlindPickCharacters(lobbyId, characterIds).toJson.toString)

  def placeCharacters(lobbyId: String, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]): WebsocketGameResponse =
    sendWSRequestG(GameRoute.PlaceCharacters, PlaceCharacters(lobbyId, coordinatesToCharacterIdMap).toJson.toString)

  def moveCharacter(lobbyId: String, path: Seq[HexCoordinates], characterId: CharacterId): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Move, Move(lobbyId, path, characterId).toJson.toString)

  def basicAttackCharacter(lobbyId: String, attackingCharacterId: CharacterId, targetCharacterId: CharacterId): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BasicAttack, BasicAttack(lobbyId, attackingCharacterId, targetCharacterId).toJson.toString)

  def endTurn(lobbyId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.EndTurn, EndTurn(lobbyId).toJson.toString)

  def fetchGame(lobbyId: String): WebsocketGameResponse =
    sendWSRequestG(GameRoute.GetState, GetState(lobbyId).toJson.toString)

  def fetchAndParseGame(lobbyId: String): GameStateView = {
    val gameResponse = fetchGame(lobbyId)
    gameResponse.statusCode shouldBe StatusCodes.OK.intValue
    gameResponse.body.parseJson.convertTo[GameStateView]
  }
}
