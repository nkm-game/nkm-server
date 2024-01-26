package com.tosware.nkm.models.lobby.ws

import com.tosware.nkm.GameId
import com.tosware.nkm.models.game.ClockConfig
import com.tosware.nkm.models.game.pick.PickType

sealed trait LobbyRequest
object LobbyRequest {
  trait ContainsLobbyId {
    val lobbyId: GameId
  }

  final case class Auth(token: String) extends LobbyRequest
  final case object Ping extends LobbyRequest
  final case class Observe(lobbyId: String) extends LobbyRequest with ContainsLobbyId
  final case object GetLobbies extends LobbyRequest
  final case class GetLobby(lobbyId: String) extends LobbyRequest with ContainsLobbyId
  final case class CreateLobby(name: String) extends LobbyRequest
  final case class JoinLobby(lobbyId: String) extends LobbyRequest with ContainsLobbyId
  final case class LeaveLobby(lobbyId: String) extends LobbyRequest with ContainsLobbyId
  final case class SetHexMapName(lobbyId: String, hexMapName: String) extends LobbyRequest with ContainsLobbyId
  final case class SetNumberOfBans(lobbyId: String, numberOfBans: Int) extends LobbyRequest with ContainsLobbyId
  final case class SetNumberOfCharactersPerPlayer(lobbyId: String, charactersPerPlayer: Int) extends LobbyRequest
      with ContainsLobbyId
  final case class SetPickType(lobbyId: String, pickType: PickType) extends LobbyRequest with ContainsLobbyId
  final case class SetLobbyName(lobbyId: String, newName: String) extends LobbyRequest with ContainsLobbyId
  final case class SetClockConfig(lobbyId: String, newConfig: ClockConfig) extends LobbyRequest with ContainsLobbyId
  final case class SetColor(lobbyId: String, newColorName: String) extends LobbyRequest with ContainsLobbyId
  final case class StartGame(lobbyId: String) extends LobbyRequest with ContainsLobbyId
}
