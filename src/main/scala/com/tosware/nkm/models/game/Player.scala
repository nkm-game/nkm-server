package com.tosware.nkm.models.game

import com.tosware.nkm.*

case class Player(
    name: PlayerId,
    characterIds: Set[CharacterId] = Set.empty,
    victoryStatus: VictoryStatus = VictoryStatus.Pending,
    isHost: Boolean = false,
    points: Int = 0,
) {
  def id: PlayerId = name
}
