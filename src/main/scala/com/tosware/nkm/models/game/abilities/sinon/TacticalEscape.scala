package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape.{duration, speedIncrease}
import com.tosware.nkm.models.game.effects.StatBuffEffect

import scala.util.Random

object TacticalEscape {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tactical Escape",
      abilityType = AbilityType.Normal,
      description = "Character increases its speed for a phase.",
      cooldown = NkmConf.int("abilities.sinon.tacticalEscape.cooldown"),
    )
  val duration: Int = NkmConf.int("abilities.sinon.tacticalEscape.duration")
  val speedIncrease: Int = NkmConf.int("abilities.sinon.tacticalEscape.speedIncrease")
}

case class TacticalEscape(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableWithoutTarget {
  override val metadata = TacticalEscape.metadata
  override val state = AbilityState(parentCharacterId)

  override def use()(implicit random: Random, gameState: GameState): GameState =
    gameState.
      addEffect(parentCharacterId, StatBuffEffect(parentCharacterId, duration, StatType.Speed, speedIncrease))(random, id)
}
