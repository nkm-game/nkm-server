package com.tosware.NKM.models.game

final case class PlaceCharacterRequest(gameId: String, hexCoordinates: HexCoordinates, characterId: String)
final case class MoveCharacterRequest(gameId: String, path: List[HexCoordinates], characterId: String)
