package com.tosware.nkm.models.game.abilities.dekomori_sanae

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object WickedEyesServant {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wicked Eyes Servant",
      abilityType = AbilityType.Passive,
      description =
        """Character gains <span style="color: blue;">{baseDamageBonus}</span> true damage on every attack and ability, if there is a character on map that has more AD or Rikka Takanashi.
          |If character kills an enemy, gain permanently 1 bonus true damage on this ability.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.dekomori_sanae.wickedEyesServant"),
    )
}

case class WickedEyesServant(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata: AbilityMetadata = WickedEyesServant.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterDied(_, _, _, causedById, characterId) =>
        if(parentCharacter.isFriendForC(characterId))
          return gameState

        val causedByCharacterOpt = gameState.backtrackCauseToCharacterId(causedById)
        if(!causedByCharacterOpt.contains(parentCharacter.id))
          return gameState

        gameState
      case _ =>
        gameState
    }
  }
}
