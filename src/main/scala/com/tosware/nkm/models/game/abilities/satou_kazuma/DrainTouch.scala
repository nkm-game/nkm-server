package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._

import scala.util.Random

object DrainTouch {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Drain Touch",
      abilityType = AbilityType.Normal,
      description =
        """Character drains {damage} HP from target enemy, dealing magical damage and restoring HP equal to damage dealt to target.
          |
          |Range: linear, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.satou_kazuma.drainTouch"),
    )
}

case class DrainTouch(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata: AbilityMetadata = DrainTouch.metadata

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
}
