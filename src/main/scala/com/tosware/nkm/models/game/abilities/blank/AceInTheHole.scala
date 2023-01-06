package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.FreeAbility

import scala.util.Random

object AceInTheHole {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Ace In The Hole",
      abilityType = AbilityType.Passive,
      description = """If Character takes damage equal to more than {maxHpPercent}% of their maximum HP during the turn of one character, they will be able to use one of their abilities on their next move, regardless of its CD.
                      |It does not affect the actual ability CD count.""".stripMargin,
      variables = NkmConf.extract("abilities.blank.aceInTheHole"),
      relatedEffectIds = Seq(FreeAbility.metadata.id),
    )
}

case class AceInTheHole(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata = AceInTheHole.metadata

  private def getDamageThisTurn()(implicit gameState: GameState): Int = {
    gameState.gameLog.events
      .inTurn(gameState.turn.number)
      .ofType[GameEvent.CharacterDamaged]
      .ofCharacter(parentCharacterId)
      .map(_.damage.amount).sum
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterDamaged(_, characterId, _) =>
        if(characterId != parentCharacterId) return gameState
        if(getDamageThisTurn() < parentCharacter.state.maxHealthPoints * metadata.variables("maxHpPercent") / 100)
          gameState
        else gameState.addEffect(parentCharacterId, FreeAbility(parentCharacterId, 1))(random, id)
      case _ => gameState
    }
  }
}
