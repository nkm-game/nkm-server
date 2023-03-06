package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId

import scala.util.Random

object Steal {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Steal",
      abilityType = AbilityType.Ultimate,
      description =
        """Character steals armor from target enemy for {duration}t,
          |zeroing their physical and magical defense and adding them to themself.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.satou_kazuma.steal"),
    )
}

case class Steal(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata: AbilityMetadata = Steal.metadata

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
}
