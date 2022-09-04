package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object StarburstStream {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Starburst Stream",
      abilityType = AbilityType.Ultimate,
      description =
        """Attack the enemy {attackTimes} times.
          |Every hit deals {damage} true damage.
          |After using this ability, you can permanently basic attack 2 times per turn.
          |
          |Range: linear, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.kirito.starburstStream"),
    )
}

case class StarburstStream(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = StarburstStream.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
