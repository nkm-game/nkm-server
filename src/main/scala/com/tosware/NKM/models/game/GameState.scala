package com.tosware.NKM.models.game

import com.softwaremill.quicklens._
import com.tosware.NKM.actors.Game.GameId
import com.tosware.NKM.models.Damage
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.CharacterEffect.CharacterEffectId
import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.GameEvent._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game.blindpick._
import com.tosware.NKM.models.game.draftpick._
import com.tosware.NKM.models.game.hex.{HexCell, HexCoordinates, HexMap, NKMUtils}

import scala.util.Random

object GameEvent {
  type GameEventId = String
  abstract class GameEvent(val eid: GameEventId)(implicit val phase: Phase, val turn: Turn, val causedById: String) {
    def index(implicit gameState: GameState): Int =
      gameState.gameLog.events.indexWhere(_.eid == eid)
  }
  trait ContainsCharacterId {
    val characterId: CharacterId
  }
  trait ContainsAbilityId {
    val abilityId: AbilityId
  }

  case class ClockUpdated(id: GameEventId, newClock: Clock)
                         (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class CharacterPlaced(id: GameEventId, characterId: CharacterId, target: HexCoordinates)
                            (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class EffectAppliedOnCell(id: GameEventId, effectId: String, target: HexCoordinates)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class EffectAppliedOnCharacter(id: GameEventId, effectId: CharacterEffectId, characterId: CharacterId)
                                     (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class EffectRemovedFromCharacter(id: GameEventId, effectId: CharacterEffectId)
                                       (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class AbilityHitCharacter(id: GameEventId, abilityId: AbilityId, targetCharacterId: CharacterId)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class AbilityUsedWithoutTarget(id: GameEventId, abilityId: AbilityId)
                                     (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class AbilityUsedOnCoordinates(id: GameEventId, abilityId: AbilityId, target: HexCoordinates)
                                     (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class AbilityUsedOnCharacter(id: GameEventId, abilityId: AbilityId, targetCharacterId: CharacterId)
                                   (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsAbilityId
  case class CharacterBasicMoved(id: GameEventId, characterId: CharacterId, path: Seq[HexCoordinates])
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterBasicAttacked(id: GameEventId, characterId: CharacterId, targetCharacterId: CharacterId)
                                   (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterTeleported(id: GameEventId, characterId: CharacterId, target: HexCoordinates)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterDamaged(id: GameEventId, characterId: CharacterId, damage: Damage)
                             (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterHealed(id: GameEventId, characterId: CharacterId, amount: Int)
                            (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterHpSet(id: GameEventId, characterId: CharacterId, amount: Int)
                           (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterStatSet(id: GameEventId, characterId: CharacterId, statType: StatType, amount: Int)
                             (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterDied(id: GameEventId, characterId: CharacterId)
                          (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterRemovedFromMap(id: GameEventId, characterId: CharacterId)
                                    (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class CharacterTookAction(id: GameEventId, characterId: CharacterId)
                                (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class BasicAttackRefreshed(id: GameEventId, characterId: CharacterId)
                                 (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class BasicMoveRefreshed(id: GameEventId, characterId: CharacterId)
                               (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
    with ContainsCharacterId
  case class TurnFinished(id: GameEventId)
                         (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
  case class PhaseFinished(id: GameEventId)
                          (implicit phase: Phase, turn: Turn, causedById: String) extends GameEvent(id)
}
case class GameLog(events: Seq[GameEvent])

case class GameState(
                      id: GameId,
                      charactersMetadata: Set[CharacterMetadata],
                      gameStatus: GameStatus,
                      pickType: PickType,
                      numberOfBans: Int,
                      numberOfCharactersPerPlayers: Int,
                      draftPickState: Option[DraftPickState],
                      blindPickState: Option[BlindPickState],
                      hexMap: Option[HexMap],
                      players: Seq[Player],
                      characters: Set[NKMCharacter],
                      phase: Phase,
                      turn: Turn,
                      characterIdsOutsideMap: Set[CharacterId],
                      characterIdsThatTookActionThisPhase: Set[CharacterId],
                      characterTakingActionThisTurn: Option[CharacterId],
                      playerIdsThatPlacedCharacters: Set[PlayerId],
                      clockConfig: ClockConfig,
                      clock: Clock,
                      gameLog: GameLog,
                    ) {
  private implicit val p: Phase = phase
  private implicit val t: Turn = turn

  def host: Player = players.find(_.isHost).get

  def currentPlayerNumber: Int = turn.number % players.size

  def playerNumber(playerId: PlayerId): Int = players.indexWhere(_.id == playerId)

  def playerById(playerId: PlayerId): Option[Player] = players.find(_.id == playerId)

  def currentPlayer: Player = players(currentPlayerNumber)

  def currentPlayerTime: Long = clock.playerTimes(currentPlayer.id)

  def abilities: Set[Ability] = characters.flatMap(_.state.abilities)

  def triggerAbilities: Set[Ability with GameEventListener] = abilities.collect {case a: GameEventListener => a}

  def characterById(characterId: CharacterId): Option[NKMCharacter] = characters.find(_.id == characterId)

  def abilityById(abilityId: AbilityId): Option[Ability] = abilities.find(_.id == abilityId)

  def characterByEffectId(characterEffectId: CharacterEffectId): NKMCharacter =
    characters.find(_.state.effects.exists(_.id == characterEffectId)).get

  def characterPickFinished: Boolean = {
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

  private def executeEventTriggers(e: GameEvent)(implicit random: Random): GameState =
    triggerAbilities.foldLeft(this)((acc, ability) => ability.onEvent(e)(random, acc))

  private def logEvent(e: GameEvent)(implicit random: Random): GameState =
    copy(gameLog = gameLog.modify(_.events).using(es => es :+ e))
      .executeEventTriggers(e)

  private def updateClock(newClock: Clock)(implicit random: Random, causedById: String): GameState =
    copy(clock = newClock).logEvent(ClockUpdated(NKMUtils.randomUUID, newClock))

  private def updateGameStatus(newGameStatus: GameStatus): GameState =
    copy(gameStatus = newGameStatus)

  private def pickTime: Long = pickType match {
    case PickType.AllRandom => clockConfig.timeAfterPickMillis
    case PickType.DraftPick => clockConfig.maxBanTimeMillis
    case PickType.BlindPick => clockConfig.maxPickTimeMillis
  }

  def initializeCharacterPick()(implicit random: Random): GameState =
    updateClock(clock.setPickTime(pickTime))(random, id)

  def startGame(g: GameStartDependencies)(implicit random: Random): GameState = {
    copy(
      charactersMetadata = g.charactersMetadata,
      players = g.players,
      hexMap = Some(g.hexMap),
      pickType = g.pickType,
      numberOfBans = g.numberOfBansPerPlayer,
      numberOfCharactersPerPlayers = g.numberOfCharactersPerPlayer,
      gameStatus = if (g.pickType == PickType.AllRandom) GameStatus.CharacterPicked else GameStatus.CharacterPick,
      draftPickState = if (g.pickType == PickType.DraftPick) Some(DraftPickState.empty(DraftPickConfig.generate(g))) else None,
      blindPickState = if (g.pickType == PickType.BlindPick) Some(BlindPickState.empty(BlindPickConfig.generate(g))) else None,
      clockConfig = g.clockConfig,
      clock = Clock.fromConfig(g.clockConfig, playerOrder = g.players.map(_.name)),
    ).initializeCharacterPick()
  }

  def placeCharactersRandomlyIfAllRandom()(implicit random: Random): GameState = {
    if (pickType == PickType.AllRandom) {
      // TODO: place characters as now they are outside of the map
      // TODO: move character assignment to start game
      assignCharactersToPlayers().copy(
        playerIdsThatPlacedCharacters = players.map(_.id).toSet,
      ).updateGameStatus(GameStatus.Running)
    } else this
  }

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

  def generateCharacter(characterMetadataId: CharacterMetadataId)(implicit random: Random): NKMCharacter = {
    val characterId = NKMUtils.randomUUID
    val metadata = charactersMetadata.find(_.id == characterMetadataId).get
    NKMCharacter.fromMetadata(characterId, metadata)
  }

  def assignCharactersToPlayers()(implicit random: Random): GameState = {
    val characterSelection: Map[PlayerId, Iterable[CharacterMetadataId]] = pickType match {
      case PickType.AllRandom =>
        val pickedCharacters = random
          .shuffle(charactersMetadata.map(_.id))
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

    copy(
      players = playersWithAssignedCharacters,
      characters = characters,
      characterIdsOutsideMap = characters.map(c => c.id),
    )
  }

  def checkIfCharacterPickFinished()(implicit random: Random): GameState =
    if(characterPickFinished) {
      updateGameStatus(GameStatus.CharacterPicked)
        .updateClock(clock.setPickTime(clockConfig.timeAfterPickMillis))(random, id)
        .assignCharactersToPlayers()
    } else this

  def startPlacingCharacters()(implicit random: Random): GameState =
    updateGameStatus(GameStatus.CharacterPlacing).placeCharactersRandomlyIfAllRandom()

  def decreasePickTime(timeMillis: Long)(implicit random: Random): GameState =
    updateClock(clock.decreasePickTime(timeMillis))(random, id)

  def decreaseTime(playerId: PlayerId, timeMillis: Long)(implicit random: Random): GameState =
    updateClock(clock.decreaseTime(playerId, timeMillis))(random, playerId)

  def increaseTime(playerId: PlayerId, timeMillis: Long)(implicit random: Random): GameState =
    updateClock(clock.increaseTime(playerId, timeMillis))(random, playerId)

  def pause()(implicit random: Random): GameState =
    updateClock(clock.pause())(random, id)

  def unpause()(implicit random: Random): GameState =
    updateClock(clock.unpause())(random, id)

  def surrender(playerIds: PlayerId*): GameState = {
    def filterPlayers: Player => Boolean = p => playerIds.contains(p.name)

    this.modify(_.players.eachWhere(filterPlayers).victoryStatus).setTo(VictoryStatus.Lost).checkVictoryStatus()
  }

  def ban(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): GameState =
    copy(draftPickState = draftPickState.map(_.ban(playerId, characterIds)))

  def finishBanningPhase()(implicit random: Random): GameState =
    copy(
      draftPickState = draftPickState.map(_.finishBanning()),
    ).updateClock(clock.setPickTime(clockConfig.maxPickTimeMillis))(random, id)

  def pick(playerId: PlayerId, characterId: CharacterMetadataId)(implicit random: Random): GameState =
    copy(
      draftPickState = draftPickState.map(_.pick(playerId, characterId)),
    ).updateClock(clock.setPickTime(clockConfig.maxPickTimeMillis))(random, id)
      .checkIfCharacterPickFinished()

  def draftPickTimeout(): GameState =
    surrender(draftPickState.get.currentPlayerPicking.get)

  def blindPick(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(implicit random: Random): GameState =
    copy(blindPickState = blindPickState.map(_.pick(playerId, characterIds)))
      .checkIfCharacterPickFinished()

  def blindPickTimeout(): GameState =
    surrender(blindPickState.get.pickingPlayers: _*)

  def checkIfPlacingCharactersFinished(): GameState =
    if(placingCharactersFinished) updateGameStatus(GameStatus.Running) else this

  def placeCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId])(implicit random: Random): GameState =
    coordinatesToCharacterIdMap.foldLeft(this){case (acc, (coordinate, characterId)) => acc.placeCharacter(coordinate, characterId)(random, playerId)}
      .copy(playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters + playerId)
      .checkIfPlacingCharactersFinished()

  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId)(implicit random: Random, causedBy: String): GameState =
    updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
    .modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))
      .logEvent(CharacterPlaced(NKMUtils.randomUUID, characterId, targetCellCoordinates))

  def basicMoveCharacter(playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedBy: CharacterId = characterId
    val newGameState = takeActionWithCharacter(characterId)
    // case if character dies on the way? make a test of this and create a new functions with while(onMap)
    path.tail.foldLeft(newGameState)((acc, coordinate) => acc.teleportCharacter(coordinate, characterId)(random, playerId))
      .logEvent(CharacterBasicMoved(NKMUtils.randomUUID, characterId, path))
  }

  def teleportCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId)(implicit random: Random, causedBy: String): GameState = {
    val parentCellOpt = characterById(characterId).get.parentCell(this)

    parentCellOpt.fold(this)(c => updateHexCell(c.coordinates)(_.copy(characterId = None)))
    .updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
    .logEvent(CharacterTeleported(NKMUtils.randomUUID, characterId, targetCellCoordinates))
  }


  def basicAttack(attackingCharacterId: CharacterId, targetCharacterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedBy: CharacterId = attackingCharacterId
    val newGameState = takeActionWithCharacter(attackingCharacterId)
    val attackingCharacter = characterById(attackingCharacterId).get
    attackingCharacter.basicAttack(targetCharacterId)(random, newGameState)
      .logEvent(CharacterBasicAttacked(NKMUtils.randomUUID, attackingCharacterId, targetCharacterId))
  }

  private def updateCharacter(characterId: CharacterId)(updateFunction: NKMCharacter => NKMCharacter): GameState =
    this.modify(_.characters.each).using {
      case character if character.id == characterId => updateFunction(character)
      case character => character
    }

  private def updateHexCell(targetCoords: HexCoordinates)(updateFunction: HexCell => HexCell): GameState =
    this.modify(_.hexMap.each.cells.each).using {
      case cell if cell.coordinates == targetCoords => updateFunction(cell)
      case cell => cell
    }

  def updateAbility(abilityId: AbilityId, newAbility: Ability): GameState =
    this.modify(_.characters.each.state.abilities.each).using {
      case ability if ability.id == abilityId => newAbility
      case ability => ability
    }

  def damageCharacter(characterId: CharacterId, damage: Damage)(implicit random: Random, causedBy: String): GameState =
    updateCharacter(characterId)(_.receiveDamage(damage))
      .logEvent(CharacterDamaged(NKMUtils.randomUUID, characterId, damage))
      .removeFromMapIfDead(characterId)

  def heal(characterId: CharacterId, amount: Int)(implicit random: Random, causedBy: String): GameState =
    updateCharacter(characterId)(_.heal(amount))
      .logEvent(CharacterHealed(NKMUtils.randomUUID, characterId, amount))

  def setHp(characterId: CharacterId, amount: Int)(implicit random: Random, causedBy: String): GameState =
    updateCharacter(characterId)(_.modify(_.state.healthPoints).setTo(amount))
      .logEvent(CharacterHpSet(NKMUtils.randomUUID, characterId, amount))

  def setStat(characterId: CharacterId, statType: StatType, amount: Int)(implicit random: Random, causedBy: String): GameState = {
    val updateStat = statType match {
      case StatType.AttackPoints => modify(_: NKMCharacter)(_.state.pureAttackPoints)
      case StatType.BasicAttackRange => modify(_: NKMCharacter)(_.state.pureBasicAttackRange)
      case StatType.Speed => modify(_: NKMCharacter)(_.state.pureSpeed)
      case StatType.PhysicalDefense => modify(_: NKMCharacter)(_.state.purePhysicalDefense)
      case StatType.MagicalDefense => modify(_: NKMCharacter)(_.state.pureMagicalDefense)
    }
    updateCharacter(characterId)(c => updateStat(c).setTo(amount))
      .logEvent(CharacterStatSet(NKMUtils.randomUUID, characterId, statType, amount))
  }

  def removeFromMapIfDead(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
    if(characterById(characterId).get.isDead) {
      logEvent(CharacterDied(NKMUtils.randomUUID, characterId))
        .removeCharacterFromMap(characterId)
    } else this

  def addEffect(characterId: CharacterId, characterEffect: CharacterEffect)(implicit random: Random, causedById: String): GameState =
    updateCharacter(characterId)(_.addEffect(characterEffect))
      .logEvent(EffectAppliedOnCharacter(NKMUtils.randomUUID, characterEffect.id, characterId))

  def removeEffects(characterEffectIds: Seq[CharacterEffectId])(implicit random: Random, causedById: String): GameState =
    characterEffectIds.foldLeft(this){case (acc, eid) => acc.removeEffect(eid)}

  def removeEffect(characterEffectId: CharacterEffectId)(implicit random: Random, causedById: String): GameState = {
    val character = characterByEffectId(characterEffectId)
    updateCharacter(character.id)(_.removeEffect(characterEffectId))
      .logEvent(EffectRemovedFromCharacter(NKMUtils.randomUUID, characterEffectId))
  }

  def removeCharacterFromMap(characterId: CharacterId)(implicit random: Random, causedById: String): GameState = {
    val parentCellOpt = characterById(characterId).get.parentCell(this)

    parentCellOpt.fold(this)(c => updateHexCell(c.coordinates)(_.copy(characterId = None)))
      .modify(_.characterIdsOutsideMap).setTo(characterIdsOutsideMap + characterId)
      .logEvent(CharacterRemovedFromMap(NKMUtils.randomUUID, characterId))
  }

  def takeActionWithCharacter(characterId: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: String = characterId

    if(characterTakingActionThisTurn.isDefined) // do not log event more than once
      return this
    this.modify(_.characterTakingActionThisTurn)
      .setTo(Some(characterId))
      .logEvent(CharacterTookAction(NKMUtils.randomUUID, characterId))
  }

  def abilityHitCharacter(abilityId: AbilityId, targetCharacter: CharacterId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    logEvent(AbilityHitCharacter(NKMUtils.randomUUID, abilityId: AbilityId, targetCharacter: CharacterId))
  }

  def refreshBasicMove(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
    logEvent(BasicMoveRefreshed(NKMUtils.randomUUID, targetCharacter))

  def refreshBasicAttack(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
    logEvent(BasicAttackRefreshed(NKMUtils.randomUUID, targetCharacter))

  def useAbilityWithoutTarget(abilityId: AbilityId)(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).get.asInstanceOf[Ability with UsableWithoutTarget]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
      .logEvent(AbilityUsedWithoutTarget(NKMUtils.randomUUID, abilityId))
    ability.use()(random, newGameState)
  }

  def useAbilityOnCoordinates(abilityId: AbilityId, target: HexCoordinates, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).get.asInstanceOf[Ability with UsableOnCoordinates]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
    ability.use(target, useData)(random, newGameState)
      .logEvent(AbilityUsedOnCoordinates(NKMUtils.randomUUID, abilityId, target))
  }

  def useAbilityOnCharacter(abilityId: AbilityId, target: CharacterId, useData: UseData = UseData())(implicit random: Random): GameState = {
    implicit val causedById: String = abilityId
    val ability = abilityById(abilityId).get.asInstanceOf[Ability with UsableOnCharacter]
    val parentCharacter = ability.parentCharacter(this)

    val newGameState = takeActionWithCharacter(parentCharacter.id)
    ability.use(target, useData)(random, newGameState)
      .logEvent(AbilityUsedOnCharacter(NKMUtils.randomUUID, abilityId, target))
  }

  def incrementTurn(): GameState =
    this.modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))

  def endTurn()(implicit random: Random, causedById: String = id): GameState =
    this.modify(_.characterIdsThatTookActionThisPhase).using(c => c + characterTakingActionThisTurn.get)
      .modify(_.characterTakingActionThisTurn).setTo(None)
      .incrementTurn()
      .finishPhaseIfEveryCharacterTookAction()
      .logEvent(TurnFinished(NKMUtils.randomUUID))

  def passTurn(characterId: CharacterId)(implicit random: Random): GameState =
    takeActionWithCharacter(characterId).endTurn()

  def refreshCharacterTakenActions(): GameState =
    this.modify(_.characterIdsThatTookActionThisPhase).setTo(Set.empty)

  def incrementPhase(by: Int = 1): GameState =
    this.modify(_.phase).using(oldPhase => Phase(oldPhase.number + by))

  def finishPhase()(implicit random: Random, causedById: String = id): GameState =
    refreshCharacterTakenActions()
      .incrementPhase()
      .logEvent(PhaseFinished(NKMUtils.randomUUID))

  def finishPhaseIfEveryCharacterTookAction()(implicit random: Random): GameState =
    if(characterIdsThatTookActionThisPhase == characters.map(_.id))
      this.finishPhase()
    else this

  def toView(forPlayer: Option[PlayerId]): GameStateView =
    GameStateView(
      id = id,
      charactersMetadata = charactersMetadata,
      gameStatus = gameStatus,
      pickType = pickType,
      numberOfBans = numberOfBans,
      numberOfCharactersPerPlayers = numberOfCharactersPerPlayers,
      draftPickState = draftPickState.map(_.toView(forPlayer)),
      blindPickState = blindPickState.map(_.toView(forPlayer)),
      hexMap = hexMap,
      players = players,
      characters = characters.map(_.toView),
      phase = phase,
      turn = turn,
      characterIdsOutsideMap = characterIdsOutsideMap,
      characterIdsThatTookActionThisPhase = characterIdsThatTookActionThisPhase,
      characterTakingActionThisTurn = characterTakingActionThisTurn,
      playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters,
      clockConfig = clockConfig,
      clock = clock,
      currentPlayerId = currentPlayer.id,

    )

  def shortInfo: String =
    s"""
      | id: $id
      | hexMap: ${hexMap.fold("None")(_.toString)}
      | characterIdsOutsideMap: $characterIdsOutsideMap
      | characterIdsThatTookActionThisPhase: $characterIdsThatTookActionThisPhase
      | characterTakingActionThisTurn: $characterTakingActionThisTurn
      | phase: $phase
      | turn: $turn
      | players: $players
      | gameStatus: $gameStatus
      | currentPlayerId: ${if(players.nonEmpty) currentPlayer.id else "None"}
      |""".stripMargin
}

object GameState {
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
      hexMap = None,
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
      clockConfig = defaultClockConfig,
      clock = Clock.fromConfig(defaultClockConfig, Seq()),
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
                          hexMap: Option[HexMap],
                          players: Seq[Player],
                          characters: Set[NKMCharacterView],
                          phase: Phase,
                          turn: Turn,
                          characterIdsOutsideMap: Set[CharacterId],
                          characterIdsThatTookActionThisPhase: Set[CharacterId],
                          characterTakingActionThisTurn: Option[CharacterId],
                          playerIdsThatPlacedCharacters: Set[PlayerId],
                          clockConfig: ClockConfig,
                          clock: Clock,

                          currentPlayerId: PlayerId,
                        )
