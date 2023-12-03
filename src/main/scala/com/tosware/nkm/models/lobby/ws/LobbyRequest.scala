package com.tosware.nkm.models.lobby.ws

import com.tosware.nkm.models.game.ClockConfig
import com.tosware.nkm.models.game.pick.PickType

sealed trait LobbyRequest
object LobbyRequest {
  final case class Auth(token: String) extends LobbyRequest
  final case object Ping extends LobbyRequest
  final case class Observe(lobbyId: String) extends LobbyRequest
  final case object GetLobbies extends LobbyRequest
  final case class GetLobby(lobbyId: String) extends LobbyRequest
  final case class CreateLobby(name: String) extends LobbyRequest
  final case class JoinLobby(lobbyId: String) extends LobbyRequest
  final case class LeaveLobby(lobbyId: String) extends LobbyRequest
  final case class SetHexMapName(lobbyId: String, hexMapName: String) extends LobbyRequest
  final case class SetNumberOfBans(lobbyId: String, numberOfBans: Int) extends LobbyRequest
  final case class SetNumberOfCharactersPerPlayer(lobbyId: String, charactersPerPlayer: Int) extends LobbyRequest
  final case class SetPickType(lobbyId: String, pickType: PickType) extends LobbyRequest
  final case class SetLobbyName(lobbyId: String, newName: String) extends LobbyRequest
  final case class SetClockConfig(lobbyId: String, newConfig: ClockConfig) extends LobbyRequest
  final case class SetColor(lobbyId: String, newColorName: String) extends LobbyRequest
  final case class StartGame(lobbyId: String) extends LobbyRequest
}
