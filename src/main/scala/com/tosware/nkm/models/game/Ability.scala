package com.tosware.nkm.models.game

import com.softwaremill.quicklens._
import com.tosware.nkm.NkmUtils
import com.tosware.nkm.models.CommandResponse._
import com.tosware.nkm.models.game.Ability._
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.effects.FreeAbility
import com.tosware.nkm.models.game.hex.{HexCell, HexCoordinates}
import enumeratum._

import scala.util.{Random, Try}

object Ability {
  type AbilityId = String
  type AbilityMetadataId = String
  type UseCheck = (Boolean, String)
}

sealed trait AbilityType extends EnumEntry
object AbilityType extends Enum[AbilityType] {
  val values = findValues

  case object Passive extends AbilityType
  case object Normal extends AbilityType
  case object Ultimate extends AbilityType
}

case class AbilityMetadata
(
  name: String,
  abilityType: AbilityType,
  description: String,
  variables: Map[String, Int] = Map.empty,
  alternateName: String = "",
  relatedEffectIds: Seq[CharacterEffectId] = Seq.empty,
) {
  val id: AbilityMetadataId = name
}

case class AbilityState
(
  cooldown: Int = 0,
  isEnabled: Boolean = false,
  variables: Map[String, String] = Map.empty,
)


case class UseData(data: String = "")

trait BasicAttackOverride {
  def basicAttackCells(implicit gameState: GameState): Set[HexCoordinates]
  def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates]
  def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState
}

trait BasicMoveOverride {
  def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState
}

trait UsableWithoutTarget { this: Ability =>
  def use()(implicit random: Random, gameState: GameState): GameState
  def useChecks(implicit gameState: GameState): Set[UseCheck] =
    baseUseChecks
  final def canBeUsed(implicit gameState: GameState): CommandResponse =
    _canBeUsed(useChecks)
}

trait UsableOnTarget[T] { this: Ability =>
  def use(target: T, useData: UseData = UseData())(implicit random: Random, gameState: GameState): GameState
  def useChecks(implicit target: T, useData: UseData, gameState: GameState): Set[UseCheck] =
    baseUseChecks
  final def canBeUsed(implicit target: T, useData: UseData, gameState: GameState): CommandResponse =
    _canBeUsed(useChecks)
}

trait UsableOnCoordinates extends UsableOnTarget[HexCoordinates] { this: Ability =>
  override def useChecks(implicit target: HexCoordinates, useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks + UseCheck.TargetCoordinates.InRange
}

trait UsableOnCharacter extends UsableOnTarget[CharacterId] { this: Ability =>
  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks + UseCheck.TargetCharacter.InRange
}

abstract class Ability(val id: AbilityId, pid: CharacterId) extends NkmUtils {
  val metadata: AbilityMetadata

  def _canBeUsed(useChecks: Set[UseCheck])(implicit gameState: GameState): CommandResponse = {
    val failures = useChecks.filter(_._1 == false)
    if(failures.isEmpty) Success()
    else Failure(failures.map(_._2).mkString("\n"))
  }

  def baseUseChecks(implicit gameState: GameState): Set[UseCheck] = {
    Set(
      UseCheck.Base.IsNotPassive,
      UseCheck.Base.IsNotOnCooldown,
      UseCheck.Base.ParentCharacterOnMap,
      UseCheck.Base.CanBeUsedByParent,
    ) ++
    Option.when(metadata.abilityType == AbilityType.Ultimate)(UseCheck.Base.PhaseIsGreaterThan(3)).toSet
  }

  def state(implicit gameState: GameState): AbilityState =
    gameState.abilityStates(id)

