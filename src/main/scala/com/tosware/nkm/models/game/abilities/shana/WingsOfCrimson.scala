package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.shana.WingsOfCrimson.abilityEffectIdsKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState
import spray.json.*

import scala.util.Random

object WingsOfCrimson extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wings of Crimson",
      alternateName = "紅蓮の双翼 (Guren no Sōyoku)",
      abilityType = AbilityType.Passive,
      description =
        "After receiving damage, you unfold wings, gaining {bonusSpeed} Speed and Flying for {duration}t. (effect does not stack)",
      relatedEffectIds = Seq(
        effects.Fly.metadata.id,
        effects.StatBuff.metadata.id,
      ),
    )
  val abilityEffectIdsKey: String = "abilityEffectIds"
}

case class WingsOfCrimson(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = WingsOfCrimson.metadata

  val duration = metadata.variables("duration")
  val bonusSpeed = metadata.variables("bonusSpeed")

  private def abilityEffects()(implicit random: Random, gameState: GameState): Set[CharacterEffectId] =
    state.variables.get(abilityEffectIdsKey).map(_.parseJson.convertTo[Set[CharacterEffectId]]).getOrElse(Set.empty)

  private def changeAbilityEffects(aes: Set[CharacterEffectId])(
      implicit
      random: Random,
      gameState: GameState,
  ): GameState =
    gameState.setAbilityVariable(id, abilityEffectIdsKey, aes.toJson.toString)

  private def applyAbilityEffects()(implicit random: Random, gameState: GameState): GameState = {
    val e1 = effects.Fly(randomUUID(), duration)
    val e2 = effects.StatBuff(randomUUID(), duration, StatType.Speed, bonusSpeed)

    val ngs = gameState.removeEffects(abilityEffects().toSeq)(random, id)
    changeAbilityEffects(Set(e1.id, e2.id))(random, ngs)
      .addEffect(parentCharacterId, e1)(random, id)
      .addEffect(parentCharacterId, e2)(random, id)
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDamaged(_, characterId, _) if characterId == parentCharacterId =>
        applyAbilityEffects()
      case _ =>
        gameState
    }
}
