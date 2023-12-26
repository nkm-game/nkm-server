package com.tosware.nkm.models

import com.tosware.nkm.*
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.HexCoordinates

case class GameStateValidator()(implicit gameState: GameState) {
  private def playerInGame(playerId: PlayerId) =
    gameState.players.exists(_.id == playerId)

  private def characterInGame(characterId: CharacterId) =
    gameState.characters.exists(_.id == characterId)

  private def abilityInGame(abilityId: AbilityId) =
    gameState.abilities.exists(_.id == abilityId)

  private def playerIsHost(playerId: PlayerId) =
    gameState.hostIdOpt.contains(playerId)

  private def playerFinishedGame(playerId: PlayerId) =
    !gameState.playerByIdOpt(playerId)
      .map(_.victoryStatus)
      .contains(VictoryStatus.Pending)

  private def gameStatusIs(gameStatus: GameStatus) =
    gameState.gameStatus == gameStatus

  private def banValid(playerId: PlayerId, characterIds: Set[CharacterMetadataId]) =
    gameState.draftPickStateOpt.fold(false)(_.validateBan(playerId, characterIds))

  private def draftPickChecks(playerId: PlayerId, characterMetadataId: CharacterMetadataId): Set[UseCheck] =
    gameState.draftPickStateOpt.map(_.pickChecks(playerId, characterMetadataId)).getOrElse(Set.empty[UseCheck])

  private def blindPickChecks(playerId: PlayerId, characterMetadataId: Set[CharacterMetadataId]): Set[UseCheck] =
    gameState.blindPickStateOpt.map(_.pickChecks(playerId, characterMetadataId)).getOrElse(Set.empty[UseCheck])

  private def checkCharacterPlacings(
      playerId: PlayerId,
      coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId],
  ): Set[UseCheck] = {
    val playerCharacterIds: Set[CharacterId] =
      gameState.playerByIdOpt(playerId).map(_.characterIds).getOrElse(Set.empty)
    val playerSpawnPoints: Set[HexCoordinates] =
      gameState.hexMap.getSpawnPointsByNumber(gameState.playerNumber(playerId)).map(_.coordinates)

    Set(
      UseCheck.Player.HasNotPlacedCharacters(playerId),
      (coordinatesToCharacterIdMap.values.toSet.size == coordinatesToCharacterIdMap.size) -> "Duplicated character ids in the map.",
      coordinatesToCharacterIdMap.keySet.subsetOf(playerSpawnPoints) -> "All coordinates have to be your spawnpoints.",
      (playerCharacterIds == coordinatesToCharacterIdMap.values.toSet) -> "Not all or invalid characters.",
    )
  }

  private object Message {
    val abilityCannotBeUsed = "Ability cannot be used"
    val abilityNotInGame = "Ability is not in the game."
    val banNotValid = "Ban is not valid."
    val characterAlreadyActedPhase = "Character already took action this phase."
    val characterCannotAttack = "Character is unable to basic attack."
    val characterCannotBasicMove = "Character is unable to basic move."
    val characterNotInGame = "Character is not in the game."
    val characterNotOwned = "You do not own this character."
    val characterOutsideMap = "Character is not on the map."
    val gameFinished = "Game is finished."
    val gameNotInCharacterPick = "Game is not in character pick phase."
    val gameNotInCharacterPlacing = "Game is not in character placing phase."
    val gameNotRunning = "Game is not running."
    val gameNotStarted = "Game is not started."
    val gameStarted = "Game is started."
    val noCharacterTookAction = "No character took action this turn."
    val notYourTurn = "Not your turn."
    val otherCharacterAlreadyActedThisTurn = "Other character already took action this turn."
    val playerFinishedGame = "You have already finished the game."
    val playerNotHost = "Player is not a host."
    val playerNotInGame = "Player is not in the game."
    val playerPlacedCharacters = "You have already placed characters."
    val targetInvisible = "Target character is invisible."
    val targetNotInRange = "Target character not in range or not a valid target."
  }

  private object UseCheck {
    def CharacterPlacingsChecks(
        playerId: PlayerId,
        coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId],
    ): Set[UseCheck] =
      checkCharacterPlacings(playerId, coordinatesToCharacterIdMap)

    object Player {
      def IsHost(playerId: PlayerId): UseCheck =
        playerIsHost(playerId) -> Message.playerNotHost
      def InGame(playerId: PlayerId): UseCheck =
        playerInGame(playerId) -> Message.playerNotInGame
      def HasNotFinishedGame(playerId: PlayerId): UseCheck =
        (!playerFinishedGame(playerId)) -> Message.playerFinishedGame
      def IsTheirTurn(playerId: PlayerId): UseCheck =
        (gameState.currentPlayer.id == playerId) -> Message.notYourTurn
      def HasNotPlacedCharacters(playerId: PlayerId): UseCheck =
        !gameState.playerIdsThatPlacedCharacters.contains(playerId) -> Message.playerPlacedCharacters
    }
    object Game {
      def NotStarted: UseCheck =
        gameStatusIs(GameStatus.NotStarted) -> Message.gameStarted
      def Started: UseCheck =
        (!gameStatusIs(GameStatus.NotStarted)) -> Message.gameNotStarted
      def NotFinished: UseCheck =
        (!gameStatusIs(GameStatus.Finished)) -> Message.gameFinished
      def Running: UseCheck =
        gameStatusIs(GameStatus.Running) -> Message.gameNotRunning
      def InCharacterPick: UseCheck =
        gameStatusIs(GameStatus.CharacterPick) -> Message.gameNotInCharacterPick
      def InCharacterPlacing: UseCheck =
        gameStatusIs(GameStatus.CharacterPlacing) -> Message.gameNotInCharacterPlacing
      def SomeCharacterTookAction: UseCheck =
        gameState.characterTakingActionThisTurnOpt.nonEmpty -> Message.noCharacterTookAction
    }
    object DraftPick {
      def BanValid(playerId: PlayerId, characterMetadataIds: Set[CharacterMetadataId]): UseCheck =
        banValid(playerId, characterMetadataIds) -> Message.banNotValid
      def PickChecks(playerId: PlayerId, characterMetadataId: CharacterMetadataId): Set[UseCheck] =
        draftPickChecks(playerId, characterMetadataId)
    }
    object BlindPick {
      def PickChecks(playerId: PlayerId, characterMetadataIds: Set[CharacterMetadataId]): Set[UseCheck] =
        blindPickChecks(playerId, characterMetadataIds)
    }
    object Character {
      def InGame(characterId: CharacterId): UseCheck =
        characterInGame(characterId) -> Message.characterNotInGame
      def IsOwnedBy(playerId: PlayerId, characterId: CharacterId): UseCheck =
        gameState.characterByIdOpt(characterId)
          .fold(true)(_.owner(gameState).id == playerId) -> Message.characterNotOwned
      def OtherCharacterDidNotTakeActionThisTurn(characterId: CharacterId): UseCheck =
        (!gameState.characterTakingActionThisTurnOpt.exists(_ != characterId)) ->
          Message.otherCharacterAlreadyActedThisTurn
      def DidNotActThisPhase(characterId: CharacterId): UseCheck =
        (!gameState.characterIdsThatTookActionThisPhase.contains(characterId)) -> Message.characterAlreadyActedPhase
      def IsVisible(characterId: CharacterId): UseCheck =
        !gameState.characterByIdOpt(characterId).fold(true)(_.isInvisible) -> Message.targetInvisible
      def CanAttack(characterId: CharacterId): UseCheck =
        gameState.characterByIdOpt(characterId).fold(true)(_.canBasicAttack) -> Message.characterCannotAttack
      def CanBasicMove(characterId: CharacterId): UseCheck =
        gameState.characterByIdOpt(characterId).fold(true)(_.canBasicMove) -> Message.characterCannotBasicMove
      def IsNotOutsideMap(characterId: CharacterId): UseCheck =
        (!gameState.characterIdsOutsideMap.contains(characterId)) -> Message.characterOutsideMap
      def InRangeOfBasicAttack(characterId: CharacterId, targetCoordsOpt: Option[HexCoordinates]): UseCheck =
        (for {
          targetCoords <- targetCoordsOpt
          character <- gameState.characterByIdOpt(characterId)
        } yield character.basicAttackCellCoords.contains(targetCoords)).getOrElse(false)
          -> Message.targetNotInRange
    }
    object Ability {
      def InGame(abilityId: AbilityId): UseCheck =
        abilityInGame(abilityId) -> Message.abilityNotInGame
      def IsUsable(abilityId: AbilityId): UseCheck =
        gameState.abilityByIdOpt(abilityId).fold(true)(_.isInstanceOf[Usable]) -> Message.abilityCannotBeUsed
      def UseChecks(abilityId: AbilityId, useData: UseData): Set[UseCheck] =
        gameState.abilityByIdOpt(abilityId).fold(Set.empty[UseCheck]) {
          case usable: Usable =>
            usable.useChecks(useData, gameState)
          case _ => Set.empty
        }
    }
    object MovePath {
      def IsNotTooShort(path: Seq[HexCoordinates]): UseCheck =
        (path.size >= 2) -> "Movement path is too short."
      def ExistsOnMap(path: Seq[HexCoordinates]): UseCheck =
        (path.toCells.size == path.size) -> "Not all cells exist on the map."
      def IsFreeToMove(path: Seq[HexCoordinates], forCharacterId: CharacterId): UseCheck =
        path.toCells.forall(c => c.looksFreeToPass(forCharacterId)) ->
          "Some of the cells in the path are not free to move."
      def StartsWithParentCell(path: Seq[HexCoordinates], parentCoords: Option[HexCoordinates]): UseCheck =
        (path.nonEmpty && parentCoords.contains(path.head)) -> "Path has to start with character's parent cell."
      def IsWithinSpeedRange(path: Seq[HexCoordinates], speed: Int): UseCheck =
        ((path.size - 1) <= speed) -> "You cannot move above speed range."
      def IsUnique(path: Seq[HexCoordinates]): UseCheck =
        (path.toSet.size == path.size) -> "You cannot visit several cells in one move."
    }
  }

  def validateStartGame(): CommandResponse = {
    val checks = Set(UseCheck.Game.NotStarted)
    models.UseCheck.canBeUsed(checks)
  }

  def validatePause(playerId: PlayerId): CommandResponse = {
    val checks = Set(
      UseCheck.Player.IsHost(playerId),
      UseCheck.Game.Started,
      UseCheck.Game.NotFinished,
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validateSurrender(playerId: PlayerId): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Game.Started,
      UseCheck.Player.HasNotFinishedGame(playerId),
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validateBanCharacters(playerId: PlayerId, characterIds: Set[CharacterMetadataId]): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Game.InCharacterPick,
      UseCheck.DraftPick.BanValid(playerId, characterIds),
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validatePickCharacter(playerId: PlayerId, characterMetadataId: CharacterMetadataId): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Game.InCharacterPick,
    ) ++ UseCheck.DraftPick.PickChecks(playerId, characterMetadataId)
    models.UseCheck.canBeUsed(checks)
  }

  def validateBlindPickCharacters(
      playerId: PlayerId,
      characterMetadataIds: Set[CharacterMetadataId],
  ): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Game.InCharacterPick,
    ) ++ UseCheck.BlindPick.PickChecks(playerId, characterMetadataIds)
    models.UseCheck.canBeUsed(checks)
  }

  def validatePlacingCharacters(
      playerId: PlayerId,
      coordinatesToCharacterIdMap: Map[HexCoordinates, CharacterId],
  ): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Game.InCharacterPlacing,
    ) ++ UseCheck.CharacterPlacingsChecks(playerId, coordinatesToCharacterIdMap)
    models.UseCheck.canBeUsed(checks)
  }

  def validateEndTurn(playerId: PlayerId): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Player.IsTheirTurn(playerId),
      UseCheck.Game.Running,
      UseCheck.Game.SomeCharacterTookAction,
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validatePassTurn(playerId: PlayerId, characterId: CharacterId): CommandResponse = {
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Character.InGame(characterId),
      UseCheck.Game.Running,
      UseCheck.Player.IsTheirTurn(playerId),
      UseCheck.Character.IsOwnedBy(playerId, characterId),
      UseCheck.Character.OtherCharacterDidNotTakeActionThisTurn(characterId),
      UseCheck.Character.DidNotActThisPhase(characterId),
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validateBasicMoveCharacter(
      playerId: PlayerId,
      path: Seq[HexCoordinates],
      characterId: CharacterId,
  ): CommandResponse = {
    val character = gameState.characterById(characterId)
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Character.InGame(characterId),
      UseCheck.Game.Running,
      UseCheck.Player.IsTheirTurn(playerId),
      UseCheck.Character.IsOwnedBy(playerId, characterId),
      UseCheck.Character.IsNotOutsideMap(characterId),
      UseCheck.Character.OtherCharacterDidNotTakeActionThisTurn(characterId),
      UseCheck.Character.DidNotActThisPhase(characterId),
      UseCheck.Character.CanBasicMove(characterId),
      UseCheck.MovePath.IsNotTooShort(path),
      UseCheck.MovePath.ExistsOnMap(path),
      UseCheck.MovePath.IsFreeToMove(path, characterId),
      UseCheck.MovePath.StartsWithParentCell(path, character.parentCellOpt.map(_.coordinates)),
      UseCheck.MovePath.IsWithinSpeedRange(path, character.state.speed),
      UseCheck.MovePath.IsUnique(path),
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validateBasicAttackCharacter(
      playerId: PlayerId,
      characterId: CharacterId,
      targetCharacterId: CharacterId,
  ): CommandResponse = {
    val targetCharacter = gameState.characterById(targetCharacterId)
    val targetCoords = targetCharacter.parentCellOpt.map(_.coordinates)

    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Character.InGame(characterId),
      UseCheck.Game.Running,
      UseCheck.Player.IsTheirTurn(playerId),
      UseCheck.Character.IsOwnedBy(playerId, characterId),
      UseCheck.Character.DidNotActThisPhase(characterId),
      UseCheck.Character.OtherCharacterDidNotTakeActionThisTurn(characterId),
      UseCheck.Character.IsVisible(targetCharacterId),
      UseCheck.Character.InRangeOfBasicAttack(characterId, targetCoords),
      UseCheck.Character.CanAttack(characterId),
      UseCheck.Character.IsNotOutsideMap(characterId),
      UseCheck.Character.IsNotOutsideMap(targetCharacterId),
    )
    models.UseCheck.canBeUsed(checks)
  }

  def validateAbilityUse(playerId: PlayerId, abilityId: AbilityId, useData: UseData = UseData()): CommandResponse = {
    val ability = gameState.abilityById(abilityId)
    val characterId = ability.parentCharacter.id
    val checks = Set(
      UseCheck.Player.InGame(playerId),
      UseCheck.Ability.InGame(abilityId),
      UseCheck.Game.Running,
      UseCheck.Player.IsTheirTurn(playerId),
      UseCheck.Character.IsOwnedBy(playerId, characterId),
      UseCheck.Character.DidNotActThisPhase(characterId),
      UseCheck.Character.OtherCharacterDidNotTakeActionThisTurn(characterId),
      UseCheck.Ability.IsUsable(abilityId),
    ) ++ UseCheck.Ability.UseChecks(abilityId, useData)
    models.UseCheck.canBeUsed(checks)
  }
}
