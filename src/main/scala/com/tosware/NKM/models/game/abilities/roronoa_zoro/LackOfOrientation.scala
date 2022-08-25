package com.tosware.NKM.models.game.abilities.roronoa_zoro

import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexCoordinates

import scala.util.Random

object LackOfOrientation {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Lack of orientation",
      abilityType = AbilityType.Passive,
      description = "Character has a 50% chance to go get lost during basic move.",
    )
}

case class LackOfOrientation(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with BasicMoveOverride {
  override val metadata = LackOfOrientation.metadata
  override val state = AbilityState(parentCharacterId)

  override def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState = ???
}
