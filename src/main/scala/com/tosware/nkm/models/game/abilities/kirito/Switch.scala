package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object Switch {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Switch",
      abilityType = AbilityType.Normal,
      description =
        """Switch places with selected friend.
          |You or your friend have to be in a basic attack range of an enemy.
          |You can use basic attack or ultimate ability just after using this ability.
          |
          |Range: linear, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.kirito.switch"),
    )
}

case class Switch(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Switch.metadata

  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
