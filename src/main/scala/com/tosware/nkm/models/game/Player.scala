package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.Player._

object Player {
  type PlayerId = String
}

case class Player(name: PlayerId,
                  characterIds: Set[CharacterId] = Set.empty,
                  victoryStatus: VictoryStatus = VictoryStatus.Pending,
                  isHost: Boolean = false,
                 ) {
  def id: PlayerId = name
}
