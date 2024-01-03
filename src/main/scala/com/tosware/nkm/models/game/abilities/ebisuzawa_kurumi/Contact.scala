package com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState
import spray.json.*

import scala.util.Random

object Contact extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Contact",
      abilityType = AbilityType.Passive,
      description =
        """Deal {damage} physical damage after you hit a character with a basic attack or an ability.
          |This effect can be applied only once per character.
          |""".stripMargin,
    )
  val hitCharacterIdsKey: String = "hitCharacterIds"
}

case class Contact(
    abilityId: AbilityId,
    parentCharacterId: CharacterId,
) extends Ability(abilityId) with GameEventListener {
  import Contact.*
  override val metadata: AbilityMetadata = Contact.metadata
  private def hitCharacterIds(implicit gameState: GameState): Set[CharacterId] =
    state.variables.get(hitCharacterIdsKey)
      .map(_.parseJson.convertTo[Set[CharacterId]])
      .getOrElse(Set.empty)
  def hitCharacter(characterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    val damage = Damage(DamageType.Physical, metadata.variables("damage"))

    val ngs = gameState
      .setAbilityVariable(id, hitCharacterIdsKey, (hitCharacterIds + characterId).toJson.toString)

    hitAndDamageCharacter(characterId, damage)(random, ngs)
  }
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterBasicAttacked(_, characterId, targetCharacterId) =>
        if (characterId != parentCharacterId) return gameState
        if (hitCharacterIds.contains(targetCharacterId)) return gameState
        hitCharacter(targetCharacterId)
      case GameEvent.AbilityHitCharacter(_, abilityId, targetCharacterId) =>
        if (hitCharacterIds.contains(targetCharacterId)) return gameState
        val ability = gameState.abilityById(abilityId)
        if (ability.parentCharacter.id != parentCharacterId) return gameState
        hitCharacter(targetCharacterId)
      case _ => gameState
    }
}
