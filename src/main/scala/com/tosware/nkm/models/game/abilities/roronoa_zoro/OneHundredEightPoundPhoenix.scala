package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object OneHundredEightPoundPhoenix extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "108 Pound Phoenix",
      alternateName = "百八煩悩鳳 (Hyakuhachi Pound Hō)",
      abilityType = AbilityType.Ultimate,
      description =
        """Send 3 shockwaves towards an enemy.
          |Each shockwave deals {damage} physical damage.
          |
          |Range: linear, stops at walls and enemies, {range}
          |""".stripMargin,
      traits = Seq(AbilityTrait.ContactEnemy),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class OneHundredEightPoundPhoenix(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = OneHundredEightPoundPhoenix.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCellOpt.fold(Set.empty[HexCell])(_.getArea(
      metadata.variables("range"),
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
    )).toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  private def sendShockwave(direction: HexDirection)(implicit random: Random, gameState: GameState): GameState = {
    val ngs = for {
      parentCell <- parentCellOpt
      targetCharacter <-
        parentCell.firstCharacterInLine(direction, metadata.variables("range"), c => c.isEnemyForC(parentCharacterId))
    } yield gameState.damageCharacter(targetCharacter.id, Damage(DamageType.Physical, metadata.variables("damage")))(random, id)

    ngs.getOrElse(gameState)
  }
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    val ngs = for {
      targetCharacter <- gameState.characterByIdOpt(target)
      targetCell <- targetCharacter.parentCellOpt
      parentCell <- parentCellOpt
      direction <- parentCell.coordinates.getDirection(targetCell.coordinates)
    } yield {
      val g1 = sendShockwave(direction)
      val g2 = sendShockwave(direction)(random, g1)
      val g3 = sendShockwave(direction)(random, g2)
      g3
    }
    ngs.getOrElse(gameState)
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ characterBaseUseChecks(useData.firstAsCharacterId)
}
