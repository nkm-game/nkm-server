package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object ScreechAlpha {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Screech Alpha",
      abilityType = AbilityType.Normal,
      description =
        """Silence nearby enemies for {silenceDuration}t and slow them by {slowAmount} for {slowDuration}t.
          |
          |Radius: {radius}""".stripMargin,
      variables = NkmConf.extract("abilities.crona.screechAlpha"),
    )
}

case class ScreechAlpha(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget {
  override val metadata = ScreechAlpha.metadata

  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use()(implicit random: Random, gameState: GameState): GameState = ???
}
