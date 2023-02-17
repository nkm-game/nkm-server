package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.GameEvent.{CharacterTookAction, GameEvent}
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.NkmUtils._
import com.tosware.nkm.models.game.Player.PlayerId

trait GameLogLike {
  val events: Seq[GameEvent]
  def characterThatTookActionInTurn(turnNumber: Int): Option[CharacterId] =
    events
      .ofType[CharacterTookAction]
      .inTurn(turnNumber)
      .headOption
      .map(_.characterId)
}

case class GameLog(events: Seq[GameEvent]) extends GameLogLike {
  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): GameLogView = {
    val eventsFiltered = events.filterNot(e => gameState.hiddenEidsFor(forPlayerOpt).contains(e.id))
    GameLogView(eventsFiltered)
  }
}

case class GameLogView(events: Seq[GameEvent]) extends GameLogLike
