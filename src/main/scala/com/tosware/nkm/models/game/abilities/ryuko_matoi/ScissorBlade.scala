package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object ScissorBlade extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "ScissorBlade",
      abilityType = AbilityType.Passive,
      description =
        """Basic attacks decrease physical defense of target by {physicalDefenseDecrease} for {duration}t.
          |This effect is applied before basic attack and can stack.""".stripMargin,
      relatedEffectIds = Seq(effects.StatNerf.metadata.id),
      traits = Seq(AbilityTrait.ContactEnemy),
    )
}

case class ScissorBlade(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with GameEventListener {
  override val metadata: AbilityMetadata = ScissorBlade.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterPreparedToAttack(_, characterId, targetCharacterId) =>
        if (characterId != parentCharacterId) gameState
        else {
          val effect = effects.StatNerf(
            randomUUID(),
            metadata.variables("duration"),
            StatType.PhysicalDefense,
            metadata.variables("physicalDefenseDecrease"),
          )
          gameState.addEffect(targetCharacterId, effect)(random, id)
        }
      case _ => gameState
    }
}
