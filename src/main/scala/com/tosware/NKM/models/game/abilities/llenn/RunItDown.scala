package com.tosware.NKM.models.game.abilities.llenn

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.GameEvent.CharacterTookAction
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._

import scala.util.Random

object RunItDown {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Run It Down",
      abilityType = AbilityType.Ultimate,
      description =
        """Character can move three times this turn.
          |After each move they can use basic attack.""".stripMargin,
      cooldown = NKMConf.int("abilities.llenn.runItDown.cooldown"),
    )
}

case class RunItDown(abilityId: AbilityId, parentCharacterId: CharacterId, movesLeft: Int = 0)
  extends Ability(abilityId)
    with UsableWithoutTarget
    with GameEventListener
{
  override val metadata = RunItDown.metadata
  override val state = AbilityState(parentCharacterId)

  private def setMovesLeft(value: Int): RunItDown =
    copy(movesLeft = value)

  override def use()(implicit random: Random,  gameState: GameState): GameState =
    gameState.updateAbility(id, setMovesLeft(3))
      .refreshBasicMove(parentCharacterId)(random, id)
      .refreshBasicAttack(parentCharacterId)(random, id)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterBasicMoved(_, characterId, _) =>
        if(characterId == parentCharacterId && movesLeft > 0) {
          if(movesLeft == 1) {
            gameState.updateAbility(id, setMovesLeft(movesLeft - 1))
              .refreshBasicAttack(parentCharacterId)(random, id)
          } else {
            gameState.updateAbility(id, setMovesLeft(movesLeft - 1))
              .refreshBasicMove(parentCharacterId)(random, id)
              .refreshBasicAttack(parentCharacterId)(random, id)
          }
        } else gameState
      case GameEvent.TurnFinished(_) =>
        val characterIdThatTookAction =
          gameState.gameLog.events
          .ofType[CharacterTookAction]
          .inTurn(e.turn.number)
          .head
          .characterId
        if(characterIdThatTookAction == parentCharacterId && movesLeft > 0) {
          gameState.updateAbility(id, setMovesLeft(0))
        } else gameState
      case _ => gameState
    }

  }
}
