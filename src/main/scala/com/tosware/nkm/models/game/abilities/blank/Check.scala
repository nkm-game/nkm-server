package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.{Disarm, HasToTakeAction}

import scala.util.Random

object Check {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Check",
      abilityType = AbilityType.Normal,
      description =
        """Character forces selected enemy character to take action.
          |It cannot use a basic attack this turn.""".stripMargin,
      variables = NkmConf.extract("abilities.blank.check"),
      relatedEffectIds = Seq(HasToTakeAction.metadata.id, Disarm.metadata.id),
    )
}

case class Check(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = Check.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.cells.toCoords

  override def targetsInRange(implicit gameState: GameState) = {
    rangeCellCoords
      .whereEnemiesOfC(parentCharacterId)
      .characters.filterNot(c => gameState.characterIdsThatTookActionThisPhase.contains(c.id))
      .flatMap(_.parentCell.map(_.coordinates))
  }

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    gameState
      .abilityHitCharacter(id, target)
      .addEffect(target, effects.Disarm(NkmUtils.randomUUID(), metadata.variables("disarmDuration")))(random, id)
      .addEffect(target, effects.HasToTakeAction(NkmUtils.randomUUID(), 1))(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsEnemy,
      !gameState.characterIdsThatTookActionThisPhase.contains(target) -> "This character already took action in this phase."
    )
  }
}
