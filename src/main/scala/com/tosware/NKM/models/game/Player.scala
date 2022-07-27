package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.Player._

object Player {
  type PlayerId = String
}

case class Player(name: PlayerId,
                  characters: Set[NKMCharacter] = Set.empty,
                  victoryStatus: VictoryStatus = VictoryStatus.Pending,
                  isHost: Boolean = false,
                 ) {
  def id: PlayerId = name
  def characterIds: Set[CharacterId] = characters.map(_.id)

  def toView: PlayerView = PlayerView(
    name = name,
    characters = characters.map(_.toView),
    victoryStatus = victoryStatus,
    isHost = isHost,
  )
}

case class PlayerView
(
  name: PlayerId,
  characters: Set[NKMCharacterView],
  victoryStatus: VictoryStatus,
  isHost: Boolean,
)
