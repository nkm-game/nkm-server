package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object SummerBreeze extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Summer Breeze",
      abilityType = AbilityType.Normal,
      description =
        """Summon Summer Breeze that knocks back an enemy by {knockback}.
          |If the enemy will be knocked back into a wall or another character, Stun them for {stunDuration}t and deal {damage} magical damage.
          |
          |Range: linear, {range}""".stripMargin,
      relatedEffectIds = Seq(effects.Stun.metadata.id),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class SummerBreeze(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = SummerBreeze.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCellOpt.fold(Set.empty[HexCoordinates])(
      _.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords
    )
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  private def stunAndDamage(target: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .addEffect(target, effects.Stun(randomUUID(), metadata.variables("stunDuration")))(random, id)
      .damageCharacter(target, Damage(DamageType.Magical, metadata.variables("damage")))(random, id)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
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
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ characterBaseUseChecks(useData.firstAsCharacterId)
}
