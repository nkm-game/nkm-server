package com.tosware.nkm.models.game.event

import com.tosware.nkm.NkmUtils
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.event.GameEvent.{CharacterTookAction, GameEvent}


trait GameLogLike extends NkmUtils {
  val events: Seq[GameEvent]
  def characterThatTookActionInTurn(turnNumber: Int): Option[CharacterId] =
    events
      .ofType[CharacterTookAction]
      .inTurn(turnNumber)
      .headOption
      .map(_.characterId)
}
