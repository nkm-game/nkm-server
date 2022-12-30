package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.GameEvent.{CharacterTookAction, GameEvent}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexUtils._

case class GameLog(events: Seq[GameEvent]) {
  def characterThatTookActionInTurn(turnNumber: Int): Option[CharacterId] =
    events
      .ofType[CharacterTookAction]
      .inTurn(turnNumber)
      .headOption
      .map(_.characterId)

}

