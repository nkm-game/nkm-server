package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.{Silence, Snare}
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object BindingRibbons extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Binding Ribbons",
      abilityType = AbilityType.Normal,
      description =
        """Cast a spell that Silences hit enemies for {silenceDuration}t.
          |If this ability hits at least {enemiesToHitToActivateSnare} enemies, they will be Snared for {snareDuration}t.
          |
          |Range: circular, {range}
          |Radius: circular, {radius}""".stripMargin,
      relatedEffectIds = Seq(Silence.metadata.id, Snare.metadata.id),
    )
}

case class BindingRibbons(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId) with UsableOnCoordinates {
  override val metadata: AbilityMetadata = BindingRibbons.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords

  private def hitCharacter(target: CharacterId, targetsHit: Int)(implicit
      random: Random,
      gameState: GameState,
  ): GameState = {
    val silencedGameState =
      gameState
        .abilityHitCharacter(id, target)
        .addEffect(target, effects.Silence(randomUUID(), metadata.variables("silenceDuration")))(random, id)

    if (targetsHit >= metadata.variables("enemiesToHitToActivateSnare")) {
      silencedGameState
        .addEffect(target, effects.Snare(randomUUID(), metadata.variables("snareDuration")))(random, id)
    } else silencedGameState
  }

  override def use(target: HexCoordinates, useData: UseData)(implicit
      random: Random,
      gameState: GameState,
  ): GameState = {
    val targets = target.getCircle(metadata.variables("radius")).whereSeenEnemiesOfC(parentCharacterId).characters.map(_.id)
    targets.foldLeft(gameState)((acc, cid) => hitCharacter(cid, targets.size)(random, acc))
  }
}
