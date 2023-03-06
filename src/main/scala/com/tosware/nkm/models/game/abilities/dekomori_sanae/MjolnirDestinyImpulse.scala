package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object MjolnirDestinyImpulse {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mjolnir Destiny Impulse",
      abilityType = AbilityType.Ultimate,
      description =
        """Character hits with a hammer in a selected area, dealing {damage} physical damage to all enemies in radius {radius}.
          |This ability can be used again this turn if it kills at least one enemy.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.dekomori_sanae.mjolnirDestinyImpulse"),
    )
}

case class MjolnirDestinyImpulse(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCoordinates {
  override val metadata: AbilityMetadata = MjolnirDestinyImpulse.metadata

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = ???
}
