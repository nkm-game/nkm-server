package helpers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.WSProbe
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.event.GameEvent.GameEvent
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.ws.*
import com.tosware.nkm.models.game.ws.GameRequest.Action.*
import com.tosware.nkm.models.game.ws.GameRequest.CharacterSelect.*
import com.tosware.nkm.models.game.ws.GameRequest.General.*
import com.tosware.nkm.models.lobby.LobbyState
import com.tosware.nkm.models.lobby.ws.*
import spray.json.*

object WSTrait {
  val wsPrefix = "/ws"
  val wsLobbyUri = s"$wsPrefix/lobby"
  val wsGameUri = s"$wsPrefix/game"

  val ok = StatusCodes.OK.intValue
  val nok = StatusCodes.InternalServerError.intValue
  val unauthorized = StatusCodes.Unauthorized.intValue
}

trait WSTrait extends UserApiTrait {
  import WSTrait.*

  private var eventObservers: Seq[GameEventObserver] = Seq.empty

  def observeEvents(gameId: GameId): Unit = {
    eventObservers = (0 until numberOfUsers).map { tokenId =>
      new GameEventObserver(gameId, tokenId)(this)
    }
    eventObservers.foreach(_.start())
  }

  def collectObservedEvents(): Seq[Seq[GameEvent]] = {
    eventObservers.foreach(_.stop())
    eventObservers.map(_.observedEvents())
  }

  def withWS[T](wsUri: String, body: WSProbe => T): T = {
    val wsClient: WSProbe = WSProbe()
    WS(wsUri, wsClient.flow) ~> routes ~> check(body(wsClient))
  }

  def withLobbyWS[T](body: WSProbe => T): T = withWS(wsLobbyUri, body)

  def withGameWS[T](body: WSProbe => T): T = withWS(wsGameUri, body)

  def sendRequestL(request: WebsocketLobbyRequest)(implicit wsClient: WSProbe): Unit = {
    logger.info(request.toJson.toString)
    wsClient.sendMessage(request.toJson.toString)
  }

  def sendRequestG(request: WebsocketGameRequest)(implicit wsClient: WSProbe): Unit = {
    logger.info(request.toJson.toString)
    wsClient.sendMessage(request.toJson.toString)
  }

  def fetchResponseL()(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    wsClient.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[WebsocketLobbyResponse]

  def fetchResponseG()(implicit wsClient: WSProbe): WebsocketGameResponse =
    wsClient.expectMessage().asTextMessage.getStrictText.parseJson.convertTo[WebsocketGameResponse]

  def sendWSRequestL(route: LobbyRoute, requestJson: String = "")(implicit
      wsClient: WSProbe
  ): WebsocketLobbyResponse = {
    sendRequestL(WebsocketLobbyRequest(route, requestJson))
    fetchResponseL()
  }

  def sendWSRequestG(route: GameRoute, requestJson: String = "")(implicit wsClient: WSProbe): WebsocketGameResponse = {
    sendRequestG(WebsocketGameRequest(route, requestJson))
    fetchResponseG()
  }

  def authL(tokenId: Int)(implicit wsClient: WSProbe): WebsocketLobbyResponse = {
    val response = sendWSRequestL(LobbyRoute.Auth, LobbyRequest.Auth(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def authG(tokenId: Int)(implicit wsClient: WSProbe): WebsocketGameResponse = {
    val response = sendWSRequestG(GameRoute.Auth, GameRequest.General.Auth(tokens(tokenId)).toJson.toString)
    response.statusCode shouldBe StatusCodes.OK.intValue
    response
  }

  def observeL(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.Observe, LobbyRequest.Observe(lobbyId).toJson.toString)

  def observeG(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Observe, GameRequest.General.Observe(lobbyId).toJson.toString)

  def createLobby(lobbyName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.CreateLobby, LobbyRequest.CreateLobby(lobbyName).toJson.toString)

  def fetchLobbies()(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.GetLobbies)

  def fetchLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.GetLobby, LobbyRequest.GetLobby(lobbyId).toJson.toString)

  def joinLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.JoinLobby, LobbyRequest.JoinLobby(lobbyId).toJson.toString)

  def leaveLobby(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.LeaveLobby, LobbyRequest.LeaveLobby(lobbyId).toJson.toString)

  def setHexMap(lobbyId: String, hexMapName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetHexMap, LobbyRequest.SetHexMapName(lobbyId, hexMapName).toJson.toString)

  def setPickType(lobbyId: String, pickType: PickType)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetPickType, LobbyRequest.SetPickType(lobbyId, pickType).toJson.toString)

  def setNumberOfBans(lobbyId: String, numberOfBans: Int)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetNumberOfBans, LobbyRequest.SetNumberOfBans(lobbyId, numberOfBans).toJson.toString)

