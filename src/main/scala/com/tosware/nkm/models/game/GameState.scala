package com.tosware.nkm.models.game

import com.softwaremill.quicklens._
import com.tosware.nkm._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character._
import com.tosware.nkm.models.game.character_effect.{CharacterEffect, CharacterEffectState}
import com.tosware.nkm.models.game.effects._
import com.tosware.nkm.models.game.event.GameEvent._
import com.tosware.nkm.models.game.event._
import com.tosware.nkm.models.game.hex._
import com.tosware.nkm.models.game.hex_effect.{HexCellEffect, HexCellEffectState}
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.pick.blindpick._
import com.tosware.nkm.models.game.pick.draftpick._

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.util.Random

object GameState extends Logging {
  def empty(id: String): GameState = {
    val defaultPickType = PickType.AllRandom
    val defaultClockConfig = ClockConfig.defaultForPickType(defaultPickType)

    GameState(
      id = id,
      charactersMetadata = Set(),
      numberOfBans = 0,
      numberOfCharactersPerPlayers = 1,
      draftPickState = None,
      blindPickState = None,
      hexMap = HexMap.empty,
      players = Seq(),
      characters = Set(),
      phase = Phase(0),
      turn = Turn(0),
      gameStatus = GameStatus.NotStarted,
      pickType = defaultPickType,
      characterIdsOutsideMap = Set(),
      characterIdsThatTookActionThisPhase = Set(),
      characterTakingActionThisTurn = None,
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


case class GameState
(
  id: GameId,
  charactersMetadata: Set[CharacterMetadata],
  gameStatus: GameStatus,
  pickType: PickType,
  numberOfBans: Int,
  numberOfCharactersPerPlayers: Int,
  draftPickState: Option[DraftPickState],
  blindPickState: Option[BlindPickState],
  hexMap: HexMap,
  players: Seq[Player],
  characters: Set[NkmCharacter],
  phase: Phase,
  turn: Turn,
  characterIdsOutsideMap: Set[CharacterId],
  characterIdsThatTookActionThisPhase: Set[CharacterId],
  characterTakingActionThisTurn: Option[CharacterId],
  playerIdsThatPlacedCharacters: Set[PlayerId],
  abilityStates: Map[AbilityId, AbilityState],
  characterEffectStates: Map[CharacterEffectId, CharacterEffectState],
  hexCellEffectStates: Map[HexCellEffectId, HexCellEffectState],
  clockConfig: ClockConfig,
  clock: Clock,
  lastTimestamp: Instant,
  gameLog: GameLog,
  hiddenEvents: Seq[EventHideData],
)
{
  import GameState._
  private implicit val p: Phase = phase
  private implicit val t: Turn = turn

  def hostId: PlayerId = players.find(_.isHost).get.id

  def currentPlayerNumber: Int = turn.number % players.size

  def isBlindPickingPhase: Boolean = blindPickState.fold(false)(_.pickPhase == BlindPickPhase.Picking)

  def isDraftBanningPhase: Boolean = draftPickState.fold(false)(_.pickPhase == DraftPickPhase.Banning)

  def isInCharacterSelect: Boolean = Seq(GameStatus.CharacterPick, GameStatus.CharacterPicked).contains(gameStatus)

  def isSharedTime: Boolean = Seq(GameStatus.CharacterPicked, GameStatus.CharacterPlacing).contains(gameStatus) ||
    isBlindPickingPhase || isDraftBanningPhase

  def playerNumber(playerId: PlayerId): Int = players.indexWhere(_.id == playerId)

  def playerByIdOpt(playerId: PlayerId): Option[Player] = players.find(_.id == playerId)

  def playerById(playerId: PlayerId): Player =
    playerByIdOpt(playerId).get

  def currentPlayer: Player = players(currentPlayerNumber)

  def currentCharacterOpt: Option[NkmCharacter] = characterTakingActionThisTurn.map(characterById)

  def currentPlayerTime: Long = clock.playerTimes(currentPlayer.id)

  def millisSinceLastClockUpdate(): Long = ChronoUnit.MILLIS.between(lastTimestamp, Instant.now())

  def getCurrentClock(): Clock = {
    if(!clock.isRunning) return clock;
    val timeToDecrease: Long = millisSinceLastClockUpdate()

    if(isSharedTime)
      clock.decreaseSharedTime(timeToDecrease)
    else
      clock.decreaseTime(currentPlayer.id, timeToDecrease)
  }


  def charactersToTakeAction: Set[CharacterId] = characters.filterNot(_.isDead).map(_.id) -- characterIdsThatTookActionThisPhase

  def abilities: Set[Ability] = characters.flatMap(_.state.abilities)

  def effects: Set[CharacterEffect] = characters.flatMap(_.state.effects)

  def hexCellEffects: Set[HexCellEffect] = hexMap.cells.flatMap(_.effects)

  def triggerAbilities: Set[Ability with GameEventListener] = abilities.collect {case a: GameEventListener => a}

  def triggerEffects: Set[CharacterEffect with GameEventListener] = effects.collect {case e: GameEventListener => e}

  def characterById(characterId: CharacterId): NkmCharacter = characters.find(_.id == characterId).get

  def abilityById(abilityId: AbilityId): Ability = abilities.find(_.id == abilityId).get

  def effectById(effectId: CharacterEffectId): CharacterEffect = effects.find(_.id == effectId).get

  def hexCellEffectById(effectId: HexCellEffectId): HexCellEffect = hexCellEffects.find(_.id == effectId).get

  def hiddenEventsFor(forPlayerOpt: Option[PlayerId]): Seq[EventHideData] =
    if(forPlayerOpt.isEmpty)
      hiddenEvents
    else
      hiddenEvents.filterNot(_.showOnlyFor.contains(forPlayerOpt.get))

  def hiddenEidsFor(forPlayerOpt: Option[PlayerId]): Seq[GameEventId] =
    hiddenEventsFor(forPlayerOpt).map(_.eid)

  def characterPickFinished: Boolean = {
    if(pickType == PickType.AllRandom) return true
    val draftPickFinished = draftPickState.fold(false)(_.pickPhase == DraftPickPhase.Finished)
    val blindPickFinished = blindPickState.fold(false)(_.pickPhase == BlindPickPhase.Finished)
    draftPickFinished || blindPickFinished
  }

  def placingCharactersFinished: Boolean = playerIdsThatPlacedCharacters.size == players.size

  def timeoutNumber: Int = gameStatus match {
    case GameStatus.NotStarted => 0
    case GameStatus.CharacterPick | GameStatus.CharacterPicked =>
      pickType match {
        case PickType.AllRandom => 0
        case PickType.DraftPick => draftPickState.fold(0)(_.pickNumber)
        case PickType.BlindPick => blindPickState.fold(0)(_.pickNumber)
      }
    case GameStatus.CharacterPlacing | GameStatus.Running | GameStatus.Finished =>
      turn.number
  }

  def backtrackCauseToCharacterId(causedById: String): Option[CharacterId] =
    if(characters.map(_.id).contains(causedById))
      Some(causedById)
    else if(effects.map(_.id).contains(causedById))
      Some(effectById(causedById).parentCharacter(this).id)
    else if(abilities.map(_.id).contains(causedById))
      Some(abilityById(causedById).parentCharacter(this).id)
    else None

  def getDirection(from: CharacterId, to: CharacterId)(implicit gameState: GameState): Option[HexDirection] =
    for {
      fromCoordinates: HexCoordinates <- characterById(from).parentCell.map(_.coordinates)
      toCoordinates: HexCoordinates <- characterById(to).parentCell.map(_.coordinates)
      direction: HexDirection <- fromCoordinates.getDirection(toCoordinates)
    } yield direction

  def invisibleCharacterCoords(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): Set[HexCoordinates] =
    characters
      .filterNot(c => forPlayerOpt.contains(c.owner.id))
      .filter(_.state.effects.ofType[Invisibility].nonEmpty)
      .flatMap(_.parentCell.map(_.coordinates))

  private def handleTrigger(event: GameEvent, trigger: GameEventListener)(implicit random: Random, gameState: GameState): GameState = {
    try {
      trigger.onEvent(event)(random, gameState)
    } catch {
      case e: Throwable =>
        logger.error(e.getMessage)
        gameState
    }
  }

  def updateTimestamp(): GameState = copy(lastTimestamp = Instant.now())

  private def executeEventTriggers(e: GameEvent)(implicit random: Random): GameState = {
    val stateAfterAbilityTriggers = triggerAbilities.foldLeft(this)((acc, ability) => {
      handleTrigger(e, ability)(random, acc)
    })
    triggerEffects.foldLeft(stateAfterAbilityTriggers)((acc, effect) => {
      handleTrigger(e, effect)(random, acc)
    })
  }

  private def logEvent(e: GameEvent)(implicit random: Random): GameState =
    copy(gameLog = gameLog.modify(_.events).using(es => es :+ e))
      .executeEventTriggers(e)

  private def logEvents(es: Seq[GameEvent])(implicit random: Random): GameState =
    es.foldLeft(this){case (acc, event) => acc.logEvent(event)}

  def logAndHideEvent(e: GameEvent, showOnlyFor: Seq[PlayerId], revealCondition: RevealCondition)(implicit random: Random): GameState =
    copy(hiddenEvents = hiddenEvents :+ event.EventHideData(e.id, showOnlyFor, revealCondition))
      .logEvent(e)

  def reveal(revealCondition: RevealCondition)(implicit random: Random): GameState =
    copy(hiddenEvents = hiddenEvents.filterNot(_.revealCondition == revealCondition))
      .logEvent(EventsRevealed(randomUUID(), phase, turn, id, hiddenEvents.filter(_.revealCondition == revealCondition).map(_.eid)))

  private def updateClock(newClock: Clock)(implicit random: Random, causedById: String): GameState =
    updateTimestamp()
      .copy(clock = newClock)
      .logEvent(ClockUpdated(randomUUID(), phase, turn, causedById, newClock))

  private def updateGameStatus(newGameStatus: GameStatus)(implicit random: Random, causedById: String): GameState =
    copy(gameStatus = newGameStatus)
      .logEvent(GameStatusUpdated(randomUUID(), phase, turn, causedById, newGameStatus))


  def initializeCharacterPick()(implicit random: Random): GameState = {
    val characterPickInitialPickTime: Long = pickType match {
      case PickType.AllRandom => clockConfig.timeAfterPickMillis
      case PickType.DraftPick => clockConfig.maxBanTimeMillis
      case PickType.BlindPick => clockConfig.maxPickTimeMillis
    }
    updateClock(clock.setSharedTime(characterPickInitialPickTime))(random, id)
  }

  def initializeCharacterPlacing()(implicit random: Random): GameState =
    updateClock(clock.setSharedTime(clockConfig.timeForCharacterPlacing))(random, id)

  def startGame(g: GameStartDependencies)(implicit random: Random): GameState =
    copy(
      hexMap = g.hexMap,
      charactersMetadata = g.charactersMetadata,
      players = g.players,
      pickType = g.pickType,
      numberOfBans = g.numberOfBansPerPlayer,
      numberOfCharactersPerPlayers = g.numberOfCharactersPerPlayer,
      gameStatus = if (g.pickType == PickType.AllRandom) GameStatus.CharacterPicked else GameStatus.CharacterPick,
      draftPickState = if (g.pickType == PickType.DraftPick) Some(DraftPickState.empty(DraftPickConfig.generate(g))) else None,
      blindPickState = if (g.pickType == PickType.BlindPick) Some(BlindPickState.empty(BlindPickConfig.generate(g))) else None,
      clockConfig = g.clockConfig,
      clock = Clock.fromConfig(g.clockConfig, playerOrder = g.players.map(_.name)),
    ).initializeCharacterPick()

  def placeCharactersRandomly(forPlayers: Set[PlayerId])(implicit random: Random, causedById: String): GameState = {
    players.filter(pl => forPlayers.contains(pl.id)).foldLeft(this){
      case (acc, p) =>
        val spawnCoords = hexMap.getSpawnPointsFor(p.id)(this).map(_.coordinates)
        val characterIdsShuffled = random.shuffle(p.characterIds.toSeq)
        val coordinatesToCharacterIdMap = spawnCoords.zip(characterIdsShuffled).toMap
        acc.placeCharacters(p.id, coordinatesToCharacterIdMap)
    }
  }

  def pickAndPlaceCharactersRandomlyIfAllRandom()(implicit random: Random, causedById: String): GameState =
    if (pickType == PickType.AllRandom)
      assignCharactersToPlayers()
        .placeCharactersRandomly(players.map(_.id).toSet)
    else this

  def finishGame()(implicit random: Random, causedById: String): GameState =
    updateGameStatus(GameStatus.Finished)
      .updateClock(clock.pause())(random, id)

  def checkVictoryStatus()(implicit random: Random, causedById: String): GameState = {
    def filterPendingPlayers: Player => Boolean = _.victoryStatus == VictoryStatus.Pending

    if (gameStatus == GameStatus.CharacterPick && players.count(_.victoryStatus == VictoryStatus.Lost) > 0) {
      this.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
        .setTo(VictoryStatus.Drawn)
        .finishGame()
    } else if (players.count(_.victoryStatus == VictoryStatus.Pending) == 1) {
      this.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
        .setTo(VictoryStatus.Won)
        .finishGame()
    } else this
  }

  def generateCharacter(characterMetadataId: CharacterMetadataId)(implicit random: Random): NkmCharacter = {
    val characterId = randomUUID()
    val metadata = charactersMetadata.find(_.id == characterMetadataId).get
    NkmCharacter.fromMetadata(characterId, metadata)
  }

  def assignCharactersToPlayers()(implicit random: Random): GameState = {
    val characterSelection: Map[PlayerId, Iterable[CharacterMetadataId]] = pickType match {
      case PickType.AllRandom =>
        val pickedCharacters = random
          .shuffle(charactersMetadata.map(_.id).toSeq)
          .grouped(numberOfCharactersPerPlayers)
          .take(players.length)
        players.map(_.id).zip(pickedCharacters).toMap
      case PickType.DraftPick =>
        draftPickState.get.characterSelection
      case PickType.BlindPick =>
        blindPickState.get.characterSelection
    }

    val playersWithCharacters =
      players.map(p => {
        val generatedCharacters = characterSelection(p.id).map(c => generateCharacter(c)).toSet
        (p, generatedCharacters)
      })
    val playersWithAssignedCharacters = playersWithCharacters.map{case (p, cs) => p.copy(characterIds = cs.map(_.id))}
    val characters = playersWithCharacters.flatMap(_._2).toSet
    val abilitiesByCharacter = characters.map(c => (c.id, c.state.abilities))
    val abilityStatesMap: Map[AbilityId, AbilityState] = abilitiesByCharacter.collect
    {
      case (_: CharacterId, as: Seq[Ability]) => as.map(a => a.id -> AbilityState())
    }.flatten.toMap

    copy(
      players = playersWithAssignedCharacters,
      characters = characters,
      characterIdsOutsideMap = characters.map(c => c.id),
      abilityStates = abilityStates.concat(abilityStatesMap)
    )
  }

  def checkIfCharacterPickFinished()(implicit random: Random, causedById: String): GameState = {
    if(characterPickFinished) {
      updateGameStatus(GameStatus.CharacterPicked)
        .updateClock(clock.setSharedTime(clockConfig.timeAfterPickMillis))(random, id)
        .assignCharactersToPlayers()
        .reveal(RevealCondition.BlindPickFinished)
        .logEvent(CharactersPicked(randomUUID(), phase, turn, id))
    } else this
  }

  def startPlacingCharacters()(implicit random: Random, causedById: String): GameState =
    updateGameStatus(GameStatus.CharacterPlacing)
      .initializeCharacterPlacing()
      .pickAndPlaceCharactersRandomlyIfAllRandom()

  def decreaseSharedTime(timeMillis: Long)(implicit random: Random): GameState =
    updateClock(clock.decreaseSharedTime(timeMillis))(random, id)

  def decreaseTime(playerId: PlayerId, timeMillis: Long)(implicit random: Random): GameState =
    updateClock(clock.decreaseTime(playerId, timeMillis))(random, playerId)

  def increaseTime(playerId: PlayerId, timeMillis: Long)(implicit random: Random): GameState =
    updateClock(clock.increaseTime(playerId, timeMillis))(random, playerId)

  def pause()(implicit random: Random): GameState = {
    val timeToDecrease: Long = millisSinceLastClockUpdate()
    val ngs = if(isSharedTime) {
      decreaseSharedTime(timeToDecrease)
    } else {
      decreaseTime(currentPlayer.id, timeToDecrease)
    }

    ngs.updateClock(ngs.clock.pause())(random, id)
  }

  def unpause()(implicit random: Random): GameState =
    updateClock(clock.unpause())(random, id)

  def surrender(playerIds: PlayerId*)(implicit random: Random, causedById: String): GameState = {
    def filterPlayers: Player => Boolean = p => playerIds.contains(p.name)

    this
      .modify(_.players.eachWhere(filterPlayers).victoryStatus).setTo(VictoryStatus.Lost)
      .logEvents(
        playerIds.map(pid => PlayerSurrendered(randomUUID(), phase, turn, pid, pid)) ++
        playerIds.map(pid => PlayerLost(randomUUID(), phase, turn, id, pid))
      )
      .checkVictoryStatus()
      .skipTurnIfPlayerKnockedOut()(random, playerIds.mkString(", "))
  }

  def ban(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(implicit random: Random): GameState =
    copy(draftPickState = draftPickState.map(_.ban(playerId, characterIds)))
      .logAndHideEvent(PlayerBanned(randomUUID(), phase, turn, playerId, playerId, characterIds), Seq(playerId), RevealCondition.BanningPhaseFinished)
      .logEvent(PlayerFinishedBanning(randomUUID(), phase, turn, playerId, playerId))

  def finishBanningPhase()(implicit random: Random): GameState =
    copy(draftPickState = draftPickState.map(_.finishBanning()))
      .updateClock(clock.setSharedTime(clockConfig.maxPickTimeMillis))(random, id)
      .reveal(RevealCondition.BanningPhaseFinished)
      .logEvent(BanningPhaseFinished(randomUUID(), phase, turn, id))

  def pick(playerId: PlayerId, characterId: CharacterMetadataId)(implicit random: Random, causedById: String): GameState =
    copy(draftPickState = draftPickState.map(_.pick(playerId, characterId)))
      .updateClock(clock.setSharedTime(clockConfig.maxPickTimeMillis))(random, id)
      .logEvent(PlayerPicked(randomUUID(), phase, turn, playerId, playerId, characterId))
      .checkIfCharacterPickFinished()

  def draftPickTimeout()(implicit random: Random, causedById: String): GameState =
    surrender(draftPickState.get.currentPlayerPicking.get)

  def blindPick(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(implicit random: Random, causedById: String): GameState =
    copy(blindPickState = blindPickState.map(_.pick(playerId, characterIds)))
      .logAndHideEvent(PlayerBlindPicked(randomUUID(), phase, turn, playerId, playerId, characterIds), Seq(playerId), RevealCondition.BlindPickFinished)
      .logEvent(PlayerFinishedBlindPicking(randomUUID(), phase, turn, playerId, playerId))
      .checkIfCharacterPickFinished()

  def blindPickTimeout()(implicit random: Random, causedById: String): GameState =
    surrender(blindPickState.get.pickingPlayers: _*)

  def placingCharactersTimeout()(implicit random: Random, causedById: String): GameState = {
    val pidsThatDidNotPlace: Set[PlayerId] = players.map(_.id).toSet -- playerIdsThatPlacedCharacters
    placeCharactersRandomly(pidsThatDidNotPlace)
  }

  def checkIfPlacingCharactersFinished()(implicit random: Random, causedById: String): GameState =
    if(placingCharactersFinished)
      logEvent(PlacingCharactersFinished(randomUUID(), phase, turn, id))
        .reveal(RevealCondition.CharacterPlacingFinished)
        .updateGameStatus(GameStatus.Running)
    else this

  def placeCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId])(implicit random: Random, causedById: String): GameState =
    coordinatesToCharacterIdMap.foldLeft(this){case (acc, (coordinate, characterId)) => acc.placeCharacter(coordinate, characterId)(random, playerId)}
      .copy(playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters + playerId)
      .checkIfPlacingCharactersFinished()

  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId)(implicit random: Random, causedById: String): GameState = {
    val ngs = updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
      .modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))
    val cpEvent = CharacterPlaced(randomUUID(), phase, turn, causedById, characterId, targetCellCoordinates)
    if(gameStatus == GameStatus.CharacterPlacing) {
      ngs.logAndHideEvent(cpEvent, Seq(characterById(characterId).owner(ngs).id), RevealCondition.CharacterPlacingFinished)
    } else {
      ngs.logEvent(cpEvent)
    }
  }

