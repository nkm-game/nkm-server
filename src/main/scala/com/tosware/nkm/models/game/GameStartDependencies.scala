package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.hex.HexMap

case class GameStartDependencies(
                                  players: Seq[Player],
                                  hexMap: HexMap,
                                  pickType: PickType,
                                  numberOfBansPerPlayer: Int,
                                  numberOfCharactersPerPlayer: Int,
                                  charactersMetadata: Set[CharacterMetadata],
                                  clockConfig: ClockConfig,
                                )
