package com.tosware.NKM.models.lobby

import com.tosware.NKM.models.game.PickType

final case class LobbyCreationRequest(name: String)

final case class LobbyJoinRequest(lobbyId: String)

final case class LobbyLeaveRequest(lobbyId: String)

case class SetHexMapNameRequest(lobbyId: String, hexMapName: String)

case class SetNumberOfBansRequest(lobbyId: String, numberOfBans: Int)

case class SetNumberOfCharactersPerPlayerRequest(lobbyId: String, charactersPerPlayer: Int)

case class SetPickTypeRequest(lobbyId: String, pickType: PickType)

final case class StartGameRequest(lobbyId: String)