  def basicMoveCharacter(characterId: CharacterId, path: Seq[HexCoordinates])(implicit random: Random): GameState = {
    implicit val causedById: CharacterId = characterId
    val newGameState = takeActionWithCharacter(characterId)
    characterById(characterId).basicMove(path)(random, newGameState)
      .logEvent(CharacterBasicMoved(randomUUID(), phase, turn, causedById, characterId, path))
  }

  def teleportCharacter(characterId: CharacterId, targetCellCoordinates: HexCoordinates)(implicit random: Random, causedById: String): GameState = {
    val parentCellOpt = characterById(characterId).parentCell(this)

    val removedFromParentCellState = parentCellOpt.fold(this)(c => updateHexCell(c.coordinates)(_.copy(characterId = None)))
    val targetIsFreeToStand = hexMap.getCell(targetCellCoordinates).get.isFreeToStand
    val characterIsOnMap = characterById(characterId).isOnMap(this)

    val ngs = if (targetIsFreeToStand) {
      if (characterIsOnMap)
        removedFromParentCellState.updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
      else removedFromParentCellState.placeCharacter(targetCellCoordinates, characterId)
    } else {
      // probably just passing by a friendly characterOpt
      removedFromParentCellState.removeCharacterFromMap(characterId)
    }
    ngs.logEvent(CharacterTeleported(randomUUID(), phase, turn, causedById, characterId, targetCellCoordinates))
  }


