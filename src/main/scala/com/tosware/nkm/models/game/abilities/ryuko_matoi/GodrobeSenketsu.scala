package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ryuko_matoi.GodrobeSenketsu.{abilityEffectIdsKey, bonusDamageKey}
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.GameEvent.TurnFinished
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import spray.json.*

import scala.util.Random

object GodrobeSenketsu extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Godrobe Senketsu",
      alternateName = "神衣鮮血 (Kamui Senketsu)",
      abilityType = AbilityType.Ultimate,
      description =
        """Wear Senketsu.
          |While this ability is active:
          | - gain Flying and {initialAttackDamageBonus} AD
          | - at the end of your turn gain additional {bonusAttackDamagePerTurn} and receive {damage} true damage
          |
          |You can move after using this ability.
          |
          |This ability can be disabled.
          |When disabled, you lose all bonus AD from this ability, stop receiving damage and the ability goes on cooldown.""".stripMargin,
    )

  val bonusDamageKey: String = "bonusDamage"
  val abilityEffectIdsKey: String = "abilityEffectIds"
}

case class GodrobeSenketsu(
    abilityId: AbilityId,
    parentCharacterId: CharacterId,
) extends Ability(abilityId, parentCharacterId) with Usable with GameEventListener {
  override val metadata = GodrobeSenketsu.metadata

  private def damageBonus(implicit gameState: GameState): Int =
    state.variables.get(bonusDamageKey).map(_.toInt).getOrElse(0)

  private def abilityEffects()(implicit random: Random, gameState: GameState): Set[CharacterEffectId] =
    state.variables.get(abilityEffectIdsKey).map(_.parseJson.convertTo[Set[CharacterEffectId]]).getOrElse(Set.empty)

  private def changeDamageBonus(newAdBonus: Int)(implicit random: Random, gameState: GameState): GameState =
    gameState.setAbilityVariable(id, bonusDamageKey, newAdBonus.toString)

  private def changeAbilityEffects(aes: Set[CharacterEffectId])(implicit
      random: Random,
      gameState: GameState,
  ): GameState =
    gameState.setAbilityVariable(id, abilityEffectIdsKey, aes.toJson.toString)

  private def applyAbilityEffects()(implicit random: Random, gameState: GameState): GameState = {
    val e1 = effects.Fly(randomUUID(), Int.MaxValue)
    val e2 = effects.StatBuff(randomUUID(), Int.MaxValue, StatType.AttackPoints, damageBonus)

    val ngs = gameState.removeEffects(abilityEffects().toSeq)(random, id)
    changeAbilityEffects(Set(e1.id, e2.id))(random, ngs)
      .addEffect(parentCharacterId, e1)(random, id)
      .addEffect(parentCharacterId, e2)(random, id)
  }

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    if (state.isEnabled) {
      changeDamageBonus(0)
        .removeEffects(abilityEffects().toSeq)(random, id)
        .setAbilityEnabled(abilityId, false)
    } else {
      val initialAdBonus = metadata.variables("initialAttackDamageBonus")
      val ngs = changeDamageBonus(initialAdBonus)
      applyAbilityEffects()(random, ngs)
        .refreshBasicMove(parentCharacterId)(random, id)
        .setAbilityEnabled(abilityId, true)
    }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    if (!state.isEnabled) return gameState

    e match {
      case TurnFinished(_, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if (characterIdThatTookAction != parentCharacter.id) return gameState
        val bonusAdPerTurn = metadata.variables("bonusAttackDamagePerTurn")
        val newAdBonus = damageBonus + bonusAdPerTurn
        val ngs = changeDamageBonus(newAdBonus)
        applyAbilityEffects()(random, ngs)
          .damageCharacter(parentCharacterId, Damage(DamageType.True, metadata.variables("damage")))(random, id)
      case _ =>
        gameState
    }
  }
}
