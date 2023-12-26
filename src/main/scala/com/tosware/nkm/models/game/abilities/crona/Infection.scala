package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Infection extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Infection",
      abilityType = AbilityType.Ultimate,
      description =
        """Infect an enemy with Black Blood for {duration}t.
          |Infected enemy also receives damage from Black Blood detonation.
          |
          |Range: circular, {range}""".stripMargin,
      relatedEffectIds = Seq(effects.BlackBlood.metadata.id),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class Infection(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = Infection.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val effect = effects.BlackBlood(randomUUID(), metadata.variables("duration"), parentCharacterId, abilityId)
    gameState.addEffect(useData.firstAsCharacterId, effect)(random, abilityId)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ characterBaseUseChecks(useData.firstAsCharacterId)
}
