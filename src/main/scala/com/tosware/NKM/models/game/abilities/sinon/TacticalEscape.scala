package com.tosware.NKM.models.game.abilities.sinon

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.sinon.TacticalEscape.{duration, speedIncrease}
import com.tosware.NKM.models.game.effects.StatBuffEffect

import scala.util.Random

object TacticalEscape {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tactical Escape",
      abilityType = AbilityType.Normal,
      description = "Character increases its speed for a phase.",
      cooldown = NKMConf.int("abilities.sinon.tacticalEscape.cooldown"),
    )
  val duration: Int = NKMConf.int("abilities.sinon.tacticalEscape.duration")
  val speedIncrease: Int = NKMConf.int("abilities.sinon.tacticalEscape.speedIncrease")
}

case class TacticalEscape(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableWithoutTarget {
  override val metadata = TacticalEscape.metadata
  override val state = AbilityState(parentCharacterId)

  override def use()(implicit random: Random, gameState: GameState): GameState =
    gameState.
      addEffect(parentCharacterId, StatBuffEffect(parentCharacterId, duration, StatType.Speed, speedIncrease))(random, id)
}
