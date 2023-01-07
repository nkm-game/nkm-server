package com.tosware.nkm.models.game.abilities.llenn

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.llenn.RunItDown.movesLeftKey
import spray.json._

import scala.util.Random

object RunItDown {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Run It Down",
      abilityType = AbilityType.Ultimate,
      description =
        """Character can move three times this turn.
          |After each move they can use basic attack.""".stripMargin,
      variables = NkmConf.extract("abilities.llenn.runItDown"),
    )
  val movesLeftKey: String = "movesLeft"
}

case class RunItDown(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableWithoutTarget
    with GameEventListener
{
  override val metadata = RunItDown.metadata

  def movesLeft(implicit gameState: GameState): Int =
    state.variables.get(movesLeftKey)
      .map(_.parseJson.convertTo[Int])
      .getOrElse(0)

  private def setMovesLeft(value: Int)(implicit random: Random, gameState: GameState): GameState =
    gameState.setAbilityVariable(id, movesLeftKey, value.toJson.toString)


  override def use()(implicit random: Random, gameState: GameState): GameState =
    setMovesLeft(3)
    .refreshBasicMove(parentCharacterId)(random, id)
    .refreshBasicAttack(parentCharacterId)(random, id)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterBasicMoved(_, characterId, _) =>
        if(characterId != parentCharacterId) return gameState
        if(movesLeft <= 0) return gameState

        val ngs = setMovesLeft(movesLeft - 1)
          .refreshBasicAttack(parentCharacterId)(random, id)

        if (movesLeft(ngs) <= 0) return ngs

        ngs.refreshBasicMove(parentCharacterId)(random, id)

      case GameEvent.TurnFinished(_) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if (characterIdThatTookAction != parentCharacterId) return gameState
        if(movesLeft <= 0) return gameState
        setMovesLeft(0)
      case _ => gameState
    }

  }
}
