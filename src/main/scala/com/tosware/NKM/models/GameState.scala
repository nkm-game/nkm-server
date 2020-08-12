package com.tosware.NKM.models

case class GameState(hexMap: HexMap,
                     charactersOutsideMap: List[NKMCharacter] = List(),
                     phase: Phase = Phase(0),
                     turn: Turn = Turn(0),
                     players: List[Player] = List())
