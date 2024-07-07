package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.HexMap
import com.tosware.nkm.models.game.pick.PickType

case class GameStartDependencies(
    players: Seq[Player],
    hexMap: HexMap,
    pickType: PickType,
    gameMode: GameMode = GameMode.Deathmatch,
    numberOfBansPerPlayer: Int,
    numberOfCharactersPerPlayer: Int,
    charactersMetadata: Set[CharacterMetadata],
    clockConfig: ClockConfig,
)
