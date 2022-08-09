package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.Player._

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
