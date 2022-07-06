package com.tosware.NKM.models

import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._

case class GameStateValidator(gameState: GameState) {
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

  private val playerNotHostMessage = "Player is not a host."
  private val gameStartedMessage = "Game is started."
  private val gameNotStartedMessage = "Game is not started."
  private val playerNotInGameMessage = "This player is not in the game."
  private val playerFinishedGameMessage = "This player already finished the game."
  private val gameNotInCharacterPickMessage = "Game is not in character pick phase."
  private val gameNotInCharacterPlacingMessage = "Game is not in character placing phase."
  private val banNotValidMessage = "Ban is not valid."
  private val pickNotValidMessage = "Pick is not valid."

  def validateStartGame(): CommandResponse = {
    if (!gameStatusIs(GameStatus.NotStarted)) Failure(gameStartedMessage)
    else Success()
  }

  def validatePause(playerId: PlayerId): CommandResponse = {
    if (!playerIsHost(playerId)) Failure(playerNotHostMessage)
    Success()
  }

  def validateSurrender(playerId: PlayerId): CommandResponse = {
    if (!playerInGame(playerId)) Failure(playerNotInGameMessage)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(gameNotStartedMessage)
    else if (playerFinishedGame(playerId)) Failure(playerFinishedGameMessage)
    else Success()
  }

  def validateBanCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse = {
    if (!playerInGame(playerId)) Failure(playerNotInGameMessage)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(gameNotStartedMessage)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(gameNotInCharacterPickMessage)
    else if (!banValid(playerId, characterIds)) Failure(banNotValidMessage)
    else Success()
  }

  def validatePickCharacter(playerId: PlayerId, characterId: CharacterMetadataId): CommandResponse = {
    if (!playerInGame(playerId)) Failure(playerNotInGameMessage)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(gameNotStartedMessage)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(gameNotInCharacterPickMessage)
    else if (!characterPickValid(playerId, characterId)) Failure(pickNotValidMessage)
    else Success()
  }

  def validateBlindPickCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse = {
    if (!playerInGame(playerId)) Failure(playerNotInGameMessage)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(gameNotStartedMessage)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(gameNotInCharacterPickMessage)
    else if (!characterBlindPickValid(playerId, characterIds)) Failure(pickNotValidMessage)
    else Success()
  }

  def validatePlacingCharacters(playerId: PlayerId, coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId]): CommandResponse = {
    if (!playerInGame(playerId)) Failure(playerNotInGameMessage)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(gameNotStartedMessage)
    else if (!gameStatusIs(GameStatus.CharacterPlacing)) Failure(gameNotInCharacterPlacingMessage)
    else charactersPlacingValid(playerId, coordinatesToCharacterIdMap) match {
      case (true, _) => Success()
      case (false, msg) => Failure(msg)
    }
  }
}
