package com.tosware.nkm.models.lobby.ws

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class LobbyResponseType(val value: String) extends StringEnumEntry

object LobbyResponseType extends StringEnum[LobbyResponseType] {
  val values = findValues
  case object Auth extends LobbyResponseType("auth")
  case object Observe extends LobbyResponseType("observe")
  case object Lobbies extends LobbyResponseType("lobbies")
  case object Lobby extends LobbyResponseType("lobby")
  case object CreateLobby extends LobbyResponseType("create_lobby")
  case object JoinLobby extends LobbyResponseType("join_lobby")
  case object LeaveLobby extends LobbyResponseType("leave_lobby")
  case object SetHexMap extends LobbyResponseType("set_hexmap")
  case object SetPickType extends LobbyResponseType("set_pick_type")
  case object SetNumberOfBans extends LobbyResponseType("set_number_of_bans")
  case object SetNumberOfCharacters extends LobbyResponseType("set_number_of_characters")
  case object SetLobbyName extends LobbyResponseType("set_lobby_name")
  case object SetClockConfig extends LobbyResponseType("set_clock_config")
  case object StartGame extends LobbyResponseType("start_game")

  case object Error extends LobbyResponseType("error")
}
