package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.hex._

import scala.util.Random

object SummerBreeze {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Summer Breeze",
      abilityType = AbilityType.Normal,
      description =
        """Character summons Summer Breeze that knocks back selected enemy by {knockback}.
          |If the enemy will be knocked back into a wall or another character,
          |they will be stunned for {stunDuration}t and receive {damage} magical damage.
          |
          |Range: linear, {range}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.nibutani_shinka.summerBreeze"),
      relatedEffectIds = Seq(effects.Stun.metadata.id),
    )
}

case class SummerBreeze(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata: AbilityMetadata = SummerBreeze.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(
      _.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords
    )

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)


  private def stunAndDamage(target: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    gameState
      .addEffect(target, effects.Stun(randomUUID(), metadata.variables("stunDuration")))(random, id)
      .damageCharacter(target, Damage(DamageType.Magical, metadata.variables("damage")))(random, id)
  }


  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val direction = gameState.getDirection(parentCharacterId, target).get

    val (knockbackGs, knockbackResult) =
      gameState
        .abilityHitCharacter(id, target)
        .knockbackCharacter(target, direction, metadata.variables("knockback"))(random, id)

    knockbackResult match {
      case KnockbackResult.HitNothing =>
        knockbackGs
      case KnockbackResult.HitWall | KnockbackResult.HitEndOfMap | KnockbackResult.HitCharacter =>
        stunAndDamage(target)(random, knockbackGs)
    }
  }
}
