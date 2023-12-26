package com.tosware.nkm.models.game.abilities.llenn

import com.tosware.nkm.*
import com.tosware.nkm.models.game.abilities.llenn.RunItDown.movesLeftKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState
import spray.json.*

import scala.util.Random

object RunItDown extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Run It Down",
      abilityType = AbilityType.Ultimate,
      description =
        """Move up to three times this turn.
          |After each move you can use a basic attack.""".stripMargin,
    )
  val movesLeftKey: String = "movesLeft"
}

case class RunItDown(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with Usable
    with GameEventListener {
  override val metadata: AbilityMetadata = RunItDown.metadata
  private def movesLeft(implicit gameState: GameState): Int =
    state.variables.get(movesLeftKey)
      .map(_.parseJson.convertTo[Int])
      .getOrElse(0)
  private def setMovesLeft(value: Int)(implicit random: Random, gameState: GameState): GameState =
    gameState.setAbilityVariable(id, movesLeftKey, value.toJson.toString)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    setMovesLeft(3)
      .refreshBasicMove(parentCharacterId)(random, id)
      .refreshBasicAttack(parentCharacterId)(random, id)
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterBasicMoved(_, _, _, _, characterId, _) =>
        if (characterId != parentCharacterId) return gameState
        if (movesLeft <= 0) return gameState
        val ngs = setMovesLeft(movesLeft - 1)
          .refreshBasicAttack(parentCharacterId)(random, id)
        if (movesLeft(ngs) <= 0) return ngs
        ngs.refreshBasicMove(parentCharacterId)(random, id)
      case GameEvent.TurnFinished(_, _, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if (characterIdThatTookAction != parentCharacterId) return gameState
        if (movesLeft <= 0) return gameState
        setMovesLeft(0)
      case _ => gameState
    }

}
