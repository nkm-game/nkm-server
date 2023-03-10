package com.tosware.nkm.models.game.abilities.monkey_d_luffy

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._

import scala.util.Random

object GearSecond {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Gear Second",
      alternateName = "ギアセカンド",
      abilityType = AbilityType.Ultimate,
      description =
        """Character gains {bonusSpeed} speed and enchants their normal ability for {duration}t.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.monkey_d_luffy.gearSecond"),
      relatedEffectIds = Seq(
        effects.StatBuff.metadata.id,
        effects.AbilityEnchant.metadata.id,
      )
    )
}

case class GearSecond(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with Usable {
  override val metadata: AbilityMetadata = GearSecond.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    gameState
  }
}
