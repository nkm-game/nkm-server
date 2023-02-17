package com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableWithoutTarget}

import scala.util.Random

object Survivor {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Survivor",
      abilityType = AbilityType.Normal,
      description =
        """Character becomes invisible for {invisibilityDuration}t or to first basic attack.
          |This character's next basic attack will deal {bonusDamagePercent}% damage and will stun the target for {stunDuration}t.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.ebisuzawa_kurumi.survivor"),
      relatedEffectIds = Seq(
        effects.Invisibility.metadata.id,
        effects.NextBasicAttackBuff.metadata.id,
        effects.ApplyEffectOnBasicAttack.metadata.id,
        effects.Stun.metadata.id,
      ),
    )
}

case class Survivor(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableWithoutTarget {
  override val metadata = Survivor.metadata

  override def use()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .addEffect(parentCharacterId, effects.Invisibility(randomUUID(), metadata.variables("invisibilityDuration")))(random, id)
      .addEffect(parentCharacterId, effects.ApplyEffectOnBasicAttack(
        randomUUID(),
        Int.MaxValue,
        effects.Stun(randomUUID(), metadata.variables("stunDuration"))),
      )(random, id)
      .addEffect(parentCharacterId, effects.NextBasicAttackBuff(
        randomUUID(),
        Int.MaxValue,
        metadata.variables("bonusDamagePercent") * parentCharacter.state.attackPoints), // TODO calculate buff at attack
      )(random, id)
}