  def basicAttack(attackingCharacterId: CharacterId, targetCharacterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: CharacterId = attackingCharacterId
    val newGameState = takeActionWithCharacter(attackingCharacterId)
      .logEvent(CharacterPreparedToAttack(randomUUID(), phase, turn, causedById, attackingCharacterId, targetCharacterId))

    val attackingCharacter = newGameState.characterById(attackingCharacterId)
    val targetCharacter = newGameState.characterById(targetCharacterId)
    val blockEffects = targetCharacter.state.effects.ofType[Block]
    if(blockEffects.nonEmpty) {
      newGameState.removeEffect(blockEffects.head.id)
    } else {
      attackingCharacter.basicAttack(targetCharacterId)(random, newGameState)
        .logEvent(CharacterBasicAttacked(randomUUID(), phase, turn, causedById, attackingCharacterId, targetCharacterId))
    }
  }

  def updatePlayer(playerId: PlayerId)(updateFunction: Player => Player): GameState =
    this.modify(_.players.each).using {
      case player if player.id == playerId => updateFunction(player)
      case player => player
    }

  def updateCharacter(characterId: CharacterId)(updateFunction: NkmCharacter => NkmCharacter): GameState =
    this.modify(_.characters.each).using {
      case character if character.id == characterId => updateFunction(character)
      case character => character
    }

