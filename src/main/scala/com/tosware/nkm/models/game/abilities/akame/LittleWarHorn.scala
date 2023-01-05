package com.tosware.nkm.models.game.abilities.akame

import com.softwaremill.quicklens._
import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.GameEvent.EffectRemovedFromCharacter
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.StatBuff

import scala.util.Random

object LittleWarHorn {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Little War Horn",
      abilityType = AbilityType.Ultimate,
      description =
        """Character gains {attackPoints} AD and {speedIncrease} speed for {duration}t.
          |When this effect is finished, set this character's base speed to {finalSpeed}.""".stripMargin,
      variables = NkmConf.extract("abilities.akame.littleWarHorn"),
      relatedEffectIds = Seq(StatBuff.metadata.id),
    )
}

case class LittleWarHorn(abilityId: AbilityId, parentCharacterId: CharacterId, effectIdToListen: CharacterEffectId = "")
  extends Ability(abilityId, parentCharacterId)
    with UsableWithoutTarget
    with GameEventListener
{
  override val metadata = LittleWarHorn.metadata

  private val duration = metadata.variables("duration")

  def updateEffectToListen(effectId: CharacterEffectId)(implicit gameState: GameState): LittleWarHorn =
    this.modify(_.effectIdToListen).setTo(effectId)

  override def use()(implicit random: Random, gameState: GameState): GameState = {
    val adEffect = effects.StatBuff(NkmUtils.randomUUID(), duration, StatType.AttackPoints, metadata.variables("attackPoints"))
    val speedEffect = effects.StatBuff(NkmUtils.randomUUID(), duration, StatType.Speed, metadata.variables("speedIncrease"))
    gameState
      .addEffect(parentCharacterId, adEffect)(random, id)
      .addEffect(parentCharacterId, speedEffect)(random, id)
      .updateAbility(id, updateEffectToListen(speedEffect.effectId))
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case EffectRemovedFromCharacter(_, effectId) =>
        if(effectIdToListen == effectId) {
          gameState.setStat(parentCharacterId, StatType.Speed, metadata.variables("finalSpeed"))(random, id)
        } else gameState
      case _ => gameState
    }
  }
}
