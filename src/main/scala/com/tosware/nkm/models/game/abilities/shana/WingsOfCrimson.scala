package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object WingsOfCrimson extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wings of Crimson",
      alternateName = "紅蓮の双翼 (Guren no Sōyoku)",
      abilityType = AbilityType.Passive,
      description = "After receiving damage, you unfold wings, gaining {bonusSpeed} Speed and Flying for {duration}t. ",
      relatedEffectIds = Seq(
        effects.Fly.metadata.id,
        effects.StatBuff.metadata.id,
      ),
    )
}

case class WingsOfCrimson(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = WingsOfCrimson.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDamaged(_, _, _, _, characterId, _) =>
        if (characterId != parentCharacterId) return gameState
        val duration = metadata.variables("duration")
        val e1 = effects.Fly(randomUUID(), duration)
        val e2 = effects.StatBuff(randomUUID(), duration, StatType.Speed, metadata.variables("bonusSpeed"))
        gameState
          .addEffect(parentCharacterId, e1)(random, id)
          .addEffect(parentCharacterId, e2)(random, id)
      case _ =>
        gameState
    }
}
