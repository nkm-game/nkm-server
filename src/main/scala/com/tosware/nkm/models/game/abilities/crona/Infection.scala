package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object Infection {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Infection",
      abilityType = AbilityType.Ultimate,
      description =
        """Infect enemy with Black Blood for {duration}t.
          |Infected enemy also receives damage from Black Blood detonation
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.crona.infection"),
    )
}

case class Infection(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Infection.metadata

  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