  def updateHexCell(targetCoords: HexCoordinates)(updateFunction: HexCell => HexCell): GameState =
    this.modify(_.hexMap.cells.each).using {
      case cell if cell.coordinates == targetCoords => updateFunction(cell)
      case cell => cell
    }

  def updateAbility(abilityId: AbilityId, newAbility: Ability): GameState =
    this.modify(_.characters.each.state.abilities.each).using {
      case ability if ability.id == abilityId => newAbility
      case ability => ability
    }

  def executeCharacter(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    damageCharacter(characterId, Damage(DamageType.True, Int.MaxValue))

  def damageCharacter(characterId: CharacterId, damage: Damage)(implicit random: Random, causedById: String): GameState = {
    if(characterById(characterId).isDead) {
      logger.error(s"Unable to damage character $characterId. Character dead.")
      this
    } else {
      updateCharacter(characterId)(_.receiveDamage(damage))
        .checkIfCharacterDied(characterId) // needs to be removed first in order to avoid infinite triggers
        .logEvent(CharacterDamaged(randomUUID(), phase, turn, causedById, characterId, damage))
    }
  }

  def heal(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
    if(characterById(characterId).isDead) {
      logger.error(s"Unable to heal character $characterId. Character dead.")
      this
    } else {
      val healPreparedId = randomUUID()
      val healPreparedGs = logEvent(HealPrepared(healPreparedId, phase, turn, causedById, characterId, amount))

      val additionalHealing = healPreparedGs.gameLog.events
        .inTurn(turn.number)
        .ofType[HealAmplified]
        .filter(_.healPreparedId == healPreparedId)
        .map(_.additionalAmount)
        .sum

      val resultHealing = amount + additionalHealing

      healPreparedGs
        .updateCharacter(characterId)(_.heal(resultHealing))
        .logEvent(CharacterHealed(randomUUID(), phase, turn, causedById, characterId, resultHealing))
    }

  def amplifyHeal(healPreparedId: GameEventId, additionalAmount: Int)(implicit random: Random, causedById: String): GameState =
    if(additionalAmount == 0)
      this
    else
      logEvent(HealAmplified(randomUUID(), phase, turn, causedById, healPreparedId, additionalAmount))

  def setHp(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.modify(_.state.healthPoints).setTo(amount))
      .logEvent(CharacterHpSet(randomUUID(), phase, turn, causedById, characterId, amount))

  def setShield(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.modify(_.state.shield).setTo(amount))
      .logEvent(CharacterShieldSet(randomUUID(), phase, turn, causedById, characterId, amount))

  def setStat(characterId: CharacterId, statType: StatType, amount: Int)(implicit random: Random, causedById: String): GameState = {
    val updateStat = statType match {
      case StatType.AttackPoints => modify(_: NkmCharacter)(_.state.pureAttackPoints)
      case StatType.BasicAttackRange => modify(_: NkmCharacter)(_.state.pureBasicAttackRange)
      case StatType.Speed => modify(_: NkmCharacter)(_.state.pureSpeed)
      case StatType.PhysicalDefense => modify(_: NkmCharacter)(_.state.purePhysicalDefense)
      case StatType.MagicalDefense => modify(_: NkmCharacter)(_.state.pureMagicalDefense)
    }
    updateCharacter(characterId)(c => updateStat(c).setTo(amount))
      .logEvent(CharacterStatSet(randomUUID(), phase, turn, causedById, characterId, statType, amount))
  }

  def knockbackCharacter(
    characterId: CharacterId,
    direction: HexDirection,
    knockback: Int,
  )(implicit random: Random, causedById: String): (GameState, KnockbackResult) = {
    val targetCellOpt = hexMap.getCellOfCharacter(characterId)
    targetCellOpt.fold((this, KnockbackResult.HitNothing)) { targetCell =>
      val lineCells: Seq[HexCell] = targetCell.getLine(direction, knockback)(this)
      if(lineCells.isEmpty)
        return (this, KnockbackResult.HitEndOfMap)

      val firstBlockedCellOpt = lineCells.find(!_.isFreeToStand)

      firstBlockedCellOpt.fold {
        {
          val teleportGs = teleportCharacter(characterId, lineCells.last.coordinates)(random, id)
          if (lineCells.size < knockback)
            return (teleportGs, KnockbackResult.HitEndOfMap)
          else
            return (teleportGs, KnockbackResult.HitNothing)
        }
      } { firstBlockedCell =>
        val cellToTeleportIndex = lineCells.indexOf(firstBlockedCellOpt.get) - 1
        val result = if(firstBlockedCell.isWall)
          KnockbackResult.HitWall
        else
          KnockbackResult.HitCharacter
        if(cellToTeleportIndex < 0) {
          return (this, result)
        } else {
          val cellToTeleport = lineCells(cellToTeleportIndex)
          val teleportGs = teleportCharacter(characterId, cellToTeleport.coordinates)(random, id)
          return (teleportGs, result)
        }
      }
    }
  }

  def checkIfCharacterDied(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    if(characterById(characterId).isDead) {
      handleCharacterDeath(characterId)
    } else this

  def knockOutPlayer(playerId: PlayerId)(implicit random: Random, causedById: String): GameState =
    updatePlayer(playerId)(_.modify(_.victoryStatus).setTo(VictoryStatus.Lost))
      .checkVictoryStatus()
      .skipTurnIfPlayerKnockedOut()(random, playerId)

  def checkIfPlayerKnockedOut(playerId: PlayerId)(implicit random: Random, causedById: String): GameState =
    if(characters.filter(_.owner(this).id == playerId).forall(_.isDead)) {
      knockOutPlayer(playerId)
    } else this

  def handleCharacterDeath(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    this.removeCharacterFromMap(characterId)
      .logEvent(CharacterDied(randomUUID(), phase, turn, causedById, characterId))
      .checkIfPlayerKnockedOut(characterById(characterId).owner(this).id)

  def addEffect(characterId: CharacterId, characterEffect: CharacterEffect)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.addEffect(characterEffect))
      .modify(_.characterEffectStates).using(ces => ces.updated(characterEffect.id, CharacterEffectState(characterEffect.initialCooldown)))
      .logEvent(EffectAddedToCharacter(randomUUID(), phase, turn, causedById, characterEffect.id, characterId))

  def removeEffects(characterEffectIds: Seq[CharacterEffectId])(implicit random: Random, causedById: String): GameState =
    characterEffectIds.foldLeft(this){case (acc, eid) => acc.removeEffect(eid)}

  def removeEffect(characterEffectId: CharacterEffectId)(implicit random: Random, causedById: String): GameState = {
    val character = effectById(characterEffectId).parentCharacter(this)
    updateCharacter(character.id)(_.removeEffect(characterEffectId))
      .modify(_.characterEffectStates).using(ces => ces.removed(characterEffectId))
      .logEvent(EffectRemovedFromCharacter(randomUUID(), phase, turn, causedById, characterEffectId))
  }

  def addHexCellEffect(coordinates: HexCoordinates, hexCellEffect: HexCellEffect)(implicit random: Random, causedById: String): GameState =
    updateHexCell(coordinates)(_.addEffect(hexCellEffect))
      .modify(_.hexCellEffectStates).using(hes => hes.updated(hexCellEffect.id, HexCellEffectState(hexCellEffect.initialCooldown)))
      .logEvent(EffectAddedToCell(randomUUID(), phase, turn, causedById, hexCellEffect.id, coordinates))

  def removeHexCellEffects(heids: Seq[HexCellEffectId])(implicit random: Random, causedById: String): GameState =
    heids.foldLeft(this){case (acc, eid) => acc.removeHexCellEffect(eid)}

  def removeHexCellEffect(heid: HexCellEffectId)(implicit random: Random, causedById: String): GameState = {
    val coordinates = hexCellEffectById(heid).parentCell(this).get.coordinates
    updateHexCell(coordinates)(_.removeEffect(heid))
      .modify(_.hexCellEffectStates).using(hes => hes.removed(heid))
      .logEvent(EffectRemovedFromCell(randomUUID(), phase, turn, causedById, heid))
  }

  def removeCharacterFromMap(characterId: CharacterId)(implicit random: Random, causedById: String): GameState = {
    val parentCellOpt = characterById(characterId).parentCell(this)

    parentCellOpt.fold(this)(c => updateHexCell(c.coordinates)(_.copy(characterId = None)))
      .modify(_.characterIdsOutsideMap).setTo(characterIdsOutsideMap + characterId)
      .logEvent(CharacterRemovedFromMap(randomUUID(), phase, turn, causedById, characterId))
  }

  def takeActionWithCharacter(characterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: String = characterId

    if(characterTakingActionThisTurn.isDefined) // do not log event more than once
      return this
    this.modify(_.characterTakingActionThisTurn)
      .setTo(Some(characterId))
      .logEvent(CharacterTookAction(randomUUID(), phase, turn, causedById, characterId))
  }

  def abilityHitCharacter(abilityId: AbilityId, targetCharacter: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    logEvent(AbilityHitCharacter(randomUUID(), phase, turn, causedById, abilityId: AbilityId, targetCharacter: CharacterId))
  }

  def refreshBasicMove(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
    logEvent(BasicMoveRefreshed(randomUUID(), phase, turn, causedById, targetCharacter))

  def refreshBasicAttack(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
    logEvent(BasicAttackRefreshed(randomUUID(), phase, turn, causedById, targetCharacter))

  def putAbilityOnCooldown(abilityId: AbilityId): GameState = {
    val newState = abilityById(abilityId).getCooldownState(this)
    this.copy(abilityStates = abilityStates.updated(abilityId, newState))
  }

  def afterAbilityUse(abilityId: AbilityId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId

    val ngs = if(abilityStates(abilityId).isEnabled) this
    else putAbilityOnCooldownOrDecrementFreeAbility(abilityId)

    ngs.logEvent(AbilityUseFinished(randomUUID(), phase, turn, causedById, abilityId))
  }

  def putAbilityOnCooldownOrDecrementFreeAbility(abilityId: AbilityId)(implicit random: Random): GameState = {
    val freeAbilityEffectOpt = abilityById(abilityId).parentCharacter(this).state.effects.ofType[FreeAbility].headOption
    if(freeAbilityEffectOpt.nonEmpty) {
      this.decrementEffectCooldown(freeAbilityEffectOpt.get.effectId)
    } else putAbilityOnCooldown(abilityId)
  }

  def decrementAbilityCooldown(abilityId: AbilityId, amount: Int = 1): GameState = {
    val newState = abilityById(abilityId).getDecrementCooldownState(amount)(this)
    this.copy(abilityStates = abilityStates.updated(abilityId, newState))
  }

  def setAbilityEnabled(abilityId: AbilityId, newEnabled: Boolean): GameState = {
    val newState = abilityById(abilityId).getEnabledChangedState(newEnabled)(this)
    this.copy(abilityStates = abilityStates.updated(abilityId, newState))
  }

  def setAbilityVariable(abilityId: AbilityId, key: String, value: String)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val newState = abilityById(abilityId).getVariablesChangedState(key, value)(this)
    this.copy(abilityStates = abilityStates.updated(abilityId, newState))
      .logEvent(AbilityVariableSet(randomUUID(), phase, turn, causedById, abilityId, key, value))
  }

  def setEffectVariable(effectId: CharacterEffectId, key: String, value: String)(implicit random: Random): GameState = {
    implicit val causedById: String = effectId
    val newState = effectById(effectId).getVariablesChangedState(key, value)(this)
    this.copy(characterEffectStates = characterEffectStates.updated(effectId, newState))
      .logEvent(EffectVariableSet(randomUUID(), phase, turn, causedById, effectId, key, value))
  }

  def decrementEffectCooldown(effectId: CharacterEffectId)(implicit random: Random): GameState = {
    val newState = effectById(effectId).getDecrementCooldownState(this)
    if(newState.cooldown > 0) {
      this.copy(characterEffectStates = characterEffectStates.updated(effectId, newState))
    } else {
      this.removeEffect(effectId)(random, id)
    }
  }

  def useAbility(abilityId: AbilityId, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).asInstanceOf[Ability with Usable]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsed(randomUUID(), phase, turn, causedById, abilityId))
    ability.use(useData)(random, newGameState)
      .afterAbilityUse(abilityId)
  }

  def useAbilityOnCoordinates(abilityId: AbilityId, target: HexCoordinates, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).asInstanceOf[Ability with UsableOnCoordinates]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsedOnCoordinates(randomUUID(), phase, turn, causedById, abilityId, target))
    ability.use(target, useData)(random, newGameState)
      .afterAbilityUse(abilityId)
  }

