package com.tosware.nkm.models.game

import com.softwaremill.quicklens._
import com.tosware.nkm.actors.Game.GameId
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.GameEvent._
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.blindpick._
import com.tosware.nkm.models.game.draftpick._
import com.tosware.nkm.models.game.effects.{Block, FreeAbility}
import com.tosware.nkm.models.game.hex._
import com.tosware.nkm.models.{Damage, DamageType}
import com.tosware.nkm.{Logging, NkmUtils}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.util.Random

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
  clockConfig: ClockConfig,
  clock: Clock,
  lastTimestamp: Instant,
  gameLog: GameLog,
) extends NkmUtils
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

  def triggerAbilities: Set[Ability with GameEventListener] = abilities.collect {case a: GameEventListener => a}

  def triggerEffects: Set[CharacterEffect with GameEventListener] = effects.collect {case e: GameEventListener => e}

  def characterById(characterId: CharacterId): NkmCharacter = characters.find(_.id == characterId).get

  def abilityById(abilityId: AbilityId): Ability = abilities.find(_.id == abilityId).get

  def effectById(effectId: CharacterEffectId): CharacterEffect = effects.find(_.id == effectId).get

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
  private def handleTrigger(event: GameEvent, trigger: GameEventListener)(implicit random: Random, gameState: GameState) = {
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

  private def updateClock(newClock: Clock)(implicit random: Random, causedById: String): GameState =
    updateTimestamp().
      copy(clock = newClock).logEvent(ClockUpdated(NkmUtils.randomUUID(), phase, turn, causedById, newClock))

  private def updateGameStatus(newGameStatus: GameStatus): GameState =
    copy(gameStatus = newGameStatus)

  private def characterPickInitialPickTime: Long = pickType match {
    case PickType.AllRandom => clockConfig.timeAfterPickMillis
    case PickType.DraftPick => clockConfig.maxBanTimeMillis
    case PickType.BlindPick => clockConfig.maxPickTimeMillis
  }

  def initializeCharacterPick()(implicit random: Random): GameState =
    updateClock(clock.setSharedTime(characterPickInitialPickTime))(random, id)

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

  def placeCharactersRandomly(forPlayers: Set[PlayerId])(implicit random: Random): GameState = {
    players.filter(pl => forPlayers.contains(pl.id)).foldLeft(this){
      case (acc, p) =>
        val spawnCoords = hexMap.getSpawnPointsFor(p.id)(this).map(_.coordinates)
        val characterIdsShuffled = random.shuffle(p.characterIds.toSeq)
        val coordinatesToCharacterIdMap = spawnCoords.zip(characterIdsShuffled).toMap
        acc.placeCharacters(p.id, coordinatesToCharacterIdMap)(random)
    }
  }

  def pickAndPlaceCharactersRandomlyIfAllRandom()(implicit random: Random): GameState =
    if (pickType == PickType.AllRandom)
      assignCharactersToPlayers()
        .placeCharactersRandomly(players.map(_.id).toSet)
    else this

  def checkVictoryStatus(): GameState = {
    def filterPendingPlayers: Player => Boolean = _.victoryStatus == VictoryStatus.Pending

    if (gameStatus == GameStatus.CharacterPick && players.count(_.victoryStatus == VictoryStatus.Lost) > 0) {
      this.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
        .setTo(VictoryStatus.Drawn)
        .updateGameStatus(GameStatus.Finished)
    } else if (players.count(_.victoryStatus == VictoryStatus.Pending) == 1) {
      this.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
        .setTo(VictoryStatus.Won)
        .updateGameStatus(GameStatus.Finished)
    } else this
  }

  def generateCharacter(characterMetadataId: CharacterMetadataId)(implicit random: Random): NkmCharacter = {
    val characterId = NkmUtils.randomUUID()
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

  def checkIfCharacterPickFinished()(implicit random: Random): GameState = {
    if(characterPickFinished) {
      updateGameStatus(GameStatus.CharacterPicked)
        .updateClock(clock.setSharedTime(clockConfig.timeAfterPickMillis))(random, id)
        .assignCharactersToPlayers()
        .logEvent(CharactersPicked(NkmUtils.randomUUID(), phase, turn, id))
    } else this
  }

  def startPlacingCharacters()(implicit random: Random): GameState =
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

  def surrender(playerIds: PlayerId*)(implicit random: Random): GameState = {
    def filterPlayers: Player => Boolean = p => playerIds.contains(p.name)

    this
      .modify(_.players.eachWhere(filterPlayers).victoryStatus).setTo(VictoryStatus.Lost)
      .logEvents(playerIds.map(pid => PlayerLost(randomUUID(), phase, turn, id, pid)))
      .checkVictoryStatus()
      .skipTurnIfPlayerKnockedOut()(random, playerIds.mkString(", "))
  }

  def ban(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): GameState =
    copy(draftPickState = draftPickState.map(_.ban(playerId, characterIds)))

  def finishBanningPhase()(implicit random: Random): GameState =
    copy(
      draftPickState = draftPickState.map(_.finishBanning()),
    ).updateClock(clock.setSharedTime(clockConfig.maxPickTimeMillis))(random, id)

  def pick(playerId: PlayerId, characterId: CharacterMetadataId)(implicit random: Random): GameState =
    copy(
      draftPickState = draftPickState.map(_.pick(playerId, characterId)),
    ).updateClock(clock.setSharedTime(clockConfig.maxPickTimeMillis))(random, id)
      .checkIfCharacterPickFinished()

  def draftPickTimeout()(implicit random: Random): GameState =
    surrender(draftPickState.get.currentPlayerPicking.get)

  def blindPick(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(implicit random: Random): GameState =
    copy(blindPickState = blindPickState.map(_.pick(playerId, characterIds)))
      .checkIfCharacterPickFinished()

  def blindPickTimeout()(implicit random: Random): GameState =
    surrender(blindPickState.get.pickingPlayers: _*)

  def placingCharactersTimeout()(implicit random: Random): GameState = {
    val pidsThatDidNotPlace: Set[PlayerId] = players.map(_.id).toSet -- playerIdsThatPlacedCharacters
    placeCharactersRandomly(pidsThatDidNotPlace)
  }

  def checkIfPlacingCharactersFinished(): GameState =
    if(placingCharactersFinished) updateGameStatus(GameStatus.Running) else this

  def placeCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId])(implicit random: Random): GameState =
    coordinatesToCharacterIdMap.foldLeft(this){case (acc, (coordinate, characterId)) => acc.placeCharacter(coordinate, characterId)(random, playerId)}
      .copy(playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters + playerId)
      .checkIfPlacingCharactersFinished()

  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
    .modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))
      .logEvent(CharacterPlaced(NkmUtils.randomUUID(), phase, turn, causedById, characterId, targetCellCoordinates))

  def basicMoveCharacter(characterId: CharacterId, path: Seq[HexCoordinates])(implicit random: Random): GameState = {
    implicit val causedById: CharacterId = characterId
    val newGameState = takeActionWithCharacter(characterId)
    characterById(characterId).basicMove(path)(random, newGameState)
      .logEvent(CharacterBasicMoved(NkmUtils.randomUUID(), phase, turn, causedById, characterId, path))
  }

  def teleportCharacter(characterId: CharacterId, targetCellCoordinates: HexCoordinates)(implicit random: Random, causedById: String): GameState = {
    val parentCellOpt = characterById(characterId).parentCell(this)

    val removedFromParentCellState = parentCellOpt.fold(this)(c => updateHexCell(c.coordinates)(_.copy(characterId = None)))
    val targetIsFreeToStand = hexMap.getCell(targetCellCoordinates).get.isFreeToStand
    val characterIsOnMap = characterById(characterId).isOnMap(this)

    if (targetIsFreeToStand) {
      if (characterIsOnMap)
        removedFromParentCellState.updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
      else removedFromParentCellState.placeCharacter(targetCellCoordinates, characterId)
    } else {
      // probably just passing by a friendly characterOpt
      removedFromParentCellState.removeCharacterFromMap(characterId)
    }.logEvent(CharacterTeleported(NkmUtils.randomUUID(), phase, turn, causedById, characterId, targetCellCoordinates))
  }


  def basicAttack(attackingCharacterId: CharacterId, targetCharacterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: CharacterId = attackingCharacterId
    val newGameState = takeActionWithCharacter(attackingCharacterId)
      .logEvent(CharacterPreparedToAttack(NkmUtils.randomUUID(), phase, turn, causedById, attackingCharacterId, targetCharacterId))

    val attackingCharacter = newGameState.characterById(attackingCharacterId)
    val targetCharacter = newGameState.characterById(targetCharacterId)
    val blockEffects = targetCharacter.state.effects.ofType[Block]
    if(blockEffects.nonEmpty) {
      newGameState.removeEffect(blockEffects.head.id)
    } else {
      attackingCharacter.basicAttack(targetCharacterId)(random, newGameState)
        .logEvent(CharacterBasicAttacked(NkmUtils.randomUUID(), phase, turn, causedById, attackingCharacterId, targetCharacterId))
    }
  }

  private def updatePlayer(playerId: PlayerId)(updateFunction: Player => Player): GameState =
    this.modify(_.players.each).using {
      case player if player.id == playerId => updateFunction(player)
      case player => player
    }

  private def updateCharacter(characterId: CharacterId)(updateFunction: NkmCharacter => NkmCharacter): GameState =
    this.modify(_.characters.each).using {
      case character if character.id == characterId => updateFunction(character)
      case character => character
    }

  private def updateHexCell(targetCoords: HexCoordinates)(updateFunction: HexCell => HexCell): GameState =
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
        .logEvent(CharacterDamaged(NkmUtils.randomUUID(), phase, turn, causedById, characterId, damage))
    }
  }

  def heal(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
    if(characterById(characterId).isDead) {
      logger.error(s"Unable to heal character $characterId. Character dead.")
      this
    } else {
      updateCharacter(characterId)(_.heal(amount))
        .logEvent(CharacterHealed(NkmUtils.randomUUID(), phase, turn, causedById, characterId, amount))
    }

  def setHp(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.modify(_.state.healthPoints).setTo(amount))
      .logEvent(CharacterHpSet(NkmUtils.randomUUID(), phase, turn, causedById, characterId, amount))

  def setShield(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.modify(_.state.shield).setTo(amount))
      .logEvent(CharacterShieldSet(NkmUtils.randomUUID(), phase, turn, causedById, characterId, amount))

  def setStat(characterId: CharacterId, statType: StatType, amount: Int)(implicit random: Random, causedById: String): GameState = {
    val updateStat = statType match {
      case StatType.AttackPoints => modify(_: NkmCharacter)(_.state.pureAttackPoints)
      case StatType.BasicAttackRange => modify(_: NkmCharacter)(_.state.pureBasicAttackRange)
      case StatType.Speed => modify(_: NkmCharacter)(_.state.pureSpeed)
      case StatType.PhysicalDefense => modify(_: NkmCharacter)(_.state.purePhysicalDefense)
      case StatType.MagicalDefense => modify(_: NkmCharacter)(_.state.pureMagicalDefense)
    }
    updateCharacter(characterId)(c => updateStat(c).setTo(amount))
      .logEvent(CharacterStatSet(NkmUtils.randomUUID(), phase, turn, causedById, characterId, statType, amount))
  }

  def checkIfCharacterDied(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    if(characterById(characterId).isDead) {
      handleCharacterDeath(characterId)
    } else this

  def knockOutPlayer(playerId: PlayerId)(implicit random: Random): GameState =
    updatePlayer(playerId)(_.modify(_.victoryStatus).setTo(VictoryStatus.Lost))
      .checkVictoryStatus()
      .skipTurnIfPlayerKnockedOut()(random, playerId)

  def checkIfPlayerKnockedOut(playerId: PlayerId)(implicit random: Random): GameState =
    if(characters.filter(_.owner(this).id == playerId).forall(_.isDead)) {
      knockOutPlayer(playerId)
    } else this

  def handleCharacterDeath(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    this.removeCharacterFromMap(characterId)
      .logEvent(CharacterDied(NkmUtils.randomUUID(), phase, turn, causedById, characterId))
      .checkIfPlayerKnockedOut(characterById(characterId).owner(this).id)

  def addEffect(characterId: CharacterId, characterEffect: CharacterEffect)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.addEffect(characterEffect))
      .modify(_.characterEffectStates).using(ces => ces.updated(characterEffect.id, CharacterEffectState(characterEffect.initialCooldown)))
      .logEvent(EffectAddedToCharacter(NkmUtils.randomUUID(), phase, turn, causedById, characterEffect.id, characterId))

  def removeEffects(characterEffectIds: Seq[CharacterEffectId])(implicit random: Random, causedById: String): GameState =
    characterEffectIds.foldLeft(this){case (acc, eid) => acc.removeEffect(eid)}

  def removeEffect(characterEffectId: CharacterEffectId)(implicit random: Random, causedById: String): GameState = {
    val character = effectById(characterEffectId).parentCharacter(this)
    updateCharacter(character.id)(_.removeEffect(characterEffectId))
      .modify(_.characterEffectStates).using(ces => ces.removed(characterEffectId))
      .logEvent(EffectRemovedFromCharacter(NkmUtils.randomUUID(), phase, turn, causedById, characterEffectId))
  }

  def removeCharacterFromMap(characterId: CharacterId)(implicit random: Random, causedById: String): GameState = {
    val parentCellOpt = characterById(characterId).parentCell(this)

    parentCellOpt.fold(this)(c => updateHexCell(c.coordinates)(_.copy(characterId = None)))
      .modify(_.characterIdsOutsideMap).setTo(characterIdsOutsideMap + characterId)
      .logEvent(CharacterRemovedFromMap(NkmUtils.randomUUID(), phase, turn, causedById, characterId))
  }

  def takeActionWithCharacter(characterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: String = characterId

    if(characterTakingActionThisTurn.isDefined) // do not log event more than once
      return this
    this.modify(_.characterTakingActionThisTurn)
      .setTo(Some(characterId))
      .logEvent(CharacterTookAction(NkmUtils.randomUUID(), phase, turn, causedById, characterId))
  }

  def abilityHitCharacter(abilityId: AbilityId, targetCharacter: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    logEvent(AbilityHitCharacter(NkmUtils.randomUUID(), phase, turn, causedById, abilityId: AbilityId, targetCharacter: CharacterId))
  }

  def refreshBasicMove(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
    logEvent(BasicMoveRefreshed(NkmUtils.randomUUID(), phase, turn, causedById, targetCharacter))

  def refreshBasicAttack(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
    logEvent(BasicAttackRefreshed(NkmUtils.randomUUID(), phase, turn, causedById, targetCharacter))

  def putAbilityOnCooldown(abilityId: AbilityId): GameState = {
    val newState = abilityById(abilityId).getCooldownState(this)
    this.copy(abilityStates = abilityStates.updated(abilityId, newState))
  }

  def afterAbilityUse(abilityId: AbilityId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId

    val ngs = if(abilityStates(abilityId).isEnabled) this
    else putAbilityOnCooldownOrDecrementFreeAbility(abilityId)

    ngs.logEvent(AbilityUseFinished(NkmUtils.randomUUID(), phase, turn, causedById, abilityId))
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

  def decrementEffectCooldown(effectId: CharacterEffectId)(implicit random: Random): GameState = {
    val newState = effectById(effectId).getDecrementCooldownState(this)
    if(newState.cooldown > 0) {
      this.copy(characterEffectStates = characterEffectStates.updated(effectId, newState))
    } else {
      this.removeEffect(effectId)(random, id)
    }
  }

  def useAbilityWithoutTarget(abilityId: AbilityId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).asInstanceOf[Ability with UsableWithoutTarget]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsedWithoutTarget(NkmUtils.randomUUID(), phase, turn, causedById, abilityId))
    ability.use()(random, newGameState)
      .afterAbilityUse(abilityId)
  }

  def useAbilityOnCoordinates(abilityId: AbilityId, target: HexCoordinates, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).asInstanceOf[Ability with UsableOnCoordinates]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsedOnCoordinates(NkmUtils.randomUUID(), phase, turn, causedById, abilityId, target))
    ability.use(target, useData)(random, newGameState)
      .afterAbilityUse(abilityId)
  }

  def useAbilityOnCharacter(abilityId: AbilityId, target: CharacterId, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).asInstanceOf[Ability with UsableOnCharacter]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsedOnCharacter(NkmUtils.randomUUID(), phase, turn, causedById, abilityId, target))
    ability.use(target, useData)(random, newGameState)
      .afterAbilityUse(abilityId)
  }

  def incrementTurn(): GameState =
    this.modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))

  def finishTurn()(implicit random: Random, causedById: String = id): GameState = {
    this
      .decreaseTime(currentPlayer.id, millisSinceLastClockUpdate())
      .incrementTurn()
      .logEvent(TurnFinished(NkmUtils.randomUUID(), phase, turn, causedById))
      .finishPhaseIfEveryCharacterTookAction()
      .logEvent(TurnStarted(NkmUtils.randomUUID(), phase, turn, causedById))
      .skipTurnIfPlayerKnockedOut()
      .skipTurnIfNoCharactersToTakeAction()
  }

  def skipTurnIfNoCharactersToTakeAction()(implicit random: Random, causedById: String = id): GameState =
    if(currentPlayer.characterIds.intersect(charactersToTakeAction).isEmpty)
      finishTurn()
    else this

  def skipTurnIfPlayerKnockedOut()(implicit random: Random, causedById: String = id): GameState = {
    if(gameStatus != GameStatus.Running) return this // prevent infinite loop if no one is playing
    if(currentPlayer.victoryStatus != VictoryStatus.Pending)
      finishTurn()
    else this
  }

  def endTurn()(implicit random: Random, causedById: String = id): GameState = {
    val currentCharacterAbilityIds = currentCharacterOpt.get.state.abilities.map(_.id)
    val currentCharacterEffectIds = currentCharacterOpt.get.state.effects.map(_.id)

    val decrementAbilityCooldownsState = currentCharacterAbilityIds.foldLeft(this)((acc, abilityId) => {
      acc.decrementAbilityCooldown(abilityId)
    })


    val decrementEffectCooldownsState = currentCharacterEffectIds.foldLeft(decrementAbilityCooldownsState)((acc, effectId) => {
      acc.decrementEffectCooldown(effectId)
    })

    decrementEffectCooldownsState
      .modify(_.characterIdsThatTookActionThisPhase).using(c => c + characterTakingActionThisTurn.get)
      .modify(_.characterTakingActionThisTurn).setTo(None)
      .finishTurn()
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
      .logEvent(PhaseFinished(NkmUtils.randomUUID(), phase, turn, causedById))

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
      clock = clock,
      gameLog = gameLog.toView(forPlayerOpt),

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
      clockConfig = defaultClockConfig,
      clock = Clock.fromConfig(defaultClockConfig, Seq()),
      lastTimestamp = Instant.now(),
      gameLog = GameLog(Seq.empty),
    )
  }
}

case class GameStateView(
  id: GameId,
  charactersMetadata: Set[CharacterMetadata],
  gameStatus: GameStatus,
  pickType: PickType,
  numberOfBans: Int,
  numberOfCharactersPerPlayers: Int,
  draftPickState: Option[DraftPickStateView],
  blindPickState: Option[BlindPickStateView],
  hexMap: HexMapView,
  players: Seq[Player],
  characters: Set[NkmCharacterView],
  abilities: Set[AbilityView],
  effects: Set[CharacterEffectView],
  phase: Phase,
  turn: Turn,
  characterIdsOutsideMap: Set[CharacterId],
  characterIdsThatTookActionThisPhase: Set[CharacterId],
  characterTakingActionThisTurn: Option[CharacterId],
  playerIdsThatPlacedCharacters: Set[PlayerId],
  clockConfig: ClockConfig,
  clock: Clock,
  gameLog: GameLogView,

  currentPlayerId: PlayerId,
  hostId: PlayerId,
  isBlindPickingPhase: Boolean,
  isDraftBanningPhase: Boolean,
  isInCharacterSelect: Boolean,
  isSharedTime: Boolean,
  currentPlayerTime: Long,
  charactersToTakeAction: Set[CharacterId],
)
