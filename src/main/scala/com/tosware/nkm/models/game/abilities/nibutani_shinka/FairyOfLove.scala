package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.AbilityEnchant

import scala.util.Random

object FairyOfLove extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Fairy Of Love",
      abilityType = AbilityType.Ultimate,
      description = "Enchant the passive ability for {duration}t.",
      relatedEffectIds = Seq(effects.AbilityEnchant.metadata.id),
    )
}

case class FairyOfLove(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId) with Usable {
  override val metadata = FairyOfLove.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState.addEffect(
      parentCharacterId,
      AbilityEnchant(randomUUID(), metadata.variables("duration"), AbilityType.Passive),
    )(random, id)
}
