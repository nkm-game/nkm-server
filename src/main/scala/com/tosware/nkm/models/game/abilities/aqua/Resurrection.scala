package com.tosware.nkm.models.game.abilities.aqua

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Resurrection extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Resurrection",
      abilityType = AbilityType.Ultimate,
      description =
        """Resurrect an ally that died in the last {diedMaxInLastNPhases} phases (current phase counts).
          |Resurrected character respawns with half of base HP on selected spawn point.""".stripMargin,
      targetsMetadata = Seq(
        AbilityTargetMetadata(1 to 1, AbilityTargetType.DeadCharacter),
        AbilityTargetMetadata.SingleCoordinate,
      ),
    )
}

case class Resurrection(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = Resurrection.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.getSpawnPointsFor(parentCharacter.owner.id).map(_.coordinates)

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEmpty

  override def use(useData: UseData)(implicit
      random: Random,
      gameState: GameState,
  ): GameState = {
    val targetCoordinates = useData.firstAsCoordinates
    val targetCharacter = gameState.characterById(useData.secondAsCharacterId)

    gameState
      .setHp(targetCharacter.id, targetCharacter.state.maxHealthPoints / 2)(random, id)
      .placeCharacter(targetCoordinates, targetCharacter.id)(random, id)
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[(Boolean, CharacterId)] = {
    val targetCoordinates = useData.firstAsCoordinates
    val targetCharacterId = useData.secondAsCharacterId

    super.useChecks ++ coordinatesBaseUseChecks(targetCoordinates) ++ Seq(
      UseCheck.Character.IsFriend(targetCharacterId),
      UseCheck.Character.IsDead(targetCharacterId),
      UseCheck.Coordinates.IsFriendlySpawn(targetCoordinates),
      UseCheck.Coordinates.IsFreeToStand(targetCoordinates),
      gameState.gameLog.events.ofType[GameEvent.CharacterDied]
        .ofCharacter(targetCharacterId)
        .exists(e =>
          gameState.phase.number - e.context.phase.number < metadata.variables("diedMaxInLastNPhases")
        ) -> s"Target character has not died in the last ${metadata.variables("diedMaxInLastNPhases")} phases.",
    )
  }
}
