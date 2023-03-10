package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._

import scala.util.Random

object MjolnirHammer {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mjolnir Hammer",
      abilityType = AbilityType.Normal,
      description =
        """Character hits twice, dealing {damage} physical damage on each hit.
          |If both attacks target the same character, it will receive half damage from second hit.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.dekomori_sanae.mjolnirHammer"),
    )
}

case class MjolnirHammer(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata: AbilityMetadata = MjolnirHammer.metadata

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
}
