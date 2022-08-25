package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.Aster.radius
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.{Damage, DamageType}

import scala.util.Random

object Aster {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Aster",
      alternateName = "星",
      abilityType = AbilityType.Normal,
      description =
        "Character shoots rays of energy from Aster, dealing magical damage in a sphere to enemies.".stripMargin,
      cooldown = NkmConf.int("abilities.hecate.aster.cooldown"),
      range = NkmConf.int("abilities.hecate.aster.range"),
    )
  val radius: Int = NkmConf.int("abilities.hecate.aster.radius")
  val damage: Int = NkmConf.int("abilities.hecate.aster.damage")
}

case class Aster(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCoordinates {
  override val metadata = Aster.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.range).whereExists

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targets = target.getCircle(radius).whereEnemiesOfC(parentCharacterId).characters.map(_.id)
    val damage = Damage(DamageType.Magical, Aster.damage)
    targets.foldLeft(gameState)((acc, cid) => blastCharacter(cid, damage)(random, acc))
  }

  private def blastCharacter(target: CharacterId, damage: Damage)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, damage)(random, id)
}
