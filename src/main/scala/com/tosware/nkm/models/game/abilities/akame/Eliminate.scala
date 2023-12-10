package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Eliminate extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Eliminate",
      abilityType = AbilityType.Normal,
      description =
        """Hit critically, dealing double AD damage to target.
          |
          |Range: circular, {range}""".stripMargin,
      traits = Seq(AbilityTrait.ContactEnemy),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class Eliminate(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = Eliminate.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range"))

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, Damage(DamageType.Physical, parentCharacter.state.attackPoints * 2))(random, id)
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target = useData.firstAsCharacterId
    super.useChecks ++ characterBaseUseChecks(target) ++ Seq(
      UseCheck.Character.IsEnemy(useData.firstAsCharacterId)
    )
  }
}
