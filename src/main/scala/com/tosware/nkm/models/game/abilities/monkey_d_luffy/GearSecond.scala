package com.tosware.nkm.models.game.abilities.monkey_d_luffy

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType

import scala.util.Random

object GearSecond extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Gear Second",
      alternateName = "ギアセカンド (Giru Sekando)",
      abilityType = AbilityType.Ultimate,
      description =
        """Gain {bonusSpeed} speed and enchant your Normal ability for {duration}t.
          |""".stripMargin,
      relatedEffectIds = Seq(
        effects.StatBuff.metadata.id,
        effects.AbilityEnchant.metadata.id,
      ),
    )
}

case class GearSecond(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId)
    with Usable {
  override val metadata: AbilityMetadata = GearSecond.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .addEffect(
        parentCharacterId,
        effects.AbilityEnchant(randomUUID(), metadata.variables("duration"), AbilityType.Normal),
      )(random, id)
      .addEffect(
        parentCharacterId,
        effects.StatBuff(
          randomUUID(),
          metadata.variables("duration"),
          StatType.Speed,
          metadata.variables("bonusSpeed"),
        ),
      )(random, id)
}
