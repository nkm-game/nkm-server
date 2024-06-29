package com.tosware.nkm.models.game.game_state

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.extensions.*
import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.models.game.hex_effect.*
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.pick.blindpick.*
import com.tosware.nkm.models.game.pick.draftpick.*

import java.time.Instant
import java.time.temporal.ChronoUnit

object GameState extends Logging
    with GameStateActorEndpoint.ChampionSelect
    with GameStateActorEndpoint.Gameplay
    with GameStateActorEndpoint.Initialization
    with GameStateActorEndpoint.Time
    with GameStateAbilityUtils
    with GameStateActorEndpoint.General
    with GameStateActorTimeouts
    with GameStateCharacterUtils
    with GameStateEffectUtils
    with GameStateEventManagement
    with GameStateHexCellEffectUtils
    with GameStateInitialization
    with GameStateInternalTriggers
    with GameStateTimeManagement
    with GameStateUpdateUtils {
  def empty(id: String): GameState = {
    val defaultPickType = PickType.AllRandom
    val defaultGameMode = GameMode.Deathmatch
    val defaultClockConfig = ClockConfig.defaultForPickType(defaultPickType)

    GameState(
      id = id,
      charactersMetadata = Set(),
      numberOfBans = 0,
      numberOfCharactersPerPlayers = 1,
      draftPickStateOpt = None,
      blindPickStateOpt = None,
      hexMap = HexMap.empty,
      hexPointGroupOwnerships = Map(),
      players = Seq(),
      characters = Set(),
      phase = Phase(1),
      turn = Turn(0),
      gameStatus = GameStatus.NotStarted,
      pickType = defaultPickType,
      gameMode = defaultGameMode,
      characterIdsOutsideMap = Set(),
      characterIdsThatTookActionThisPhase = Set(),
      characterTakingActionThisTurnOpt = None,
      playerIdsThatPlacedCharacters = Set(),
      abilityStates = Map(),
      characterEffectStates = Map(),
      hexCellEffectStates = Map(),
      clockConfig = defaultClockConfig,
      clock = Clock.fromConfig(defaultClockConfig, Seq()),
      lastTimestamp = Instant.now(),
      gameLog = GameLog(Seq.empty),
      hiddenEvents = Seq(),
    )
  }
}

