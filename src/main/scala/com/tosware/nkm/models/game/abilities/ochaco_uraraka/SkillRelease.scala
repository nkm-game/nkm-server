package com.tosware.nkm.models.game.abilities.ochaco_uraraka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._

import scala.util.Random

object SkillRelease {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Skill Release",
      alternateName = "解除 (Kaijo)",
      abilityType = AbilityType.Ultimate,
      description =
        """Character releases their ability, and all characters lose Zero Gravity effect.
          |Enemies that lost Zero Gravity are stunned for {stunDuration}t.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.ochaco_uraraka.skillRelease"),
    )
}

case class SkillRelease(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with Usable {
  override val metadata: AbilityMetadata = SkillRelease.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = ???
}
