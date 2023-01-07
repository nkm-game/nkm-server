package com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

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
      relatedEffectIds = Seq(effects.Stun.metadata.id),
    )
}

case class Survivor(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget {
  override val metadata = Survivor.metadata

  override def use()(implicit random: Random, gameState: GameState): GameState =
    gameState
}
