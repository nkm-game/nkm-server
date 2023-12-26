package com.tosware.nkm.models.game.ability

import com.tosware.nkm.*
import com.tosware.nkm.models.CommandResponse.CommandResponse
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

trait BasicAttackOverride {
  def basicAttackCells(implicit gameState: GameState): Set[HexCoordinates]
  def basicAttackTargets(implicit gameState: GameState): Set[HexCoordinates]
  def basicAttack(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState
}

trait BasicMoveOverride {
  def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState
}

trait Usable { this: Ability =>
  def use(useData: UseData = UseData())(implicit random: Random, gameState: GameState): GameState
  def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    baseUseChecks
  final def canBeUsed(implicit useData: UseData, gameState: GameState): CommandResponse =
    models.UseCheck.canBeUsed(useChecks)
}
