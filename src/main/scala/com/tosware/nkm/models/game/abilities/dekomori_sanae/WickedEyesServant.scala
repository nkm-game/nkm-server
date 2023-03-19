package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.dekomori_sanae.WickedEyesServant.damageBonusKey
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object WickedEyesServant {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wicked Eyes Servant",
      abilityType = AbilityType.Passive,
      description =
        """Character deals <span style="color: deepskyblue;">{baseDamageBonus}</span> true damage on every attack and ability, if there is a character on map that has more AD or Rikka Takanashi.
          |If character kills an enemy, gain permanently 1 bonus true damage on this ability.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.dekomori_sanae.wickedEyesServant"),
    )
  val damageBonusKey = "damageBonus"
}

case class WickedEyesServant(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata: AbilityMetadata = WickedEyesServant.metadata

  def damageBonus(implicit gameState: GameState): Int =
    state.variables
      .get(damageBonusKey).map(_.toInt)
      .getOrElse(metadata.variables("baseDamageBonus"))

  def isActive(implicit gameState: GameState): Boolean =
    gameState.characters
      .filter(_.isOnMap)
      .filterNot(_.id == parentCharacterId)
      .exists(c => c.state.name == "Rikka Takanashi" || c.state.attackPoints > parentCharacter.state.attackPoints)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterDied(_, _, _, causedById, characterId) =>
        if(parentCharacter.isFriendForC(characterId))
          return gameState

        val causedByCharacterOpt = gameState.backtrackCauseToCharacterId(causedById)
        if(!causedByCharacterOpt.contains(parentCharacter.id))
          return gameState

        gameState
          .setAbilityVariable(id, damageBonusKey, (damageBonus + 1).toString)

      case GameEvent.CharacterDamaged(_, _, _, causedById, characterId, _) =>
        if(!isActive)
          return gameState

        if(causedById == id)
          return gameState

        val causedByCharacterOpt = gameState.backtrackCauseToCharacterId(causedById)
        if(!causedByCharacterOpt.contains(parentCharacter.id))
          return gameState

        gameState
          .damageCharacter(characterId, Damage(DamageType.True, damageBonus))(random, id)
      case _ =>
        gameState
    }
  }
}
