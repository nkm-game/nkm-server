package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.Player._

object Player {
  type PlayerId = String
}

case class Player(name: PlayerId,
                  characters: List[NKMCharacter] = List(),
                  victoryStatus: VictoryStatus = VictoryStatus.Pending)
