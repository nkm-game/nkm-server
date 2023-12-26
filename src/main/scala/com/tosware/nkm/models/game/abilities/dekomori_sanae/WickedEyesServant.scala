package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.dekomori_sanae.WickedEyesServant.damageBonusKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object WickedEyesServant extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wicked Eyes Servant",
      abilityType = AbilityType.Passive,
      description =
        """Deal <span style="color: deepskyblue;">{baseDamageBonus}</span> true damage after every damage you deal.
          |This effect is active only if there is a character on the map that has more AD or Rikka Takanashi.
          |After you kill an enemy, gain permanently 1 bonus true damage on this ability.
          |""".stripMargin,
    )
  val damageBonusKey = "damageBonus"
}

case class WickedEyesServant(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = WickedEyesServant.metadata
  def damageBonus(implicit gameState: GameState): Int =
    state.variables
      .get(damageBonusKey).map(_.toInt)
      .getOrElse(metadata.variables("baseDamageBonus"))
  private def isActive(implicit gameState: GameState): Boolean =
    gameState.characters
      .filter(_.isOnMap)
      .filterNot(_.id == parentCharacterId)
      .exists(c => c.state.name == "Rikka Takanashi" || c.state.attackPoints > parentCharacter.state.attackPoints)
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDied(_, _, _, causedById, characterId) =>
        if (parentCharacter.isFriendForC(characterId))
          return gameState
        val causedByCharacterOpt = gameState.backtrackCauseToCharacterId(causedById)
        if (!causedByCharacterOpt.contains(parentCharacter.id))
          return gameState
        gameState
          .setAbilityVariable(id, damageBonusKey, (damageBonus + 1).toString)
      case GameEvent.CharacterDamaged(_, _, _, causedById, characterId, _) =>
        if (!isActive)
          return gameState
        if (causedById == id)
          return gameState
        val causedByCharacterOpt = gameState.backtrackCauseToCharacterId(causedById)
        if (!causedByCharacterOpt.contains(parentCharacter.id))
          return gameState
        gameState
          .damageCharacter(characterId, Damage(DamageType.True, damageBonus))(random, id)
      case _ =>
        gameState
    }
}
