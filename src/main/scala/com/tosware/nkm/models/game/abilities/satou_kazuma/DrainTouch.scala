package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.GameEvent.CharacterDamaged
import com.tosware.nkm.models.game.hex.{HexCoordinates, SearchFlag}

import scala.util.Random

object DrainTouch extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Drain Touch",
      abilityType = AbilityType.Normal,
      description =
        """Drain HP from target enemy, dealing {damage} magical damage and restoring HP equal to damage dealt to target.
          |
          |Range: linear, {range}""".stripMargin,
      traits = Seq(AbilityTrait.ContactEnemy),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class DrainTouch(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = DrainTouch.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(
      _.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords
    )
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    val hitGs = hitAndDamageCharacter(target, Damage(DamageType.Magical, metadata.variables("damage")))
    val amountToHealOpt =
      hitGs
        .gameLog
        .events
        .ofType[CharacterDamaged]
        .causedBy(id)
        .lastOption
        .map(_.damageAmount)
    amountToHealOpt.fold(hitGs) { amountToHeal =>
      hitGs.heal(parentCharacterId, amountToHeal)(random, id)
    }
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ characterBaseUseChecks(useData.firstAsCharacterId)
}
