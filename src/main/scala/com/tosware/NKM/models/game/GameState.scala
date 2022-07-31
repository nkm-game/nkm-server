package com.tosware.NKM.models.game

import com.softwaremill.quicklens._
import com.tosware.NKM.models.{Damage, DamageType}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game.blindpick._
import com.tosware.NKM.models.game.draftpick._
import com.tosware.NKM.models.game.hex.{HexCell, HexCoordinates, HexMap}

import scala.util.Random

case class GameState(id: String,
                     charactersMetadata: Set[NKMCharacterMetadata],
                     hexMap: Option[HexMap],
                     characterIdsOutsideMap: Set[CharacterId],
                     characterIdsThatTookActionThisPhase: Set[CharacterId],
                     characterTakingActionThisTurn: Option[CharacterId],
                     phase: Phase,
                     turn: Turn,
                     players: Seq[Player],
                     playerIdsThatPlacedCharacters: Set[PlayerId],
                     gameStatus: GameStatus,
                     pickType: PickType,
                     numberOfBans: Int,
                     numberOfCharactersPerPlayers: Int,
                     draftPickState: Option[DraftPickState],
                     blindPickState: Option[BlindPickState],
                     clockConfig: ClockConfig,
                     clock: Clock,
                    ) {
  def host: Player = players.find(_.isHost).get

  def currentPlayerNumber: Int = turn.number % players.size

  def playerNumber(playerId: PlayerId): Int = players.indexWhere(_.id == playerId)

  def playerById(playerId: PlayerId): Option[Player] = players.find(_.id == playerId)

  def isInChampionSelect: Boolean = Seq(GameStatus.CharacterPick, GameStatus.CharacterPicked).contains(gameStatus)

  def currentPlayer: Player = players(currentPlayerNumber)

  def currentPlayerTime: Long = clock.playerTimes(currentPlayer.id)

  def characters: Set[NKMCharacter] = players.flatMap(_.characters).toSet

  def characterById(characterId: CharacterId): Option[NKMCharacter] = characters.find(_.id == characterId)

  def characterPickFinished: Boolean =  {
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

  def initializeCharacterPick(): GameState = {
    val pickTime = pickType match {
      case PickType.AllRandom => clockConfig.timeAfterPickMillis
      case PickType.DraftPick => clockConfig.maxBanTimeMillis
      case PickType.BlindPick => clockConfig.maxPickTimeMillis
    }
    copy(clock = clock.setPickTime(pickTime))
  }

  def startGame(g: GameStartDependencies): GameState = {
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
      assignCharactersToPlayers().copy(
        gameStatus = GameStatus.Running,
        playerIdsThatPlacedCharacters = players.map(_.id).toSet,
      )
    } else this
  }

  def checkVictoryStatus(): GameState = {
    def filterPendingPlayers: Player => Boolean = _.victoryStatus == VictoryStatus.Pending

    if (gameStatus == GameStatus.CharacterPick && players.count(_.victoryStatus == VictoryStatus.Lost) > 0) {
      this.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
        .setTo(VictoryStatus.Drawn)
        .modify(_.gameStatus).setTo(GameStatus.Finished)
    } else if (players.count(_.victoryStatus == VictoryStatus.Pending) == 1) {
      this.modify(_.players.eachWhere(filterPendingPlayers).victoryStatus)
        .setTo(VictoryStatus.Won)
        .modify(_.gameStatus).setTo(GameStatus.Finished)
    } else this
  }

  def checkIfBanningFinished(): GameState = {
    val banningFinished = draftPickState.fold(false)(_.pickPhase != DraftPickPhase.Banning)

    if(banningFinished) finishBanningPhase() else this
  }

  def generateCharacter(characterMetadataId: CharacterMetadataId)(implicit random: Random): NKMCharacter = {
    val characterId = java.util.UUID.nameUUIDFromBytes(random.nextBytes(16)).toString
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
    val playersWithAssignedCharacters =
      players.map(p => p.copy(characters = characterSelection(p.id).map(c => generateCharacter(c)).toSet))

    copy(
      players = playersWithAssignedCharacters,
      characterIdsOutsideMap = playersWithAssignedCharacters.flatMap(c => c.characters.map(c => c.id)).toSet
    )
  }

  def checkIfCharacterPickFinished()(implicit random: Random): GameState = {
    if(characterPickFinished) {
      copy(
        gameStatus = GameStatus.CharacterPicked,
        clock = clock.setPickTime(clockConfig.timeAfterPickMillis),
      ).assignCharactersToPlayers()
    } else this
  }

  def startPlacingCharacters()(implicit random: Random): GameState =
    this.modify(_.gameStatus).setTo(GameStatus.CharacterPlacing)
      .placeCharactersRandomlyIfAllRandom()

  def decreasePickTime(timeMillis: Long): GameState =
    copy(clock = clock.decreasePickTime(timeMillis))

  def decreaseTime(playerId: PlayerId, timeMillis: Long): GameState =
    copy(clock = clock.decreaseTime(playerId, timeMillis))

  def increaseTime(playerId: PlayerId, timeMillis: Long): GameState =
    copy(clock = clock.increaseTime(playerId, timeMillis))

  def pause(): GameState =
    copy(clock = clock.pause())

  def unpause(): GameState =
    copy(clock = clock.unpause())

  def surrender(playerIds: PlayerId*): GameState = {
    def filterPlayers: Player => Boolean = p => playerIds.contains(p.name)

    this.modify(_.players.eachWhere(filterPlayers).victoryStatus).setTo(VictoryStatus.Lost).checkVictoryStatus()
  }

  def ban(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): GameState =
    copy(draftPickState = draftPickState.map(_.ban(playerId, characterIds)))

  def finishBanningPhase(): GameState =
    copy(
      draftPickState = draftPickState.map(_.finishBanning()),
      clock = clock.setPickTime(clockConfig.maxPickTimeMillis),
    )

  def pick(playerId: PlayerId, characterId: CharacterMetadataId)(implicit random: Random): GameState =
    copy(
      draftPickState = draftPickState.map(_.pick(playerId, characterId)),
      clock = clock.setPickTime(clockConfig.maxPickTimeMillis),
    ).checkIfCharacterPickFinished()

  def draftPickTimeout(): GameState =
    surrender(draftPickState.get.currentPlayerPicking.get)

  def blindPick(playerId: PlayerId, characterIds: Set[CharacterMetadataId])(implicit random: Random): GameState =
    copy(blindPickState = blindPickState.map(_.pick(playerId, characterIds))).checkIfCharacterPickFinished()

  def blindPickTimeout(): GameState =
    surrender(blindPickState.get.pickingPlayers: _*)

  def checkIfPlacingCharactersFinished(): GameState = {
    if(placingCharactersFinished) {
      copy(gameStatus = GameStatus.Running)
    } else this
  }

  def placeCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]): GameState =
    coordinatesToCharacterIdMap.foldLeft(this){case (acc, (coordinate, characterId)) => acc.placeCharacter(coordinate, characterId)}
      .copy(playerIdsThatPlacedCharacters = playerIdsThatPlacedCharacters + playerId)
      .checkIfPlacingCharactersFinished()


  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId): GameState =
    this.modify(_.hexMap.each.cells.each).using {
      case cell if cell.coordinates == targetCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
      case cell => cell
    }.modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))

  def basicMoveCharacter(path: Seq[HexCoordinates], characterId: CharacterId): GameState = {
    val newGameState = takeActionWithCharacter(characterId)
    path.tail.foldLeft(newGameState){case (acc, coordinate) => acc.moveCharacterSingle(coordinate, characterId)}
  }

  def moveCharacterSingle(parentCellCoordinates: HexCoordinates, characterId: CharacterId): GameState = {
    val parentCell = hexMap.get.cells.find(_.characterId.contains(characterId)).getOrElse {
      // case if character dies on the way?
      // TODO      log.error(s"Unable to move character $characterId to $parentCellCoordinates")
      return this
    }
    this.modify(_.hexMap.each.cells.each).using {
      case cell if cell == parentCell => HexCell(cell.coordinates, cell.cellType, None, cell.effects, cell.spawnNumber)
      case cell if cell.coordinates == parentCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
      case cell => cell
    }
  }

  def basicAttack(attackingCharacterId: CharacterId, targetCharacterId: CharacterId) = {
    val newGameState = takeActionWithCharacter(attackingCharacterId)
    val attackingCharacter = characterById(attackingCharacterId).get

    newGameState.modify(_.players.each.characters.each).using {
      case character if character.id == targetCharacterId =>
        character.receiveDamage(Damage(attackingCharacterId, DamageType.Physical, attackingCharacter.state.attackPoints))
      case character => character
    }
  }

  def setMap(hexMap: HexMap): GameState =
    copy(hexMap = Some(hexMap))

  def addEffect(characterId: CharacterId, characterEffect: CharacterEffect): GameState =
    this.modify(_.players.each.characters.each).using {
      case character if character.id == characterId => character.addEffect(characterEffect)
      case character => character
    }

  def removeCharacterFromMap(characterId: CharacterId): GameState = {
    this.modify(_.hexMap.each.cells.each).using {
      case cell if cell.characterId.contains(characterId) => HexCell(cell.coordinates, cell.cellType, None, cell.effects, cell.spawnNumber)
      case cell => cell
    }.modify(_.characterIdsOutsideMap).setTo(characterIdsOutsideMap + characterId)
  }

  def takeActionWithCharacter(characterId: CharacterId): GameState = this.modify(_.characterTakingActionThisTurn).setTo(Some(characterId))

  def endTurn(): GameState =
    this.modify(_.characterIdsThatTookActionThisPhase).using(c => c + characterTakingActionThisTurn.get)
      .modify(_.characterTakingActionThisTurn).setTo(None)
      .modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))
      .finishPhaseIfEveryCharacterTookAction()

  def passTurn(characterId: CharacterId): GameState =
    takeActionWithCharacter(characterId).endTurn()

  def finishPhase(): GameState =
    this.modify(_.characterIdsThatTookActionThisPhase).setTo(Set.empty)
      .modify(_.phase).using(oldPhase => Phase(oldPhase.number + 1))

  def finishPhaseIfEveryCharacterTookAction(): GameState =
    if(characterIdsThatTookActionThisPhase == characters.map(_.id))
      this.finishPhase()
    else this

  def toView(forPlayer: Option[PlayerId]): GameStateView =
    GameStateView(
      id,
      charactersMetadata,
      hexMap,
      characterIdsOutsideMap,
      characterIdsThatTookActionThisPhase,
      characterTakingActionThisTurn,
      phase,
      turn,
      players.map(_.toView),
      playerIdsThatPlacedCharacters,
      gameStatus,
      pickType,
      numberOfBans,
      numberOfCharactersPerPlayers,
      draftPickState.map(_.toView(forPlayer)),
      blindPickState.map(_.toView(forPlayer)),
      clockConfig,
      clock,
      currentPlayer.id,
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
      hexMap = None,
      characterIdsOutsideMap = Set(),
      characterIdsThatTookActionThisPhase = Set(),
      characterTakingActionThisTurn = None,
      phase = Phase(0),
      turn = Turn(0),
      players = Seq(),
      playerIdsThatPlacedCharacters = Set(),
      gameStatus = GameStatus.NotStarted,
      pickType = defaultPickType,
      numberOfBans = 0,
      numberOfCharactersPerPlayers = 1,
      draftPickState = None,
      blindPickState = None,
      clockConfig = defaultClockConfig,
      clock = Clock.fromConfig(defaultClockConfig, Seq()),
    )
  }
}

case class GameStateView(
                          id: String,
                          charactersMetadata: Set[NKMCharacterMetadata],
                          hexMap: Option[HexMap],
                          characterIdsOutsideMap: Set[CharacterId],
                          characterIdsThatTookActionThisPhase: Set[CharacterId],
                          characterTakingActionThisTurn: Option[CharacterId],
                          phase: Phase,
                          turn: Turn,
                          players: Seq[PlayerView],
                          playerIdsThatPlacedCharacters: Set[PlayerId],
                          gameStatus: GameStatus,
                          pickType: PickType,
                          numberOfBans: Int,
                          numberOfCharactersPerPlayers: Int,
                          draftPickState: Option[DraftPickStateView],
                          blindPickState: Option[BlindPickStateView],
                          clockConfig: ClockConfig,
                          clock: Clock,
                          currentPlayerId: PlayerId,
                        )
