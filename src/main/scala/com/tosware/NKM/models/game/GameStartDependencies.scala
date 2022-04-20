package com.tosware.NKM.models.game

case class GameStartDependencies(
                                  players: Seq[Player],
                                  hexMap: HexMap,
                                  pickType: PickType,
                                  numberOfBansPerPlayer: Int,
                                  numberOfCharactersPerPlayer: Int,
                                  charactersMetadata: Set[NKMCharacterMetadata],
                                )
