package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.StatBuff

import scala.util.Random

object TacticalEscape extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tactical Escape",
      abilityType = AbilityType.Normal,
      description = "Gain {speedIncrease} Speed for {duration}t.",
      relatedEffectIds = Seq(effects.StatBuff.metadata.id),
    )
}

case class TacticalEscape(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = TacticalEscape.metadata
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val e = StatBuff(randomUUID(), metadata.variables("duration"), StatType.Speed, metadata.variables("speedIncrease"))
    gameState.addEffect(parentCharacterId, e)(random, id)
  }
}
