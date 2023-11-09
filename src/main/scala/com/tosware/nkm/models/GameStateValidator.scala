package com.tosware.nkm.models

import com.tosware.nkm.*
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

case class GameStateValidator()(implicit gameState: GameState) {
  private def playerInGame(playerId: PlayerId) =
    gameState.players.exists(_.id == playerId)

  private def characterInGame(characterId: CharacterId) =
    gameState.characters.exists(_.id == characterId)

  private def abilityInGame(abilityId: AbilityId) =
    gameState.abilities.exists(_.id == abilityId)

  private def playerIsHost(playerId: PlayerId) =
    gameState.hostId == playerId

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

  private def charactersPlacingValid(
      playerId: PlayerId,
      coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId],
  ): (Boolean, String) = {
    if (gameState.playerIdsThatPlacedCharacters.contains(playerId))
      return (false, "You have already placed characters.")
    if (coordinatesToCharacterIdMap.values.toSet.size != coordinatesToCharacterIdMap.size)
      return (false, "Duplicated character ids in the map.")

    val playerCharacterIds: Set[CharacterId] = gameState.playerByIdOpt(playerId).get.characterIds
    val playerSpawnPoints: Set[HexCoordinates] =
      gameState.hexMap.getSpawnPointsByNumber(gameState.playerNumber(playerId)).map(_.coordinates)

    if (!coordinatesToCharacterIdMap.keySet.subsetOf(playerSpawnPoints))
      return (false, "All coordinates have to be your spawnpoints.")
    if (playerCharacterIds != coordinatesToCharacterIdMap.values.toSet) return (false, "Not all or invalid characters.")
    (true, "")
  }

  private object Message {
    val playerNotHost = "Player is not a host."
    val gameStarted = "Game is started."
    val gameFinished = "Game is finished."
    val gameNotStarted = "Game is not started."
    val gameNotRunning = "Game is not running."
    val playerNotInGame = "Player is not in the game."
    val characterNotInGame = "This character is not in the game."
    val abilityNotInGame = "This ability is not in the game."
    val playerFinishedGame = "This player already finished the game."
    val gameNotInCharacterPick = "Game is not in character pick phase."
    val gameNotInCharacterPlacing = "Game is not in character placing phase."
    val banNotValid = "Ban is not valid."
    val pickNotValid = "Pick is not valid."
    val notYourTurn = "This is not your turn."
  }

  def validateStartGame(): CommandResponse =
    if (!gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameStarted)
    else Success()

  def validatePause(playerId: PlayerId): CommandResponse =
    if (!playerIsHost(playerId)) Failure(Message.playerNotHost)
    else if (gameStatusIs(GameStatus.Finished)) Failure(Message.gameFinished)
    else Success()

  def validateSurrender(playerId: PlayerId): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (playerFinishedGame(playerId)) Failure(Message.playerFinishedGame)
    else Success()

  def validateBanCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(Message.gameNotInCharacterPick)
    else if (!banValid(playerId, characterIds)) Failure(Message.banNotValid)
    else Success()

  def validatePickCharacter(playerId: PlayerId, characterId: CharacterMetadataId): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(Message.gameNotInCharacterPick)
    else if (!characterPickValid(playerId, characterId)) Failure(Message.pickNotValid)
    else Success()

  def validateBlindPickCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPick)) Failure(Message.gameNotInCharacterPick)
    else if (!characterBlindPickValid(playerId, characterIds)) Failure(Message.pickNotValid)
    else Success()

  def validatePlacingCharacters(
      playerId: PlayerId,
      coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId],
  ): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.CharacterPlacing)) Failure(Message.gameNotInCharacterPlacing)
    else charactersPlacingValid(playerId, coordinatesToCharacterIdMap) match {
      case (true, _)    => Success()
      case (false, msg) => Failure(msg)
    }

  def validateEndTurn(playerId: PlayerId): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else if (gameState.characterTakingActionThisTurn.isEmpty) Failure("No character took action this turn.")
    else Success()

  def validatePassTurn(playerId: PlayerId, characterId: CharacterId): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (!characterInGame(characterId)) Failure(Message.characterNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else if (!gameState.playerByIdOpt(playerId).get.characterIds.contains(characterId))
      Failure("You do not own this character.")
    else if (gameState.characterTakingActionThisTurn.nonEmpty) Failure("Some character already took action this turn.")
    else if (gameState.characterIdsThatTookActionThisPhase.contains(characterId))
      Failure("This character already took action this phase.")
    else Success()

  def validateBasicMoveCharacter(
      playerId: PlayerId,
      path: Seq[HexCoordinates],
      characterId: CharacterId,
  ): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (!characterInGame(characterId)) Failure(Message.characterNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else if (path.size < 2) Failure("Empty moves are disallowed.")
    else {
      val character = gameState.characterById(characterId)
      val parentCell = character.parentCell
      val pathCells = path.toCells
      if (pathCells.size != path.size) Failure("Not all cells exist on the map.")
      else if (pathCells.exists(c => !c.looksFreeToPass(characterId)))
        Failure("Some of the cells in the path are not free to move.")
      else if (!gameState.playerByIdOpt(playerId).get.characterIds.contains(characterId))
        Failure("You do not own this character.")
      else if (gameState.characterTakingActionThisTurn.fold(false)(_ != characterId))
        Failure("Other character already took action this turn.")
      else if (gameState.characterIdsThatTookActionThisPhase.contains(characterId))
        Failure("This character already took action this phase.")
      else if (!character.canBasicMove) Failure("This character is unable to basic move.")
      else if (gameState.characterIdsOutsideMap.contains(characterId)) Failure("Character outside map.")
      else if (!parentCell.map(_.coordinates).contains(path.head))
        Failure("Path has to start with characters parent cell.")
      else if (path.size - 1 > character.state.speed) Failure("You cannot move above speed range.")
      else if (path.toSet.size != path.size) Failure("You cannot visit several cells in one move.")
      else Success()
    }

  def validateBasicAttackCharacter(
      playerId: PlayerId,
      characterId: CharacterId,
      targetCharacterId: CharacterId,
  ): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (!characterInGame(characterId)) Failure(Message.characterNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else {
      val character = gameState.characterById(characterId)
      val targetCharacter = gameState.characterById(targetCharacterId)
      val targetParentCell = targetCharacter.parentCell
      if (!gameState.playerByIdOpt(playerId).get.characterIds.contains(characterId))
        Failure("You do not own this character.")
      else if (targetCharacter.isInvisible)
        Failure("Target character is invisible.")
      else if (gameState.characterTakingActionThisTurn.fold(false)(_ != characterId))
        Failure("Other character already took action this turn.")
      else if (gameState.characterIdsThatTookActionThisPhase.contains(characterId))
        Failure("This character already took action this phase.")
      else if (!character.canBasicAttack) Failure("This character is unable to basic attack.")
      else if (gameState.characterIdsOutsideMap.contains(characterId)) Failure("Character outside map.")
      else if (gameState.characterIdsOutsideMap.contains(targetCharacterId)) Failure("Target character outside map.")
      else if (!character.basicAttackCellCoords.contains(targetParentCell.get.coordinates))
        Failure("Target character not in range or not a valid target.")
      else Success()
    }

  def validateAbilityUse(playerId: PlayerId, abilityId: AbilityId, useData: UseData = UseData()): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (!abilityInGame(abilityId)) Failure(Message.abilityNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else {
      val ability = gameState.abilityById(abilityId).asInstanceOf[Ability & Usable]
      val character = ability.parentCharacter
      if (!gameState.playerByIdOpt(playerId).get.characterIds.contains(character.id))
        Failure("You do not own this character.")
      else if (gameState.characterTakingActionThisTurn.fold(false)(_ != character.id))
        Failure("Other character already took action this turn.")
      else if (gameState.characterIdsThatTookActionThisPhase.contains(character.id))
        Failure("This character already took action this phase.")
      else ability.canBeUsed(useData, gameState)
    }

  def validateAbilityUseOnCharacter(
      playerId: PlayerId,
      abilityId: AbilityId,
      target: CharacterId,
      useData: UseData = UseData(),
  ): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (!abilityInGame(abilityId)) Failure(Message.abilityNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else {
      val ability = gameState.abilityById(abilityId).asInstanceOf[Ability & UsableOnCharacter]
      val character = ability.parentCharacter
      if (!gameState.playerByIdOpt(playerId).get.characterIds.contains(character.id))
        Failure("You do not own this character.")
      else if (gameState.characterTakingActionThisTurn.fold(false)(_ != character.id))
        Failure("Other character already took action this turn.")
      else if (gameState.characterIdsThatTookActionThisPhase.contains(character.id))
        Failure("This character already took action this phase.")
      else ability.canBeUsed(target, useData, gameState)
    }

  def validateAbilityUseOnCoordinates(
      playerId: PlayerId,
      abilityId: AbilityId,
      target: HexCoordinates,
      useData: UseData = UseData(),
  ): CommandResponse =
    if (!playerInGame(playerId)) Failure(Message.playerNotInGame)
    else if (!abilityInGame(abilityId)) Failure(Message.abilityNotInGame)
    else if (gameStatusIs(GameStatus.NotStarted)) Failure(Message.gameNotStarted)
    else if (!gameStatusIs(GameStatus.Running)) Failure(Message.gameNotRunning)
    else if (gameState.currentPlayer.id != playerId) Failure(Message.notYourTurn)
    else {
      val ability = gameState.abilityById(abilityId).asInstanceOf[Ability & UsableOnCoordinates]
      val character = ability.parentCharacter
      if (!gameState.playerByIdOpt(playerId).get.characterIds.contains(character.id))
        Failure("You do not own this character.")
      else if (gameState.characterTakingActionThisTurn.fold(false)(_ != character.id))
        Failure("Other character already took action this turn.")
      else if (gameState.characterIdsThatTookActionThisPhase.contains(character.id))
        Failure("This character already took action this phase.")
      else ability.canBeUsed(target, useData, gameState)
    }
}
