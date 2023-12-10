package com.tosware.nkm.models.game.abilities.akame

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.StatBuff
import com.tosware.nkm.models.game.event.GameEvent.EffectRemovedFromCharacter
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object LittleWarHorn extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Little War Horn",
      abilityType = AbilityType.Ultimate,
      description =
        """Gain {attackPoints} AD and {speedIncrease} Speed for {duration}t.
          |When the effect is finished, permanently set base Speed to {finalSpeed}.""".stripMargin,
      relatedEffectIds = Seq(StatBuff.metadata.id),
    )
}

case class LittleWarHorn(abilityId: AbilityId, parentCharacterId: CharacterId, effectIdToListen: CharacterEffectId = "")
    extends Ability(abilityId)
    with Usable
    with GameEventListener {
  override val metadata: AbilityMetadata = LittleWarHorn.metadata

  private val duration = metadata.variables("duration")

  private def updateEffectToListen(effectId: CharacterEffectId): LittleWarHorn =
    this.modify(_.effectIdToListen).setTo(effectId)

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val adEffect = effects.StatBuff(randomUUID(), duration, StatType.AttackPoints, metadata.variables("attackPoints"))
    val speedEffect = effects.StatBuff(randomUUID(), duration, StatType.Speed, metadata.variables("speedIncrease"))
    gameState
      .addEffect(parentCharacterId, adEffect)(random, id)
      .addEffect(parentCharacterId, speedEffect)(random, id)
      .updateAbility(id, updateEffectToListen(speedEffect.effectId))
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case EffectRemovedFromCharacter(_, _, _, _, _, effectId, _) =>
        if (effectIdToListen == effectId) {
          gameState.setStat(parentCharacterId, StatType.Speed, metadata.variables("finalSpeed"))(random, id)
        } else gameState
      case _ => gameState
    }
}
