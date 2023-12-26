package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.effects.Stun
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object ScreechAlpha extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Screech Alpha",
      abilityType = AbilityType.Normal,
      description =
        """Stun nearby enemies for {stunDuration}t and slow them by {slowAmount} for {slowDuration}t.
          |
          |Radius: {radius}""".stripMargin,
      relatedEffectIds = Seq(Stun.metadata.id),
      traits = Seq(AbilityTrait.ContactEnemy),
    )
}

case class ScreechAlpha(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = ScreechAlpha.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("radius")).whereExists
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  private def addEffects(target: CharacterId)(implicit random: Random, gameState: GameState) = {
    val silenceEffect = effects.Stun(
      randomUUID(),
      metadata.variables("stunDuration"),
    )
    val slowEffect = effects.StatNerf(
      randomUUID(),
      metadata.variables("slowDuration"),
      StatType.Speed,
      metadata.variables("slowAmount"),
    )
    gameState
      .addEffect(target, silenceEffect)(random, abilityId)
      .addEffect(target, slowEffect)(random, abilityId)
  }
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    targetsInRange.characters.map(_.id)
      .foldLeft(gameState) { (acc, cid) =>
        addEffects(cid)(random, acc)
      }
}
