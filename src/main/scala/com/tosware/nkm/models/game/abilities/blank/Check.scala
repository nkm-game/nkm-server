package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.{Disarm, HasToTakeAction}
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Check extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Check",
      abilityType = AbilityType.Normal,
      description = "Disarm enemy character for 1t and force them to take action in the next turn of their owner.",
      relatedEffectIds = Seq(HasToTakeAction.metadata.id, Disarm.metadata.id),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class Check(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId)
    with Usable {
  override val metadata: AbilityMetadata = Check.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords
      .whereSeenEnemiesOfC(parentCharacterId)
      .characters.filterNot(c => gameState.characterIdsThatTookActionThisPhase.contains(c.id))
      .flatMap(_.parentCellOpt.map(_.coordinates))
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    gameState
      .abilityHitCharacter(id, target)
      .addEffect(target, effects.Disarm(randomUUID(), metadata.variables("disarmDuration")))(random, id)
      .addEffect(target, effects.HasToTakeAction(randomUUID(), 1))(random, id)
  }
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val target = useData.firstAsCharacterId
    super.useChecks ++ characterBaseUseChecks(target) ++ Seq(
      UseCheck.Character.IsEnemy(target),
      !gameState.characterIdsThatTookActionThisPhase.contains(
        target
      ) -> "This character already took action in this phase.",
    )
  }
}
