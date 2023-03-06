package com.tosware.nkm.models.game.abilities.ochaco_uraraka

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId

import scala.util.Random

object ReducedWeight {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Reduced Weight",
      abilityType = AbilityType.Normal,
      description =
        """Character applies Zero Gravity effect on a friendly character.
          |Additionally, doubles speed of target for {speedBuffDuration}t.
          |
          |Range: basic attack range""".stripMargin,
      variables = NkmConf.extract("abilities.ochaco_uraraka.reducedWeight"),
    )
}

case class ReducedWeight(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata: AbilityMetadata = ReducedWeight.metadata

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
}
