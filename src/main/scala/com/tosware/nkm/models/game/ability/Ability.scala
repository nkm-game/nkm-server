package com.tosware.nkm.models.game.ability

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.CommandResponse.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.effects.{AbilityEnchant, FreeAbility}
import com.tosware.nkm.models.game.hex.{HexCell, HexCoordinates}
import com.tosware.nkm.serializers.NkmJsonProtocol

import scala.util.{Random, Try}

abstract class Ability(val id: AbilityId)
    extends NkmJsonProtocol {
  val metadata: AbilityMetadata

  def baseUseChecks(implicit gameState: GameState): Set[UseCheck] =
    Set(
      UseCheck.Base.IsNotPassive,
      UseCheck.Base.IsNotOnCooldown,
      UseCheck.Base.ParentCharacterOnMap,
      UseCheck.Base.ParentCharacterNotGroundedIfHasMoveTrait,
      UseCheck.Base.CanBeUsedByParent,
    ) ++
      Option.when(metadata.abilityType == AbilityType.Ultimate)(UseCheck.Base.PhaseIsGreaterThan(3)).toSet

  def characterBaseUseChecks(characterId: CharacterId)(implicit gameState: GameState): Set[UseCheck] =
    Set(UseCheck.Character.InRange(characterId))
  def coordinatesBaseUseChecks(hexCoordinates: HexCoordinates)(implicit gameState: GameState): Set[UseCheck] =
    Set(UseCheck.Coordinates.ExistsOnMap(hexCoordinates), UseCheck.Coordinates.InRange(hexCoordinates))
  def state(implicit gameState: GameState): AbilityState =
    gameState.abilityStates(id)
  def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] = Set.empty
  def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] = Set.empty
  def parentCharacter(implicit gameState: GameState): NkmCharacter =
    gameState.characters.find(_.state.abilities.map(_.id).contains(id)).get
  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCellOpt
  def isEnchanted(implicit gameState: GameState): Boolean =
    parentCharacter.state.effects.ofType[AbilityEnchant].exists(_.abilityType == metadata.abilityType)
  def getCooldownState(implicit gameState: GameState): AbilityState =
    state.copy(cooldown = metadata.variables("cooldown"))
  def getDecrementCooldownState(amount: Int = 1)(implicit gameState: GameState): AbilityState =
    state.copy(cooldown = math.max(state.cooldown - amount, 0))
  def getEnabledChangedState(newEnabled: Boolean)(implicit gameState: GameState): AbilityState =
    state.copy(isEnabled = newEnabled)
  def getVariablesChangedState(key: String, value: String)(implicit gameState: GameState): AbilityState =
    state.modify(_.variables).using(_.updated(key, value))
  def hitAndDamageCharacter(target: CharacterId, damage: Damage)(implicit
      random: Random,
      gameState: GameState,
  ): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, damage)(random, id)

  def toView(forPlayer: Option[PlayerId])(implicit gameState: GameState): Option[AbilityView] =
    if (!forPlayer.exists(parentCharacter.isSeenBy)) None
    else {

      val canBeUsedResponse = models.UseCheck.canBeUsed(baseUseChecks)
      val canBeUsed = canBeUsedResponse match {
        case Success(_) => true
        case Failure(_) => false
      }
      val canBeUsedFailureMessage = canBeUsedResponse match {
        case Success(_)   => None
        case Failure(msg) => Some(msg)
      }

      Some(AbilityView(
        id = id,
        metadataId = metadata.id,
        parentCharacterId = parentCharacter.id,
        state = state,
        rangeCellCoords = Try(rangeCellCoords).getOrElse(Set.empty),
        targetsInRange = Try(targetsInRange).getOrElse(Set.empty),
        canBeUsed = canBeUsed,
        canBeUsedFailureMessage = canBeUsedFailureMessage,
      ))
    }

  object UseCheck {
    object Base {
      def IsNotPassive: UseCheck =
        (metadata.abilityType != AbilityType.Passive) -> s"Cannot use passive abilities."
      def IsNotOnCooldown(implicit gameState: GameState): UseCheck =
        (state.cooldown <= 0 || parentCharacter.state.effects.ofType[FreeAbility].nonEmpty) -> "Ability is on cooldown."
      def ParentCharacterOnMap(implicit gameState: GameState): UseCheck =
        parentCharacter.isOnMap -> "Parent character is not on map."
      def ParentCharacterNotGroundedIfHasMoveTrait(implicit gameState: GameState): UseCheck =
        !(metadata.traits.contains(AbilityTrait.Move) && parentCharacter.isGrounded) -> "Parent character is grounded."
      def PhaseIsGreaterThan(i: Int)(implicit gameState: GameState): UseCheck =
        (gameState.phase.number > i || parentCharacter.state.effects.ofType[
          FreeAbility
        ].nonEmpty) -> s"Phase is not greater than $i."
      def CanBeUsedByParent(implicit gameState: GameState): UseCheck =
        (
          parentCharacter.canUseAbilityOfType(metadata.abilityType) ||
            parentCharacter.state.effects.ofType[effects.AbilityUnlock].nonEmpty
        ) -> s"Ability cannot be used by parent character."
    }
    object Character {
      def InRange(target: CharacterId)(implicit gameState: GameState): UseCheck = {
        val targetCoords = gameState.hexMap.getCellOfCharacter(target).map(_.coordinates.toTuple).getOrElse("null")
        targetsInRange.toCells.exists(
          _.characterId.contains(target)
        ) -> s"Target character is not in range (target coords: $targetCoords)."
      }
      def IsEnemy(target: CharacterId)(implicit gameState: GameState): UseCheck =
        gameState.characterById(target).isEnemyForC(parentCharacter.id) -> "Target character is not an enemy."
      def IsFriend(target: CharacterId)(implicit gameState: GameState): UseCheck =
        gameState.characterById(target).isFriendForC(parentCharacter.id) -> "Target character is not a friend."
      def IsOnMap(target: CharacterId)(implicit gameState: GameState): UseCheck =
        gameState.characterById(target).isOnMap -> "Target character is not on map."
      def IsDead(target: CharacterId)(implicit gameState: GameState): UseCheck =
        gameState.characterById(target).isDead -> "Target character is not dead."
    }

    object Coordinates {
      def ExistsOnMap(target: HexCoordinates)(implicit gameState: GameState): UseCheck =
        Seq(target).toCells.nonEmpty -> s"Target does not exist on the map. ($target)"
      def InRange(target: HexCoordinates)(implicit gameState: GameState): UseCheck =
        targetsInRange.contains(target) -> s"Target is not in range. ($target)"
      def InRangeOf(rangeCells: Set[HexCoordinates], target: HexCoordinates)(implicit gameState: GameState): UseCheck =
        rangeCells.contains(target) -> s"Target is not in range. ($target)"
      def IsFreeToStand(target: HexCoordinates)(implicit gameState: GameState): UseCheck =
        Seq(target).toCells.headOption.fold(false)(_.isFreeToStand) -> s"Target is not free to stand. ($target)"
      def IsFriendlySpawn(target: HexCoordinates)(implicit gameState: GameState): UseCheck =
        gameState.hexMap.getSpawnPointsFor(parentCharacter.owner.id).toCoords.contains(
          target
        ) -> s"Target is not a friendly spawn. ($target)"
    }
  }
}
