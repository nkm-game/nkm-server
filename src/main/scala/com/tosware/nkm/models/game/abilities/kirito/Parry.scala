package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object Parry {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Parry",
      abilityType = AbilityType.Passive,
      description = "Character has a {dodgeChancePercent}% chance to avoid basic attack of an enemy",
      variables = NkmConf.extract("abilities.kirito.parry"),
    )
}

case class Parry(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with GameEventListener {
  override val metadata = Parry.metadata
  override val state = AbilityState(parentCharacterId)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = ???
}
