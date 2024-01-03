package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.FreeAbility
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object AceInTheHole extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Ace In The Hole",
      abilityType = AbilityType.Passive,
      description =
        """After taking damage higher than {maxHpPercent}% of your max HP during the turn of one character, you will be able to use one ability for free in your next turn.
          |Actual cooldown of used ability is not affected.""".stripMargin,
      relatedEffectIds = Seq(FreeAbility.metadata.id),
    )
}

case class AceInTheHole(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = AceInTheHole.metadata

  private def getDamageThisTurn()(implicit gameState: GameState): Int =
    gameState.gameLog.events
      .inTurn(gameState.turn.number)
      .ofType[GameEvent.CharacterDamaged]
      .ofCharacter(parentCharacterId)
      .map(_.damageAmount).sum

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDamaged(_, characterId, _) =>
        if (characterId != parentCharacterId) return gameState
        if (getDamageThisTurn() < parentCharacter.state.maxHealthPoints * metadata.variables("maxHpPercent") / 100)
          gameState
        else gameState.addEffect(parentCharacterId, FreeAbility(parentCharacterId, 1))(random, id)
      case _ => gameState
    }
}