  def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] = Set.empty
  def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] = Set.empty
  def parentCharacter(implicit gameState: GameState): NkmCharacter =
    gameState.characters.find(_.state.abilities.map(_.id).contains(id)).get

  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCell

  def getCooldownState(implicit gameState: GameState): AbilityState =
    state.copy(cooldown = metadata.variables("cooldown"))

  def getDecrementCooldownState(amount: Int = 1)(implicit gameState: GameState): AbilityState =
    state.copy(cooldown = math.max(state.cooldown - amount, 0))

  def getEnabledChangedState(newEnabled: Boolean)(implicit gameState: GameState): AbilityState =
    state.copy(isEnabled = newEnabled)

  def getVariablesChangedState(key: String, value: String)(implicit gameState: GameState): AbilityState =
    state.modify(_.variables).using(_.updated(key, value))

  def toView(implicit gameState: GameState): AbilityView = {
    val canBeUsedResponse = _canBeUsed(baseUseChecks)
    val canBeUsed = canBeUsedResponse match {
      case Success(_) => true
      case Failure(_) => false
    }
    val canBeUsedFailureMessage = canBeUsedResponse match {
      case Success(_) => None
      case Failure(msg) => Some(msg)
    }

    AbilityView(
      id = id,
      metadataId = metadata.id,
      parentCharacterId = parentCharacter.id,
      state = state,
      rangeCellCoords = Try(rangeCellCoords).getOrElse(Set.empty),
      targetsInRange = Try(targetsInRange).getOrElse(Set.empty),
      canBeUsed = canBeUsed,
      canBeUsedFailureMessage = canBeUsedFailureMessage,
    )
  }

  object UseCheck {
    object Base {
      def IsNotPassive(implicit gameState: GameState): UseCheck =
        (metadata.abilityType != AbilityType.Passive) -> s"Cannot use passive abilities."
      def IsNotOnCooldown(implicit gameState: GameState): UseCheck =
        (state.cooldown <= 0 || parentCharacter.state.effects.ofType[FreeAbility].nonEmpty) -> "Ability is on cooldown."
      def ParentCharacterOnMap(implicit gameState: GameState): UseCheck =
        parentCharacter.isOnMap -> "Parent character is not on map."
      def PhaseIsGreaterThan(i: Int)(implicit gameState: GameState): UseCheck =
        (gameState.phase.number > i) -> s"Phase is not greater than $i."
      def CanBeUsedByParent(implicit gameState: GameState): UseCheck = {
        parentCharacter.canUseAbilityOfType(metadata.abilityType) -> s"Ability cannot be used by parent character."
      }
    }
    object TargetCharacter {
      def InRange(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
        targetsInRange.toCells.exists(_.characterId.contains(target)) -> "Target character is not in range."
      def IsEnemy(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
        gameState.characterById(target).isEnemyForC(parentCharacter.id) -> "Target character is not an enemy."
      def IsFriend(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
        gameState.characterById(target).isFriendForC(parentCharacter.id) -> "Target character is not a friend."
      def IsOnMap(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
        gameState.characterById(target).isOnMap -> "Target character is not on map."
    }

    object TargetCoordinates {
      def InRange(implicit target: HexCoordinates, useData: UseData, gameState: GameState): UseCheck =
        Seq(target).toCells.nonEmpty -> "Target character is not in range."
      def IsFreeToStand(implicit target: HexCoordinates, useData: UseData, gameState: GameState): UseCheck =
        Seq(target).toCells.headOption.fold(false)(_.isFreeToStand) -> "Target is not free to stand."
      def IsFriendlySpawn(implicit target: HexCoordinates, useData: UseData, gameState: GameState): UseCheck =
        gameState.hexMap.getSpawnPointsFor(parentCharacter.owner.id).toCoords.contains(target) -> "Target is not a friendly spawn."
    }
  }
}

case class AbilityView
(
  id: AbilityId,
  metadataId: AbilityMetadataId,
  parentCharacterId: CharacterId,
  state: AbilityState,
  rangeCellCoords: Set[HexCoordinates],
  targetsInRange: Set[HexCoordinates],
  canBeUsed: Boolean,
  canBeUsedFailureMessage: Option[String],
)
