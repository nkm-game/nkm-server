package com.tosware.NKM.models.lobby.ws

import com.tosware.NKM.models.game.{ClockConfig, PickType}

object LobbyRequest {
  sealed trait LobbyRequest
  final case class Auth(token: String) extends LobbyRequest
  final case class Observe(lobbyId: String) extends LobbyRequest
  final case class GetLobby(lobbyId: String) extends LobbyRequest
  final case class LobbyCreation(name: String) extends LobbyRequest
  final case class LobbyJoin(lobbyId: String) extends LobbyRequest
  final case class LobbyLeave(lobbyId: String) extends LobbyRequest
  final case class SetHexMapName(lobbyId: String, hexMapName: String) extends LobbyRequest
  final case class SetNumberOfBans(lobbyId: String, numberOfBans: Int) extends LobbyRequest
  final case class SetNumberOfCharactersPerPlayer(lobbyId: String, charactersPerPlayer: Int) extends LobbyRequest
  final case class SetPickType(lobbyId: String, pickType: PickType) extends LobbyRequest
  final case class SetLobbyName(lobbyId: String, newName: String) extends LobbyRequest
  final case class SetClockConfig(lobbyId: String, newConfig: ClockConfig) extends LobbyRequest
  final case class StartGame(lobbyId: String) extends LobbyRequest
}
