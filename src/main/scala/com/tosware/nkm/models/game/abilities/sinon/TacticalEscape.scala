package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.StatBuff

import scala.util.Random

object TacticalEscape {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tactical Escape",
      abilityType = AbilityType.Normal,
      description = "Character gains {speedIncrease} speed for {duration}t.",
      variables = NkmConf.extract("abilities.sinon.tacticalEscape"),
      relatedEffectIds = Seq(effects.StatBuff.metadata.id),
    )
}

case class TacticalEscape(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with Usable {
  override val metadata = TacticalEscape.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState.
      addEffect(parentCharacterId, StatBuff(randomUUID(), metadata.variables("duration"), StatType.Speed, metadata.variables("speedIncrease")))(random, id)
}
