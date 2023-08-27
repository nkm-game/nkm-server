package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.{Silence, Snare}
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object BindingRibbons {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Binding Ribbons",
      abilityType = AbilityType.Normal,
      description =
        """Character casts a spell that silents all hit enemies for {silenceDuration}t.
          |If it hits at least {enemiesToHitToActivateSnare} enemies, they will be snared for {rootDuration}t.
          |
          |Range: circular, {range}
          |Radius: circular, {radius}""".stripMargin,
      variables = NkmConf.extract("abilities.carmelWilhelmina.bindingRibbons"),
      relatedEffectIds = Seq(Silence.metadata.id, Snare.metadata.id),
    )
}

case class BindingRibbons(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCoordinates {
  override val metadata: AbilityMetadata = BindingRibbons.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords

  private def hitCharacter(target: CharacterId, targetsHit: Int)(implicit random: Random, gameState: GameState): GameState = {
    val silencedGameState =
      gameState
        .abilityHitCharacter(id, target)
        .addEffect(target, effects.Silence(randomUUID(), metadata.variables("silenceDuration")))(random, id)

    if(targetsHit >= metadata.variables("enemiesToHitToActivateSnare")) {
      silencedGameState
        .addEffect(target, effects.Snare(randomUUID(), metadata.variables("rootDuration")))(random, id)
    } else silencedGameState
  }

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targets = target.getCircle(metadata.variables("radius")).whereEnemiesOfC(parentCharacterId).characters.map(_.id)
    targets.foldLeft(gameState)((acc, cid) => hitCharacter(cid, targets.size)(random, acc))
  }
}
