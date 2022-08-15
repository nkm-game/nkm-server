package com.tosware.NKM.models.game.abilities.hecate

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.softwaremill.quicklens._
import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.abilities.hecate.MasterThrone.healthPercent

object MasterThrone {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Master Throne",
      alternateName = "頂の座",
      abilityType = AbilityType.Passive,
      description =
        """Character can collect Life Energy using base attacks or Normal ability.
          |Life Energy can be collected only once per character.
          |""".stripMargin,
    )
  val healthPercent = NKMConf.int("abilities.hecate.masterThrone.healthPercent")
}

case class MasterThrone
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
  collectedCharacterIds: Set[CharacterId] = Set.empty,
  collectedEnergy: Int = 0
) extends Ability(abilityId) with GameEventListener {
  override val metadata = MasterThrone.metadata
  override val state = AbilityState(parentCharacterId)

  def collectEnergy(characterId: CharacterId)(implicit gameState: GameState): MasterThrone = {
    val energy = (gameState.characterById(characterId).get.state.maxHealthPoints * (healthPercent / 100f)).toInt

    this
      .modify(_.collectedCharacterIds).using(cs => cs + characterId)
      .modify(_.collectedEnergy).using(e => e + energy)
  }

  def reset()(implicit gameState: GameState): MasterThrone =
    this
      .modify(_.collectedCharacterIds).setTo(Set.empty)
      .modify(_.collectedEnergy).setTo(0)

  override def onEvent(e: GameEvent.GameEvent)(implicit gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterBasicAttacked(characterId, targetCharacterId) =>
        if(characterId != parentCharacterId) return gameState
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        gameState.updateAbility(id, collectEnergy(characterId))
      case GameEvent.AbilityHitCharacter(abilityId, targetCharacterId) =>
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        val ability = gameState.abilityById(abilityId).get
        if(ability.parentCharacter.id != parentCharacterId) return gameState
        if(ability.metadata.abilityType != AbilityType.Normal) return gameState
        gameState.updateAbility(id, collectEnergy(targetCharacterId))
      case _ => gameState
    }
  }
}
