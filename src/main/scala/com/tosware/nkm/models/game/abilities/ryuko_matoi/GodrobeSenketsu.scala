package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character_effect.CharacterEffect.CharacterEffectId
import com.tosware.nkm.models.game.GameEvent.TurnFinished
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableWithoutTarget}

import scala.util.Random

object GodrobeSenketsu {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Godrobe Senketsu",
      alternateName = "神衣鮮血",
      abilityType = AbilityType.Ultimate,
      description =
        """Character wears Senketsu, gaining flying effect and {initialAttackDamageBonus} AD.
          |At the end of this character's turn, gain additional {bonusAttackDamagePerTurn} and receive {damage} true damage.
          |Character can move after using this ability.
          |
          |This ability can be disabled.
          |When disabled, you lose all bonus AD from this ability, stop receiving damage and the ability goes on cooldown.""".stripMargin,
      variables = NkmConf.extract("abilities.ryukoMatoi.godrobeSenketsu"),
    )
}

case class GodrobeSenketsu(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
  attackDamageBonus: Int = 0,
  abilityEffects: Set[CharacterEffectId] = Set.empty,
) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget with GameEventListener {
  override val metadata = GodrobeSenketsu.metadata

  private def changeDamageBonus(newAdBonus: Int): GodrobeSenketsu =
    copy(abilityId, parentCharacterId, newAdBonus, abilityEffects)

  private def changeAbilityEffects(aes: Set[CharacterEffectId]): GodrobeSenketsu =
    copy(abilityId, parentCharacterId, attackDamageBonus, aes)

  private def applyAbilityEffects(adBonus: Int, newestAbility: GodrobeSenketsu)(implicit random: Random, gameState: GameState): GameState = {
    val e1 = effects.Fly(NkmUtils.randomUUID(), 1)
    val e2 = effects.StatBuff(NkmUtils.randomUUID(), 1, StatType.AttackPoints, adBonus)

    gameState
      .addEffect(parentCharacterId, e1)(random, id)
      .addEffect(parentCharacterId, e2)(random, id)
      .updateAbility(abilityId, newestAbility.changeAbilityEffects(Set(e1.id, e2.id))) // TODO: fix ability override
  }

  override def use()(implicit random: Random, gameState: GameState): GameState = {
    if(state.isEnabled) {
      gameState
        .updateAbility(abilityId, changeDamageBonus(0))
        .removeEffects(characterEffectIds = abilityEffects.toSeq)(random, id)
        .setAbilityEnabled(abilityId, false)
    } else {
      val initialAdBonus = metadata.variables("initialAttackDamageBonus")
      applyAbilityEffects(initialAdBonus, changeDamageBonus(initialAdBonus))
        .refreshBasicMove(parentCharacterId)(random, id)
        .setAbilityEnabled(abilityId, true)
    }
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    if(!state.isEnabled) return gameState

    e match {
      case TurnFinished(_, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if(characterIdThatTookAction != parentCharacter.id) gameState
        else {
          val bonusAdPerTurn = metadata.variables("bonusAttackDamagePerTurn")
          val newAdBonus = attackDamageBonus + bonusAdPerTurn
          applyAbilityEffects(newAdBonus, changeDamageBonus(newAdBonus))
            .damageCharacter(parentCharacterId, Damage(DamageType.True, metadata.variables("damage")))(random, id)
        }
      case _ =>
        gameState
    }
  }
}
