package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.StatBuff
import com.tosware.nkm.models.game.hex.NkmUtils

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

case class TacticalEscape(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget {
  override val metadata = TacticalEscape.metadata

  override def use()(implicit random: Random, gameState: GameState): GameState =
    gameState.
      addEffect(parentCharacterId, StatBuff(NkmUtils.randomUUID(), metadata.variables("duration"), StatType.Speed, metadata.variables("speedIncrease")))(random, id)
}
