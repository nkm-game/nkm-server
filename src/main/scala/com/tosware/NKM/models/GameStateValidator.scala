package com.tosware.NKM.models

import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._

case class GameStateValidator()(implicit gameState: GameState) {
  private def playerInGame(playerId: PlayerId) =
    gameState.players.exists(_.name == playerId)

  private def playerIsHost(playerId: PlayerId) =
    gameState.host.name == playerId

  private def playerFinishedGame(playerId: PlayerId) =
    gameState.players.find(_.name == playerId).get.victoryStatus != VictoryStatus.Pending

  private def gameStatusIs(gameStatus: GameStatus) =
    gameState.gameStatus == gameStatus

  private def banValid(playerId: PlayerId, characterIds: Set[CharacterMetadataId]) =
    gameState.draftPickState.fold(false)(_.validateBan(playerId, characterIds))

  private def characterPickValid(playerId: PlayerId, characterId: CharacterMetadataId) =
    gameState.draftPickState.fold(false)(_.validatePick(playerId, characterId))

  private def characterBlindPickValid(playerId: PlayerId, characterIds: Set[CharacterMetadataId]) =
    gameState.blindPickState.fold(false)(_.validatePick(playerId, characterIds))

  private def charactersPlacingValid(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]): (Boolean, String) = {
    if(gameState.playerIdsThatPlacedCharacters.contains(playerId)) return (false, "You have already placed characters.")
    if(gameState.hexMap.isEmpty) return (false, "HexMap is empty.")
    if(coordinatesToCharacterIdMap.values.toSet.size != coordinatesToCharacterIdMap.size) return (false, "Duplicated character ids in the map.")

    val playerCharacterIds: Set[CharacterId] = gameState.playerById(playerId).get.characters.map(_.id)
    val playerSpawnPoints: Set[HexCoordinates] = gameState.hexMap.get.getSpawnPointsByNumber(gameState.playerNumber(playerId)).map(_.coordinates)

    if(!coordinatesToCharacterIdMap.keySet.subsetOf(playerSpawnPoints)) return (false, "All coordinates have to be your spawnpoints.")
    if(playerCharacterIds != coordinatesToCharacterIdMap.values.toSet) return (false, "Not all or invalid characters.")
    (true, "")
  }

  private object Message {
    val playerNotHost = "Player is not a host."
    val gameStarted = "Game is started."
    val gameNotStarted = "Game is not started."
    val playerNotInGame = "This player is not in the game."
    val playerFinishedGame = "This player already finished the game."
    val gameNotInCharacterPick = "Game is not in character pick phase."
    val gameNotInCharacterPlacing = "Game is not in character placing phase."
    val banNotValid = "Ban is not valid."
    val pickNotValid = "Pick is not valid."
    val notYourTurn = "This is not your turn."
  }

  def validateStartGame(): CommandResponse = {
    if (!gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameStarted)
    else Success()
  }

  def validatePause(playerId: PlayerId): CommandResponse = {
    if (!playerIsHost(playerId)) Failure(Message.playerNotHost)
    Success()
  }

  def validateSurrender(playerId: PlayerId): CommandResponse = {
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (playerFinishedGame(playerId)) Failure(Message.playerFinishedGame)
    else Success()
  }

  def validateBanCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse = {
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(Message.gameNotInCharacterPick)
    else if (!banValid(playerId, characterIds)) Failure(Message.banNotValid)
    else Success()
  }

  def validatePickCharacter(playerId: PlayerId, characterId: CharacterMetadataId): CommandResponse = {
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(Message.gameNotInCharacterPick)
    else if (!characterPickValid(playerId, characterId)) Failure(Message.pickNotValid)
    else Success()
  }

  def validateBlindPickCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse = {
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(Message.gameNotInCharacterPick)
    else if (!characterBlindPickValid(playerId, characterIds)) Failure(Message.pickNotValid)
    else Success()
  }

  def validatePlacingCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]): CommandResponse = {
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPlacing)) Failure(Message.gameNotInCharacterPlacing)
    else charactersPlacingValid(playerId, coordinatesToCharacterIdMap) match {
      case (true, _) => Success()
      case (false, msg) => Failure(msg)
    }
  }

  def validateBasicMoveCharacter(playerId: PlayerId, path: Seq[HexCoordinates], characterId: CharacterId): CommandResponse = {
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else if (path.size < 2) Failure("Empty moves are disallowed.")
    else {
      val character = gameState.characterById(characterId).get
      val parentCell = character.parentCell
      if (!gameState.playerById(playerId).get.characterIds.contains(characterId)) Failure("You do not own this character.")
      else if (gameState.characterIdsOutsideMap.contains(characterId)) Failure("Character outside map.")
      else if (!parentCell.map(_.coordinates).contains(path.head)) Failure("Path has to start with characters parent cell.")
      else if (path.size - 1 > character.state.speed) Failure("You cannot move above speed range.")
      else if (path.toSet.size != path.size) Failure("You cannot visit several cells in one move.")
      else if (character.canBasicMove) Failure("This character cannot move.")
      else Success()
    }
  }

}
