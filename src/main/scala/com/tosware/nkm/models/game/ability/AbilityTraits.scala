package com.tosware.nkm.models.game.ability

import com.tosware.nkm.models.CommandResponse.CommandResponse
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.ability.Ability.UseCheck
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

trait UsableWithoutTarget { this: Ability =>
  def use()(implicit random: Random, gameState: GameState): GameState
  def useChecks(implicit gameState: GameState): Set[UseCheck] =
    baseUseChecks
  final def canBeUsed(implicit gameState: GameState): CommandResponse =
    _canBeUsed(useChecks)
}

trait UsableOnTarget[T] { this: Ability =>
  def use(target: T, useData: UseData = UseData())(implicit random: Random, gameState: GameState): GameState
  def useChecks(implicit target: T, useData: UseData, gameState: GameState): Set[UseCheck] =
    baseUseChecks
  final def canBeUsed(implicit target: T, useData: UseData, gameState: GameState): CommandResponse =
    _canBeUsed(useChecks)
}

trait UsableOnCoordinates extends UsableOnTarget[HexCoordinates] { this: Ability =>
  override def useChecks(implicit target: HexCoordinates, useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks + UseCheck.TargetCoordinates.InRange
}

trait UsableOnCharacter extends UsableOnTarget[CharacterId] { this: Ability =>
  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks + UseCheck.TargetCharacter.InRange
}
