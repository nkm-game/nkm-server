package com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object Survivor extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Survivor",
      abilityType = AbilityType.Normal,
      description =
        """Become invisible for {invisibilityDuration}t.
          |Next basic attack will deal {bonusDamagePercent}% damage and Stun the target for {stunDuration}t.
          |""".stripMargin,
      relatedEffectIds = Seq(
        effects.Invisibility.metadata.id,
        effects.NextBasicAttackBuff.metadata.id,
        effects.ApplyEffectOnBasicAttack.metadata.id,
        effects.Stun.metadata.id,
      ),
    )
}

case class Survivor(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable {
  override val metadata = Survivor.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .addEffect(parentCharacterId, effects.Invisibility(randomUUID(), metadata.variables("invisibilityDuration")))(
        random,
        id,
      )
      .addEffect(
        parentCharacterId,
        effects.ApplyEffectOnBasicAttack(
          randomUUID(),
          Int.MaxValue,
          effects.Stun(randomUUID(), metadata.variables("stunDuration")),
        ),
      )(random, id)
      .addEffect(
        parentCharacterId,
        effects.NextBasicAttackBuff(
          randomUUID(),
          Int.MaxValue,
          (metadata.variables("bonusDamagePercent") - 100) * parentCharacter.state.attackPoints / 100,
        ), // TODO calculate buff at attack
      )(random, id)
}
