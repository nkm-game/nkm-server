package com.tosware.NKM.models.game

import com.softwaremill.quicklens._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game.blindpick._
import com.tosware.NKM.models.game.draftpick._

import scala.util.Random

case class GameState(id: String,
                     charactersMetadata: Set[NKMCharacterMetadata],
                     hexMap: Option[HexMap],
                     characterIdsOutsideMap: Seq[CharacterId],
                     phase: Phase,
                     turn: Turn,
                     players: Seq[Player],
                     gameStatus: GameStatus,
                     pickType: PickType,
                     numberOfBans: Int,
                     numberOfCharactersPerPlayers: Int,
                     draftPickState: Option[DraftPickState],
                     blindPickState: Option[BlindPickState],
                     clock: Clock,
                    ) {
  def getCurrentPlayerNumber: Int = turn.number % players.length

  def getCurrentPlayer: Player = players(getCurrentPlayerNumber)

  def startGame(g: GameStartDependencies): GameState = {
    copy(
      charactersMetadata = g.charactersMetadata,
      players = g.players,
      hexMap = Some(g.hexMap),
      pickType = g.pickType,
      numberOfBans = g.numberOfBansPerPlayer,
      numberOfCharactersPerPlayers = g.numberOfCharactersPerPlayer,
      gameStatus = if(g.pickType == PickType.AllRandom) GameStatus.CharacterPicked else GameStatus.CharacterPick,
      draftPickState = if (g.pickType == PickType.DraftPick) Some(DraftPickState.empty(DraftPickConfig.generate(g))) else None,
      blindPickState = if (g.pickType == PickType.BlindPick) Some(BlindPickState.empty(BlindPickConfig.generate(g))) else None,
      clock = Clock.fromConfig(g.clockConfig),
    )
  }

  def placeCharactersRandomlyIfAllRandom(charactersMetadata: Set[NKMCharacterMetadata])(implicit random: Random): GameState = {
    if (pickType == PickType.AllRandom) {
      val pickedCharacters = random.shuffle(charactersMetadata).grouped(numberOfCharactersPerPlayers).take(players.length)
      val playersWithAssignedCharacters = players.zip(pickedCharacters).map(x => {
        val (player, characters) = x
        player.copy(characters = characters.map(c => NKMCharacter.fromMetadata(java.util.UUID.nameUUIDFromBytes(random.nextBytes(16)).toString, c)).toList)
      })
      copy(gameStatus = GameStatus.Running, players = playersWithAssignedCharacters, characterIdsOutsideMap = playersWithAssignedCharacters.flatMap(c => c.characters.map(c => c.id)))
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

  def checkIfCharacterPickFinished(): GameState = {
    val draftPickFinished = draftPickState.fold(false)(_.pickPhase == DraftPickPhase.Finished)
    val blindPickFinished = blindPickState.fold(false)(_.pickPhase == BlindPickPhase.Finished)

    if(draftPickFinished || blindPickFinished) {
      this.modify(_.gameStatus).setTo(GameStatus.CharacterPicked)
    } else this
  }

  def startPlacingCharacters()(implicit random: Random): GameState = {
    this.modify(_.gameStatus).setTo(GameStatus.CharacterPlacing).placeCharactersRandomlyIfAllRandom(charactersMetadata)
  }

  def surrender(playerIds: PlayerId*): GameState = {
    def filterPlayers: Player => Boolean = p => playerIds.contains(p.name)

    this.modify(_.players.eachWhere(filterPlayers).victoryStatus).setTo(VictoryStatus.Lost).checkVictoryStatus()
  }

  def ban(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): GameState =
    copy(draftPickState = draftPickState.map(_.ban(playerId, characterIds)))

  def finishBanningPhase(): GameState =
    copy(draftPickState = draftPickState.map(_.finishBanning()))

  def pick(playerId: PlayerId, characterId: CharacterMetadataId): GameState =
    copy(draftPickState = draftPickState.map(_.pick(playerId, characterId))).checkIfCharacterPickFinished()

  def draftPickTimeout(): GameState =
    surrender(draftPickState.get.currentPlayerPicking.get)

  def blindPick(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): GameState =
    copy(blindPickState = blindPickState.map(_.pick(playerId, characterIds))).checkIfCharacterPickFinished()

  def blindPickTimeout(): GameState =
    surrender(blindPickState.get.pickingPlayers: _*)

  def placeCharacter(targetCellCoordinates: HexCoordinates, characterId: CharacterId): GameState =
    this.modify(_.hexMap.each.cells.each).using {
      case cell if cell.coordinates == targetCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
      case cell => cell
    }.modify(_.characterIdsOutsideMap).using(_.filter(_ != characterId))
      .modify(_.turn).using(oldTurn => Turn(oldTurn.number + 1))

  def moveCharacter(parentCellCoordinates: HexCoordinates, characterId: String): GameState = {
    val parentCell = hexMap.get.cells.find(_.characterId.contains(characterId)).getOrElse {
      // TODO      log.error(s"Unable to move character $characterId to $parentCellCoordinates")
      return this
    }
    this.modify(_.hexMap.each.cells.each).using {
      case cell if cell == parentCell => HexCell(cell.coordinates, cell.cellType, None, cell.effects, cell.spawnNumber)
      case cell if cell.coordinates == parentCellCoordinates => HexCell(cell.coordinates, cell.cellType, Some(characterId), cell.effects, cell.spawnNumber)
      case cell => cell
    }
  }

  def setMap(hexMap: HexMap): GameState =
    copy(hexMap = Some(hexMap))

  def toView(forPlayer: Option[PlayerId]): GameStateView =
    GameStateView(
      id,
      charactersMetadata,
      hexMap,
      characterIdsOutsideMap,
      phase,
      turn,
      players,
      gameStatus,
      pickType,
      numberOfBans,
      numberOfCharactersPerPlayers,
      draftPickState.map(_.toView(forPlayer)),
      blindPickState.map(_.toView(forPlayer)),
      clock,
      getCurrentPlayer.name,
    )
}

object GameState {
  def empty(id: String): GameState = GameState(
    id = id,
    charactersMetadata = Set(),
    hexMap = None,
    characterIdsOutsideMap = Seq(),
    phase = Phase(0),
    turn = Turn(0),
    players = Seq(),
    gameStatus = GameStatus.NotStarted,
    pickType = PickType.AllRandom,
    numberOfBans = 0,
    numberOfCharactersPerPlayers = 1,
    draftPickState = None,
    blindPickState = None,
    clock = Clock.empty(),
  )
}

case class GameStateView(
                          id: String,
                          charactersMetadata: Set[NKMCharacterMetadata],
                          hexMap: Option[HexMap],
                          characterIdsOutsideMap: Seq[CharacterId],
                          phase: Phase,
                          turn: Turn,
                          players: Seq[Player],
                          gameStatus: GameStatus,
                          pickType: PickType,
                          numberOfBans: Int,
                          numberOfCharactersPerPlayers: Int,
                          draftPickState: Option[DraftPickStateView],
                          blindPickState: Option[BlindPickStateView],
                          clock: Clock,
                          currentPlayerId: PlayerId,
                        )
