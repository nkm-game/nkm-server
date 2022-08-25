package com.tosware.NKM.models.game.abilities.roronoa_zoro

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._
import com.tosware.NKM.models.game.hex.{HexCell, SearchFlag}

import scala.util.Random

object OgreCutter {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Ogre Cutter",
      alternateName = "鬼斬り",
      abilityType = AbilityType.Normal,
      description = "Character deals basic damage to selected target in range and teleports 2 tiles behind it.",
      cooldown = NKMConf.int("abilities.roronoaZoro.ogreCutter.cooldown"),
      range = NKMConf.int("abilities.roronoaZoro.ogreCutter.range"),
    )
}

case class OgreCutter(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = OgreCutter.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.fold(Set.empty[HexCell])(c => c.getArea(
      metadata.range,
      Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
      friendlyPlayerIdOpt = Some(parentCharacter.owner.id),
    )).toCoords

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    gameState
      .abilityHitCharacter(id, target)
      .basicAttack(parentCharacterId, target)
      .teleportCharacter(parentCharacterId, ???)(random, id)
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCharacter: NKMCharacter = gameState.characterById(target).get

    super.useChecks ++ Seq(
      UseCheck.TargetIsEnemy,
//      targetCharacter.state.effects.exists(_.metadata.effectType == CharacterEffectType.Negative) ->
//        "Target character does not have any negative effects.",
    )
  }
}
