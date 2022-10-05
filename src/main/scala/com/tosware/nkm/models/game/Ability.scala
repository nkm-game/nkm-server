package com.tosware.nkm.models.game

import com.tosware.nkm.models.CommandResponse.{CommandResponse, Failure, Success}
import com.tosware.nkm.models.game.Ability._
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.game.hex.{HexCell, HexCoordinates}
import enumeratum._

import scala.util.Random

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
) {
  val id: AbilityMetadataId = name
}

case class AbilityState
(
  parentCharacterId: CharacterId,
  cooldown: Int = 0,
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
    Set(
      UseCheck.NotOnCooldown,
      UseCheck.ParentCharacterOnMap,
    )

  final def canBeUsed(implicit gameState: GameState): CommandResponse = {
    val failures = useChecks.filter(_._1 == false)
    if(failures.isEmpty) Success()
    else Failure(failures.map(_._2).mkString("\n"))
  }
}

trait UsableOnTarget[T] { this: Ability =>
  def use(target: T, useData: UseData = UseData())(implicit random: Random, gameState: GameState): GameState
  def useChecks(implicit target: T, useData: UseData, gameState: GameState): Set[UseCheck] =
    Set(
      UseCheck.NotOnCooldown,
      UseCheck.ParentCharacterOnMap,
    )

  final def canBeUsed(implicit target: T, useData: UseData, gameState: GameState): CommandResponse = {
    val failures = useChecks.filter(_._1 == false)
    if(failures.isEmpty) Success()
    else Failure(failures.map(_._2).mkString("\n"))
  }
}

trait UsableOnCoordinates extends UsableOnTarget[HexCoordinates] { this: Ability =>
  override def useChecks(implicit target: HexCoordinates, useData: UseData, gameState: GameState): Set[UseCheck] = {
    super.useChecks ++
    Set(
      UseCheck.TargetCoordsInRange,
    )
  }
}

trait UsableOnCharacter extends UsableOnTarget[CharacterId] { this: Ability =>
  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    super.useChecks ++
    Set(
      UseCheck.TargetCharacterInRange,
    )
  }
}

abstract class Ability(val id: AbilityId, pid: CharacterId) {
  val metadata: AbilityMetadata

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

  def getDecrementCooldownState(implicit gameState: GameState): AbilityState =
    state.copy(cooldown = math.max(state.cooldown - 1, 0))

  object UseCheck {
    def NotOnCooldown(implicit gameState: GameState): UseCheck =
      (state.cooldown <= 0) -> "Ability is on cooldown."
    def ParentCharacterOnMap(implicit gameState: GameState): UseCheck =
      parentCharacter.isOnMap -> "Parent character is not on map."
    def TargetCharacterInRange(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
      targetsInRange.toCells.exists(_.characterId.contains(target)) -> "Target character is not in range."
    def TargetIsEnemy(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
      gameState.characterById(target).get.isEnemyForC(parentCharacter.id) -> "Target character is not an enemy."
    def TargetIsFriend(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
      gameState.characterById(target).get.isFriendForC(parentCharacter.id) -> "Target character is not a friend."
    def TargetIsOnMap(implicit target: CharacterId, useData: UseData, gameState: GameState): UseCheck =
      gameState.characterById(target).get.isOnMap -> "Target character is not on map."
    def TargetCoordsInRange(implicit target: HexCoordinates, useData: UseData, gameState: GameState): UseCheck =
      Seq(target).toCells.nonEmpty -> "Target character is not in range."
    def TargetIsFriendlySpawn(implicit target: HexCoordinates, useData: UseData, gameState: GameState): UseCheck =
      gameState.hexMap.get.getSpawnPointsFor(parentCharacter.owner.id).toCoords.contains(target) -> "Target is not a friendly spawn."
    def TargetIsFreeToStand(implicit target: HexCoordinates, useData: UseData, gameState: GameState): UseCheck = {
      Seq(target).toCells.headOption.fold(false)(_.isFreeToStand) -> "Target is not free to stand."
    }
  }
}