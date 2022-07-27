package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.Ability._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
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
  abilityType: AbilityType,
  name: String,
  description: String,
  cooldown: Int = 0,
  parentCanAttackAllies: Boolean = false,
) {
  val id: AbilityMetadataId = name
}

case class AbilityState
(
  currentParentCharacterId: CharacterId,
  currentCooldown: Int = 0,
  currentRangeCells: Seq[HexCoordinates],
  currentTargetsInRange: Seq[HexCoordinates],
  currentCanBeUsed: Boolean,
  currentDescription: String,
)


trait Ability {
  val id: AbilityId
  val metadataId: AbilityMetadataId
  val state: AbilityState
  val parentBaseAttackOverride: Option[(GameState, NKMCharacter) => GameState] = None
  val rangeCells: GameState => Set[HexCell]
  val targetsInRange: GameState => Set[HexCell]
  val canBeUsed: GameState => Boolean = _ => false
  val use: (GameState, Option[NKMCharacter], Option[HexCell]) => GameState = (gameState, _, _) => gameState
}