package com.tosware.nkm.models.game.abilities.sinon

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.AttackType
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.{HexCoordinates, SearchFlag}

import scala.util.Random

object SnipersSight extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Sniper's Sight",
      abilityType = AbilityType.Passive,
      description = "Basic attack range is round.",
    )
}

case class SnipersSight(
    abilityId: AbilityId,
    parentCharacterId: CharacterId,
) extends Ability(abilityId) with BasicAttackOverride {
  override val metadata: AbilityMetadata = SnipersSight.metadata
  override def basicAttackCells(implicit gameState: GameState): Set[HexCoordinates] =
    parentCellOpt match {
      case Some(parentCell) =>
        parentCharacter.state.attackType match {
          case AttackType.Melee =>
            parentCell.getArea(
              parentCharacter.state.basicAttackRange,
              Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StopAfterFriends),
              friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
            ).toCoords - parentCell.coordinates
          case AttackType.Ranged =>
            parentCell.getArea(parentCharacter.state.basicAttackRange).toCoords - parentCell.coordinates
        }
      case None => Set.empty
    }
  override def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates] =
    parentCharacter.defaultBasicAttackTargets
  override def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    parentCharacter.defaultBasicAttack(targetCharacterId)
}