case class GameState(
    id: GameId,
    charactersMetadata: Set[CharacterMetadata],
    gameStatus: GameStatus,
    pickType: PickType,
    gameMode: GameMode,
    numberOfBans: Int,
    numberOfCharactersPerPlayers: Int,
    draftPickStateOpt: Option[DraftPickState],
    blindPickStateOpt: Option[BlindPickState],
    hexMap: HexMap,
    hexPointGroupOwnerships: Map[HexPointGroupId, Option[PlayerId]],
    players: Seq[Player],
    characters: Set[NkmCharacter],
    phase: Phase,
    turn: Turn,
    characterIdsOutsideMap: Set[CharacterId],
    characterIdsThatTookActionThisPhase: Set[CharacterId],
    characterTakingActionThisTurnOpt: Option[CharacterId],
    playerIdsThatPlacedCharacters: Set[PlayerId],
    abilityStates: Map[AbilityId, AbilityState],
    characterEffectStates: Map[CharacterEffectId, CharacterEffectState],
    hexCellEffectStates: Map[HexCellEffectId, HexCellEffectState],
    clockConfig: ClockConfig,
    clock: Clock,
    lastTimestamp: Instant,
    gameLog: GameLog,
    hiddenEvents: Seq[EventHideData],
) extends Logging {
  def hostIdOpt: Option[PlayerId] = players.find(_.isHost).map(_.id)

  def currentPlayerNumber: Int = turn.number % players.size

  def isBlindPickingPhase: Boolean = blindPickStateOpt.fold(false)(_.pickPhase == BlindPickPhase.Picking)

  def isDraftBanningPhase: Boolean = draftPickStateOpt.fold(false)(_.pickPhase == DraftPickPhase.Banning)

  def isInCharacterSelect: Boolean = Seq(GameStatus.CharacterPick, GameStatus.CharacterPicked).contains(gameStatus)

  def isSharedTime: Boolean = Seq(GameStatus.CharacterPicked, GameStatus.CharacterPlacing).contains(gameStatus) ||
    isBlindPickingPhase || isDraftBanningPhase

  def playerNumber(playerId: PlayerId): Int = players.indexWhere(_.id == playerId)

  def playerByIdOpt(playerId: PlayerId): Option[Player] = players.find(_.id == playerId)

  def currentPlayer: Player = players(currentPlayerNumber)

  def currentCharacterOpt: Option[NkmCharacter] = characterTakingActionThisTurnOpt.map(characterById)

  def currentPlayerTime: Long = clock.playerTimes(currentPlayer.id)

  def millisSinceLastClockUpdate(): Long = ChronoUnit.MILLIS.between(lastTimestamp, Instant.now())

  def currentClock(): Clock = {
    if (!clock.isRunning) return clock
    val timeToDecrease: Long = millisSinceLastClockUpdate()

    if (isSharedTime)
      clock
        .setIsSharedTime(true)
        .decreaseSharedTime(timeToDecrease)
    else
      clock
        .setIsSharedTime(false)
        .decreaseTime(currentPlayer.id, timeToDecrease)
  }

  def charactersToTakeAction: Set[CharacterId] =
    characters.filterNot(_.isDead).map(_.id) -- characterIdsThatTookActionThisPhase

  def abilities: Set[Ability] = characters.flatMap(_.state.abilities)

  def effects: Set[CharacterEffect] = characters.flatMap(_.state.effects)

  def hexCellEffects: Set[HexCellEffect] = hexMap.cells.flatMap(_.effects)

  def triggerAbilities: Set[Ability & GameEventListener] = abilities.collect { case a: GameEventListener => a }

  def triggerEffects: Set[CharacterEffect & GameEventListener] = effects.collect { case e: GameEventListener => e }

  // safe
  def characterByIdOpt(characterId: CharacterId): Option[NkmCharacter] = characters.find(_.id == characterId)

  // unsafe before validation
  def characterById(characterId: CharacterId): NkmCharacter = characterByIdOpt(characterId).get

  // safe
  def abilityByIdOpt(abilityId: AbilityId): Option[Ability] = abilities.find(_.id == abilityId)

  // unsafe before validation
  def abilityById(abilityId: AbilityId): Ability = abilityByIdOpt(abilityId).get

  // safe
  def effectByIdOpt(effectId: CharacterEffectId): Option[CharacterEffect] = effects.find(_.id == effectId)

  // unsafe before validation
  def effectById(effectId: CharacterEffectId): CharacterEffect = effectByIdOpt(effectId).get

  // safe
  def hexCellEffectByIdOpt(effectId: HexCellEffectId): Option[HexCellEffect] = hexCellEffects.find(_.id == effectId)

  // unsafe before validation
  def hexCellEffectById(effectId: HexCellEffectId): HexCellEffect = hexCellEffectByIdOpt(effectId).get

  def hiddenEventsFor(forPlayerOpt: Option[PlayerId]): Seq[EventHideData] =
    forPlayerOpt.fold(hiddenEvents)(forPlayer => hiddenEvents.filterNot(_.showOnlyFor.contains(forPlayer)))

  def hiddenEidsFor(forPlayerOpt: Option[PlayerId]): Seq[GameEventId] =
    hiddenEventsFor(forPlayerOpt).map(_.eid)

  def newGameEventsSince(oldGs: GameState): Seq[GameEvent] =
    gameLog.events.drop(oldGs.gameLog.events.size)

  def characterPickFinished: Boolean = {
    if (pickType == PickType.AllRandom) return true
    val draftPickFinished = draftPickStateOpt.fold(false)(_.pickPhase == DraftPickPhase.Finished)
    val blindPickFinished = blindPickStateOpt.fold(false)(_.pickPhase == BlindPickPhase.Finished)
    draftPickFinished || blindPickFinished
  }

  def placingCharactersFinished: Boolean = playerIdsThatPlacedCharacters.size == players.size

  def backtrackCauseToPlayerId(causedById: String)(implicit gameState: GameState): Option[PlayerId] =
    backtrackCauseToCharacterId(causedById)
      .map(characterById)
      .map(_.owner.id)

  def backtrackCauseToCharacterId(causedById: String): Option[CharacterId] =
    if (characters.map(_.id).contains(causedById))
      Some(causedById)
    else if (effects.map(_.id).contains(causedById))
      Some(effectById(causedById).parentCharacter(this).id)
    else if (abilities.map(_.id).contains(causedById))
      Some(abilityById(causedById).parentCharacter(this).id)
    else None

  def invisibleCharacterCoords(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): Set[HexCoordinates] =
    characters
      .filterNot(c => forPlayerOpt.contains(c.owner.id))
      .filter(_.isInvisible)
      .flatMap(_.parentCellOpt.map(_.coordinates))

  def toView(forPlayerOpt: Option[PlayerId]): GameStateView =
    game_state.GameStateView(
      id = id,
      charactersMetadata = charactersMetadata,
      gameStatus = gameStatus,
      pickType = pickType,
      numberOfBans = numberOfBans,
      numberOfCharactersPerPlayers = numberOfCharactersPerPlayers,
      draftPickState = draftPickStateOpt.map(_.toView(forPlayerOpt)),
      blindPickState = blindPickStateOpt.map(_.toView(forPlayerOpt)),
      hexMap = hexMap.toView(forPlayerOpt)(this),
      hexPointGroupOwnerships = hexPointGroupOwnerships,
      players = players,
      characters = characters.map(_.toView(forPlayerOpt)(this)),
      phase = phase,
      turn = turn,
      characterIdsOutsideMap = characterIdsOutsideMap,
      characterIdsThatTookActionThisPhase = characterIdsThatTookActionThisPhase,
      characterTakingActionThisTurn = characterTakingActionThisTurnOpt,
      playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters,
      abilities = abilities.flatMap(_.toView(forPlayerOpt)(this)),
      effects = effects.flatMap(_.toView(forPlayerOpt)(this)),
      clockConfig = clockConfig,
      clock = currentClock(),
      gameLog = gameLog.toView(forPlayerOpt)(this),
      currentPlayerId = currentPlayer.id,
      hostId = hostIdOpt.getOrElse(""),
      isBlindPickingPhase = isBlindPickingPhase,
      isDraftBanningPhase = isDraftBanningPhase,
      isInCharacterSelect = isInCharacterSelect,
      currentPlayerTime = currentPlayerTime,
      charactersToTakeAction = charactersToTakeAction,
    )
}
