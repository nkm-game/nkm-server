package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

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
      relatedEffectIds = Seq(effects.BlackBlood.metadata.id),
    )
}

case class BlackBlood(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata = BlackBlood.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharactersPicked(_, _, _, _) =>
        val effect = effects.BlackBlood(randomUUID(), Int.MaxValue, parentCharacterId, abilityId)
        gameState.addEffect(parentCharacterId, effect)(random, abilityId)
      case _ =>
        gameState
    }
}
