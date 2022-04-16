package com.tosware.NKM.models.game

case class Player(name: String,
                  characters: List[NKMCharacter] = List(),
                  victoryStatus: VictoryStatus = VictoryStatus.Pending)
