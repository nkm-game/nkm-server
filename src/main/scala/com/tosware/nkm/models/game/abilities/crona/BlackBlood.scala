package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object BlackBlood extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Black Blood",
      abilityType = AbilityType.Passive,
      description = "You have permanent Black Blood effect.",
      relatedEffectIds = Seq(effects.BlackBlood.metadata.id),
    )
}

case class BlackBlood(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = BlackBlood.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharactersPicked(_) =>
        val effect = effects.BlackBlood(randomUUID(), Int.MaxValue, parentCharacterId, abilityId)
        gameState.addEffect(parentCharacterId, effect)(random, abilityId)
      case _ =>
        gameState
    }
}
