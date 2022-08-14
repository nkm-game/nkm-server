package com.tosware.NKM.models.game.abilities.hecate

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.softwaremill.quicklens._
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
  val healthPercent = 40
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

  def collectEnergy(characterId: CharacterId, energy: Int): MasterThrone =
    this
      .modify(_.collectedCharacterIds).using(cs => cs + characterId)
      .modify(_.collectedEnergy).using(e => e + energy)

  override def onEvent(e: GameEvent.GameEvent)(implicit gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterBasicAttacked(characterId, targetCharacterId) =>
        if(characterId != parentCharacterId) return gameState
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        val energy = (gameState.characterById(targetCharacterId).get.state.maxHealthPoints * (healthPercent / 100f)).toInt
        gameState.updateAbility(id, collectEnergy(characterId, energy))
      case _ => gameState
    }
  }
}
