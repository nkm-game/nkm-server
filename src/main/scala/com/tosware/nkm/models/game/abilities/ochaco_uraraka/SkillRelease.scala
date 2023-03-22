package com.tosware.nkm.models.game.abilities.ochaco_uraraka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character_effect.CharacterEffectName
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object SkillRelease {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Skill Release",
      alternateName = "解除 (Kaijo)",
      abilityType = AbilityType.Ultimate,
      description =
        """Character releases their ability, and all characters lose Zero Gravity effect.
          |Enemies that lost Zero Gravity are stunned for {stunDuration}t.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.ochaco_uraraka.skillRelease"),
    )
}

case class SkillRelease(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with Usable {
  override val metadata: AbilityMetadata = SkillRelease.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    gameState
      .effects
      .filter(_.state.name == CharacterEffectName.ZeroGravity)
      .flatMap(_.parentCharacter.parentCell)
      .map(_.coordinates)

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val zeroGravityEffects =
      gameState
        .effects
        .filter(_.state.name == CharacterEffectName.ZeroGravity)

    val eidsToRemove: Set[CharacterEffectId] = zeroGravityEffects.map(_.id)
    val characterIdsToStun =
      zeroGravityEffects
        .map(_.parentCharacter)
        .filter(_.isEnemyForC(parentCharacterId))
        .map(_.id)
        .toSeq

    val effectsRemovedGs =
      gameState
        .removeEffects(eidsToRemove.toSeq)(random, id)

    characterIdsToStun.foldLeft(effectsRemovedGs)((acc, cid) => {
      val stunEffect = effects.Stun(randomUUID(), metadata.variables("stunDuration"))
      acc.addEffect(cid,stunEffect)(random, id)
    })
  }
}
