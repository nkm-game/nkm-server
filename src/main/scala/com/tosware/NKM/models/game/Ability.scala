package com.tosware.NKM.models.game

import enumeratum._

sealed trait AbilityType extends EnumEntry
object AbilityType extends Enum[AbilityType] {
  val values = findValues

  case object Passive extends AbilityType
  case object Normal extends AbilityType
  case object Ultimate extends AbilityType
}

case class AbilityMetadata
(
  abilityType: AbilityType,
  name: String,
  description: NKMCharacter => String,
  cooldown: Int = 0,
  parentCanAttackAllies: Boolean = false,
)

case class AbilityState
(
  currentParentCharacter: NKMCharacter,
  currentCooldown: Int = 0,
  currentRangeCells: List[HexCell],
  currentTargetsInRange: List[HexCell],
  currentCanBeUsed: List[HexCell],
  currentDescription: String,
)


trait Ability {
  val id: String
  val metadataId: String
  val state: AbilityState
  val parentBaseAttackOverride: Option[(GameState, NKMCharacter) => GameState] = None
  val rangeCells: GameState => List[HexCell]
  val targetsInRange: GameState => List[HexCell]
  val canBeUsed: GameState => Boolean = _ => false
  val use: (GameState, Option[NKMCharacter], Option[HexCell]) => GameState = (gameState, _, _) => gameState
}