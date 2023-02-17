package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}

import scala.util.Random

object Infection {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Infection",
      abilityType = AbilityType.Ultimate,
      description =
        """Character infects enemy with Black Blood for {duration}t.
          |Infected enemy also receives damage from Black Blood detonation.
          |
          |Range: circular, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.crona.infection"),
      relatedEffectIds = Seq(effects.BlackBlood.metadata.id),
    )
}

case class Infection(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata = Infection.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val effect = effects.BlackBlood(NkmUtils.randomUUID(), metadata.variables("duration"), parentCharacterId, abilityId)
    gameState.addEffect(target, effect)(random, abilityId)
  }
}
