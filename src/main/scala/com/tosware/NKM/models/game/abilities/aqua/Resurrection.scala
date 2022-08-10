package com.tosware.NKM.models.game.abilities.aqua

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils._

object Resurrection {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Resurrection",
      abilityType = AbilityType.Ultimate,
      description = "*Character* resurrects allied character, that died maximally one phase before.\nResurrected character respawns with half base HP on selected spawn point.",
      cooldown = 8,
    )
}

case class ResurrectionUseData(characterId: CharacterId)

case class Resurrection(parentCharacterId: CharacterId) extends Ability with UsableOnCoordinates {
  override val metadata = Resurrection.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.getSpawnPointsFor(parentCharacter.owner.id).map(_.coordinates)

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEmpty

  override def use(target: HexCoordinates, useData: UseData)(implicit gameState: GameState) = {
    val targetCharacter = gameState.characterById(useData.data).get

    gameState
      .setHp(targetCharacter.id, targetCharacter.state.maxHealthPoints / 2)(id)
      .placeCharacter(target, targetCharacter.id)(id)
  }

  override def useChecks(implicit target: HexCoordinates, useData: UseData, gameState: GameState): Set[(Boolean, CharacterId)] = {
    val targetCharacter: NKMCharacter = gameState.characterById(useData.data).get

    super.useChecks ++ Seq(
      UseCheck.TargetIsFriendlySpawn,
      UseCheck.TargetIsFriend(targetCharacter.id, useData, gameState),
      UseCheck.TargetIsFreeToStand,
      targetCharacter.isDead -> "Target character is not dead.",
      gameState.gameLog.events.ofType[GameEvent.CharacterDied]
        .filter(_.characterId == targetCharacter.id)
        .exists(e => gameState.phase.number - e.phase.number < 2) -> "Target character has not died in the last 2 phases.",
    )
  }
}
