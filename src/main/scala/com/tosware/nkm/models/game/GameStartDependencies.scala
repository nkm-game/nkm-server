package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.{HexCell, HexMap}
import com.tosware.nkm.models.game.pick.PickType

case class GameStartDependencies(
                                  players: Seq[Player],
                                  hexMap: HexMap[HexCell],
                                  pickType: PickType,
                                  numberOfBansPerPlayer: Int,
                                  numberOfCharactersPerPlayer: Int,
                                  charactersMetadata: Set[CharacterMetadata],
                                  clockConfig: ClockConfig,
                                )
