package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import spray.json.*

import scala.util.Random

object MasterThrone extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Master Throne",
      alternateName = "頂の座 (Itadaki no Kura)",
      abilityType = AbilityType.Passive,
      description =
        """Character can gather Life Energy using base attacks or Normal abilities, collecting {healthPercent}% of target's max HP.
          |Life Energy can be collected only once per character.
          |""".stripMargin,

    )
  val collectedCharacterIdsKey: String = "collectedCharacterIds"
  val collectedEnergyKey: String = "collectedEnergy"
}

case class MasterThrone
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  import MasterThrone.*

  override val metadata = MasterThrone.metadata

  def collectedCharacterIds(implicit gameState: GameState): Set[CharacterId] =
    state.variables.get(collectedCharacterIdsKey)
      .map(_.parseJson.convertTo[Set[CharacterId]])
      .getOrElse(Set.empty)

  def collectedEnergy(implicit gameState: GameState): Int =
    state.variables.get(collectedEnergyKey)
      .map(_.parseJson.convertTo[Int])
      .getOrElse(0)

  def collectEnergy(characterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    val energy = (gameState.characterById(characterId).state.maxHealthPoints * (metadata.variables("healthPercent") / 100f)).toInt

    gameState
      .setAbilityVariable(id, collectedCharacterIdsKey, (collectedCharacterIds + characterId).toJson.toString)
      .setAbilityVariable(id, collectedEnergyKey, (collectedEnergy + energy).toJson.toString)
  }

  private def reset()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .setAbilityVariable(id, collectedCharacterIdsKey, Set.empty[CharacterId].toJson.toString)
      .setAbilityVariable(id, collectedEnergyKey, 0.toJson.toString)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterBasicAttacked(_, _, _, _, characterId, targetCharacterId) =>
        if(characterId != parentCharacterId) return gameState
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        collectEnergy(characterId)
      case GameEvent.AbilityHitCharacter(_, _, _, _, abilityId, targetCharacterId) =>
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        val ability = gameState.abilityById(abilityId)
        if(ability.parentCharacter.id != parentCharacterId) return gameState
        if(ability.metadata.abilityType != AbilityType.Normal) return gameState
        collectEnergy(targetCharacterId)
      case GameEvent.AbilityUseFinished(_, _, _, _, abilityId) =>
        val ability = gameState.abilityById(abilityId)
        if(ability.parentCharacter.id != parentCharacterId) return gameState
        if(ability.metadata.id == PowerOfExistence.metadata.id) reset()
        else gameState

      case _ => gameState
    }
  }
}