  def setNumberOfCharacters(lobbyId: String, numberOfCharacters: Int)(implicit
      wsClient: WSProbe
  ): WebsocketLobbyResponse =
    sendWSRequestL(
      LobbyRoute.SetNumberOfCharactersPerPlayer,
      LobbyRequest.SetNumberOfCharactersPerPlayer(lobbyId, numberOfCharacters).toJson.toString,
    )

  def setLobbyName(lobbyId: String, newName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetLobbyName, LobbyRequest.SetLobbyName(lobbyId, newName).toJson.toString)

  def setClockConfig(lobbyId: String, newConfig: ClockConfig)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetClockConfig, LobbyRequest.SetClockConfig(lobbyId, newConfig).toJson.toString)

  def setColor(lobbyId: String, newColorName: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.SetColor, LobbyRequest.SetColor(lobbyId, newColorName).toJson.toString)

  def startGame(lobbyId: String)(implicit wsClient: WSProbe): WebsocketLobbyResponse =
    sendWSRequestL(LobbyRoute.StartGame, LobbyRequest.StartGame(lobbyId).toJson.toString)

  def fetchAndParseLobby(lobbyId: String)(implicit wsClient: WSProbe): LobbyState = {
    val lobbyResponse = fetchLobby(lobbyId)
    lobbyResponse.statusCode shouldBe StatusCodes.OK.intValue
    lobbyResponse.body.parseJson.convertTo[LobbyState]
  }

  def pause(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Pause, Pause(lobbyId).toJson.toString)

  def surrender(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Surrender, Surrender(lobbyId).toJson.toString)

  def ban(lobbyId: String, characterIds: Set[CharacterMetadataId])(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BanCharacters, BanCharacters(lobbyId, characterIds).toJson.toString)

  def pick(lobbyId: String, characterId: CharacterMetadataId)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.PickCharacter, PickCharacter(lobbyId, characterId).toJson.toString)

  def blindPick(lobbyId: String, characterIds: Set[CharacterMetadataId])(implicit
      wsClient: WSProbe
  ): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BlindPickCharacters, BlindPickCharacters(lobbyId, characterIds).toJson.toString)

  def placeCharacters(lobbyId: String, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId])(implicit
      wsClient: WSProbe
  ): WebsocketGameResponse =
    sendWSRequestG(GameRoute.PlaceCharacters, PlaceCharacters(lobbyId, coordinatesToCharacterIdMap).toJson.toString)

  def moveCharacter(lobbyId: String, path: Seq[HexCoordinates], characterId: CharacterId)(implicit
      wsClient: WSProbe
  ): WebsocketGameResponse =
    sendWSRequestG(GameRoute.Move, Move(lobbyId, path, characterId).toJson.toString)

  def basicAttackCharacter(lobbyId: String, attackingCharacterId: CharacterId, targetCharacterId: CharacterId)(implicit
      wsClient: WSProbe
  ): WebsocketGameResponse =
    sendWSRequestG(GameRoute.BasicAttack, BasicAttack(lobbyId, attackingCharacterId, targetCharacterId).toJson.toString)

  def useAbility(
      lobbyId: String,
      abilityId: AbilityId,
      useData: UseData = UseData(),
  )(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(
      GameRoute.UseAbility,
      UseAbility(lobbyId, abilityId, useData).toJson.toString,
    )

  def endTurn(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.EndTurn, EndTurn(lobbyId).toJson.toString)

  def passTurn(lobbyId: String, characterId: CharacterId)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.PassTurn, PassTurn(lobbyId, characterId).toJson.toString)

  def fetchGame(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.GetState, GetState(lobbyId).toJson.toString)

  def fetchClock(lobbyId: String)(implicit wsClient: WSProbe): WebsocketGameResponse =
    sendWSRequestG(GameRoute.GetCurrentClock, GetCurrentClock(lobbyId).toJson.toString)

  def fetchAndParseGame(lobbyId: String)(implicit wsClient: WSProbe): GameStateView = {
    val gameResponse = fetchGame(lobbyId)
    gameResponse.statusCode shouldBe StatusCodes.OK.intValue
    gameResponse.body.parseJson.convertTo[GameStateView]
  }

  def fetchAndParseClock(lobbyId: String)(implicit wsClient: WSProbe): Clock = {
    val r = fetchClock(lobbyId)
    r.statusCode shouldBe StatusCodes.OK.intValue
    r.body.parseJson.convertTo[Clock]
  }
}
