package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import spray.json._

import scala.util.Random

object MasterThrone {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Master Throne",
      alternateName = "頂の座",
      abilityType = AbilityType.Passive,
      description =
        """Character can gather Life Energy using base attacks or Normal abilities, collecting {healthPercent}% of target's max HP.
          |Life Energy can be collected only once per character.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.hecate.masterThrone"),
    )
  val collectedCharacterIdsKey: String = "collectedCharacterIds"
  val collectedEnergyKey: String = "collectedEnergy"
}

case class MasterThrone
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  import MasterThrone._

  override val metadata = MasterThrone.metadata

  def collectedCharacterIds(implicit gameState: GameState): Set[CharacterId] =
    state.variables.get(collectedCharacterIdsKey)
      .map(_.parseJson.convertTo[Set[CharacterId]])
      .getOrElse(Set.empty)

  def collectedEnergy(implicit gameState: GameState): Int =
    state.variables.get(collectedEnergyKey)
      .map(_.parseJson.convertTo[Int])
      .getOrElse(0)

  def collectEnergy(characterId: CharacterId)(implicit gameState: GameState): GameState = {
    val energy = (gameState.characterById(characterId).state.maxHealthPoints * (metadata.variables("healthPercent") / 100f)).toInt

    gameState
      .setAbilityVariable(id, collectedCharacterIdsKey, (collectedCharacterIds + characterId).toJson.toString)
      .setAbilityVariable(id, collectedEnergyKey, (collectedEnergy + energy).toJson.toString)
  }

  private def reset()(implicit gameState: GameState): GameState =
    gameState
      .setAbilityVariable(id, collectedCharacterIdsKey, Set.empty[CharacterId].toJson.toString)
      .setAbilityVariable(id, collectedEnergyKey, 0.toJson.toString)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterBasicAttacked(_, characterId, targetCharacterId) =>
        if(characterId != parentCharacterId) return gameState
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        collectEnergy(characterId)(gameState)
      case GameEvent.AbilityHitCharacter(_, abilityId, targetCharacterId) =>
        if(collectedCharacterIds.contains(targetCharacterId)) return gameState
        val ability = gameState.abilityById(abilityId)
        if(ability.parentCharacter.id != parentCharacterId) return gameState
        if(ability.metadata.abilityType != AbilityType.Normal) return gameState
        collectEnergy(targetCharacterId)(gameState)
      case GameEvent.AbilityUseFinished(_, abilityId) =>
        val ability = gameState.abilityById(abilityId)
        if(ability.parentCharacter.id != parentCharacterId) return gameState
        if(ability.metadata.id == PowerOfExistence.metadata.id) reset()
        else gameState

      case _ => gameState
    }
  }
}
