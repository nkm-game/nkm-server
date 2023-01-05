package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Resurrection {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Resurrection",
      abilityType = AbilityType.Ultimate,
      description =
        """Character resurrects allied characterOpt, that died max. one phase before.
          |Resurrected characterOpt respawns with half base HP on selected spawn point.""".stripMargin,
      variables = NkmConf.extract("abilities.aqua.resurrection"),
    )
}

case class Resurrection(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCoordinates {
  override val metadata = Resurrection.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.getSpawnPointsFor(parentCharacter.owner.id).map(_.coordinates)

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEmpty

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val targetCharacter = gameState.characterById(useData.data)

    gameState
      .setHp(targetCharacter.id, targetCharacter.state.maxHealthPoints / 2)(random, id)
      .placeCharacter(target, targetCharacter.id)(random, id)
  }

  override def useChecks(implicit target: HexCoordinates, useData: UseData, gameState: GameState): Set[(Boolean, CharacterId)] = {
    val targetCharacter: NkmCharacter = gameState.characterById(useData.data)

    super.useChecks ++ Seq(
      UseCheck.TargetCharacter.IsFriend(targetCharacter.id, useData, gameState),
      UseCheck.TargetCoordinates.IsFriendlySpawn,
      UseCheck.TargetCoordinates.IsFreeToStand,
      targetCharacter.isDead -> "Target characterOpt is not dead.",
      gameState.gameLog.events.ofType[GameEvent.CharacterDied]
        .ofCharacter(targetCharacter.id)
        .exists(e => gameState.phase.number - e.phase.number < 2) -> "Target characterOpt has not died in the last 2 phases.",
    )
  }
}
