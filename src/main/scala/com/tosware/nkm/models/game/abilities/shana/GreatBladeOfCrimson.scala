package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.StatBuff

import scala.util.Random

object GreatBladeOfCrimson extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Great Blade of Crimson",
      alternateName = "紅蓮の大太刀 (Guren no Ōdachi)",
      abilityType = AbilityType.Normal,
      description = "Gain {bonusAD} AD and {bonusRange} range for {duration}t.",
      relatedEffectIds = Seq(effects.StatBuff.metadata.id),
    )
}

case class GreatBladeOfCrimson(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = GreatBladeOfCrimson.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .addEffect(
        parentCharacterId,
        StatBuff(randomUUID(), metadata.variables("duration"), StatType.AttackPoints, metadata.variables("bonusAD")),
      )(random, id)
      .addEffect(
        parentCharacterId,
        StatBuff(
          randomUUID(),
          metadata.variables("duration"),
          StatType.BasicAttackRange,
          metadata.variables("bonusRange"),
        ),
      )(random, id)
}
