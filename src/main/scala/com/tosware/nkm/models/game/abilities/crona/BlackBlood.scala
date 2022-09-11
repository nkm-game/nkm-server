package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.NkmUtils

import scala.util.Random

object BlackBlood {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Black Blood",
      abilityType = AbilityType.Passive,
      description =
        """After receiving damage, character deals {damage} magical damage to surrounding enemies.
          |
          |Radius: circular, {radius}""".stripMargin,
      variables = NkmConf.extract("abilities.crona.blackBlood"),
    )
}

case class BlackBlood(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata = BlackBlood.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharactersPicked(_) =>
        val effect = effects.BlackBlood(NkmUtils.randomUUID(), Int.MaxValue, parentCharacterId, abilityId)
        gameState.addEffect(parentCharacterId, effect)(random, abilityId)
      case _ =>
        gameState
    }
}
