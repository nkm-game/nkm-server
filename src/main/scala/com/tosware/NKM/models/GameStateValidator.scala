package com.tosware.NKM.models

import com.tosware.NKM.models.CommandResponse._
import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId
import com.tosware.NKM.models.game._

case class GameStateValidator(gameState: GameState) {
  private def playerInGame(playerId: PlayerId) =
    gameState.players.exists(_.name == playerId)

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

  private val gameStartedMessage = "Game is started."
  private val gameNotStartedMessage = "Game is not started."
  private val playerNotInGameMessage = "This player is not in the game."
  private val playerFinishedGameMessage = "This player already finished the game."
  private val gameNotInCharacterPickMessage = "Game is not in character pick phase."
  private val banNotValidMessage = "Ban is not valid."
  private val pickNotValidMessage = "Pick is not valid."

  def validateStartGame(): CommandResponse = {
    if (!gameStatusIs(GameStatus.NotStarted)) Failure(gameStartedMessage)
    else Success()
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
}
