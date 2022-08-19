package com.tosware.NKM.models.game.abilities.llenn

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.abilities.hecate.Aster.radius
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils._
import com.tosware.NKM.models.{Damage, DamageType}

import scala.util.Random

object GrenadeThrow {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Grenade Throw",
      abilityType = AbilityType.Normal,
      description =
        "Character throws a granade, dealing physical damage in a sphere.".stripMargin,
      cooldown = NKMConf.int("abilities.llenn.grenadeThrow.cooldown"),
      range = NKMConf.int("abilities.llenn.grenadeThrow.range"),
    )
  val radius: Int = NKMConf.int("abilities.llenn.grenadeThrow.radius")
  val damage: Int = NKMConf.int("abilities.llenn.grenadeThrow.damage")
}

case class GrenadeThrow(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCoordinates {
  override val metadata = GrenadeThrow.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.range).whereExists

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targets = target.getCircle(radius).whereEnemiesOf(parentCharacterId).characters.map(_.id)
    val damage = Damage(DamageType.Physical, GrenadeThrow.damage)
    targets.foldLeft(gameState)((acc, cid) => blastCharacter(cid, damage)(random, acc))
  }

  private def blastCharacter(target: CharacterId, damage: Damage)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, damage)(random, id)
}
