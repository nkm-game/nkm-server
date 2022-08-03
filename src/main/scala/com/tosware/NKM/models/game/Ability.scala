package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.Ability._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.hex.{HexCell, HexCoordinates}
import enumeratum._

object Ability {
  type AbilityId = String
  type AbilityMetadataId = String
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
  cooldown: Int = 0,
  range: Int = 0,
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
  def basicAttack(targetCharacterId: CharacterId)(implicit gameState: GameState): GameState
}

trait UsableOnCoordinates {
  def use(target: HexCoordinates, useData: UseData = UseData())(implicit gameState: GameState): GameState
}

trait UsableOnCharacter {
  def use(target: CharacterId, useData: UseData = UseData())(implicit gameState: GameState): GameState
}

trait Ability {
  def id: AbilityId = java.util.UUID.randomUUID.toString
  def metadata: AbilityMetadata
  def state: AbilityState
  def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates]
  def targetsInRange(implicit gameState: GameState): Set[HexCoordinates]
  def canBeUsed(implicit gameState: GameState): Boolean = false
  def use(targetIds: Seq[String])(implicit gameState: GameState): GameState = gameState
  def parentCharacter(implicit gameState: GameState): NKMCharacter =
    gameState.characters.find(_.state.abilities.map(_.id).contains(id)).get
  def parentCell(implicit gameState: GameState): Option[HexCell] =
    parentCharacter.parentCell
}