  def useAbilityOnCharacter(abilityId: AbilityId, target: CharacterId, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).asInstanceOf[Ability with UsableOnCharacter]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsedOnCharacter(randomUUID(), phase, turn, causedById, abilityId, target))
    ability.use(target, useData)(random, newGameState)
      .afterAbilityUse(abilityId)
  }

  def incrementTurn(): GameState =
    this.modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))

  def endTurn()(implicit random: Random, causedById: String = id): GameState = {
    this
      .logEvent(TurnFinished(randomUUID(), phase, turn, causedById))
      .decreaseTime(currentPlayer.id, millisSinceLastClockUpdate())
      .decrementEndTurnCooldowns()
      .modify(_.characterIdsThatTookActionThisPhase).using(c => c + characterTakingActionThisTurn.get)
      .modify(_.characterTakingActionThisTurn).setTo(None)
      .incrementTurn()
      .finishPhaseIfEveryCharacterTookAction()
      .skipTurnIfPlayerKnockedOut()
      .skipTurnIfNoCharactersToTakeAction()
      .logEvent(TurnStarted(randomUUID(), phase, turn, causedById))
  }

  def skipTurnIfNoCharactersToTakeAction()(implicit random: Random, causedById: String = id): GameState =
    if(currentPlayer.characterIds.intersect(charactersToTakeAction).isEmpty)
      incrementTurn()
    else this

  def skipTurnIfPlayerKnockedOut()(implicit random: Random, causedById: String = id): GameState = {
    if(gameStatus != GameStatus.Running) return this // prevent infinite loop if no one is playing
    if(currentPlayer.victoryStatus != VictoryStatus.Pending)
      incrementTurn()
    else this
  }

  def decrementEndTurnCooldowns()(implicit random: Random, causedById: String = id): GameState = {
    val currentCharacterAbilityIds = currentCharacterOpt.get.state.abilities.map(_.id)
    val currentCharacterEffectIds = currentCharacterOpt.get.state.effects.map(_.id)

    val decrementAbilityCooldownsState = currentCharacterAbilityIds.foldLeft(this)((acc, abilityId) => {
      acc.decrementAbilityCooldown(abilityId)
    })

    val decrementEffectCooldownsState = currentCharacterEffectIds.foldLeft(decrementAbilityCooldownsState)((acc, effectId) => {
      acc.decrementEffectCooldown(effectId)
    })
    decrementEffectCooldownsState
  }

  def passTurn(characterId: CharacterId)(implicit random: Random): GameState =
    takeActionWithCharacter(characterId).endTurn()

  def refreshCharacterTakenActions(): GameState =
    this.modify(_.characterIdsThatTookActionThisPhase).setTo(Set.empty)

  def incrementPhase(by: Int = 1): GameState =
    this.modify(_.phase).using(oldPhase => Phase(oldPhase.number + by))

  def finishPhase()(implicit random: Random, causedById: String = id): GameState =
    refreshCharacterTakenActions()
      .incrementPhase()
      .logEvent(PhaseFinished(randomUUID(), phase, turn, causedById))

  def finishPhaseIfEveryCharacterTookAction()(implicit random: Random): GameState =
    if(charactersToTakeAction.isEmpty) this.finishPhase()
    else this

  def toView(forPlayerOpt: Option[PlayerId]): GameStateView =
    GameStateView(
      id = id,
      charactersMetadata = charactersMetadata,
      gameStatus = gameStatus,
      pickType = pickType,
      numberOfBans = numberOfBans,
      numberOfCharactersPerPlayers = numberOfCharactersPerPlayers,
      draftPickState = draftPickState.map(_.toView(forPlayerOpt)),
      blindPickState = blindPickState.map(_.toView(forPlayerOpt)),
      hexMap = hexMap.toView(forPlayerOpt)(this),
      players = players,
      characters = characters.map(_.toView(forPlayerOpt)(this)),
      phase = phase,
      turn = turn,
      characterIdsOutsideMap = characterIdsOutsideMap,
      characterIdsThatTookActionThisPhase = characterIdsThatTookActionThisPhase,
      characterTakingActionThisTurn = characterTakingActionThisTurn,
      playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters,
      abilities = abilities.map(_.toView(this)),
      effects = effects.map(_.toView(this)),
      clockConfig = clockConfig,
      clock = getCurrentClock(),
      gameLog = gameLog.toView(forPlayerOpt)(this),

      currentPlayerId = currentPlayer.id,
      hostId = hostId ,
      isBlindPickingPhase = isBlindPickingPhase,
      isDraftBanningPhase = isDraftBanningPhase,
      isInCharacterSelect = isInCharacterSelect,
      isSharedTime = isSharedTime,
      currentPlayerTime = currentPlayerTime,
      charactersToTakeAction = charactersToTakeAction,
    )
}
