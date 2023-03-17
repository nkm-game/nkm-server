package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.hex.{HexCoordinates, SearchFlag}

import scala.util.Random

object DrainTouch {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Drain Touch",
      abilityType = AbilityType.Normal,
      description =
        """Character drains {damage} HP from target enemy, dealing magical damage and restoring HP equal to damage dealt to target.
          |
          |Range: linear, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.satou_kazuma.drainTouch"),
    )
}

case class DrainTouch(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter {
  override val metadata: AbilityMetadata = DrainTouch.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(
      _.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords
    )

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val hitGs = hitAndDamageCharacter(target, Damage(DamageType.Magical, metadata.variables("damage")))
    val amountToHealOpt =
      hitGs
        .gameLog
        .events
        .ofType[CharacterDamaged]
        .causedBy(id)
        .lastOption
        .map(_.damage.amount)
    amountToHealOpt.fold(hitGs) { amountToHeal =>
      hitGs.heal(parentCharacterId, amountToHeal)(random, id)
    }
  }
}
