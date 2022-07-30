package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.hex.HexMap

case class GameStartDependencies(
                                  players: Seq[Player],
                                  hexMap: HexMap,
                                  pickType: PickType,
                                  numberOfBansPerPlayer: Int,
                                  numberOfCharactersPerPlayer: Int,
                                  charactersMetadata: Set[NKMCharacterMetadata],
                                  clockConfig: ClockConfig,
                                )
