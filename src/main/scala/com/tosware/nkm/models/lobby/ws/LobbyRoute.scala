package com.tosware.nkm.models.lobby.ws

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class LobbyRoute(val value: String) extends StringEnumEntry

object LobbyRoute extends StringEnum[LobbyRoute] {
  val values = findValues
  case object Ping extends LobbyRoute("ping")
  case object Auth extends LobbyRoute("auth")
  case object Observe extends LobbyRoute("observe")
  case object GetLobbies extends LobbyRoute("lobbies")
  case object GetLobby extends LobbyRoute("lobby")
  case object CreateLobby extends LobbyRoute("create_lobby")
  case object JoinLobby extends LobbyRoute("join_lobby")
  case object LeaveLobby extends LobbyRoute("leave_lobby")
  case object SetHexMap extends LobbyRoute("set_hexmap")
  case object SetGameMode extends LobbyRoute("set_game_mode")
  case object SetPickType extends LobbyRoute("set_pick_type")
  case object SetNumberOfBans extends LobbyRoute("set_number_of_bans")
  case object SetNumberOfCharactersPerPlayer extends LobbyRoute("set_number_of_characters")
  case object SetLobbyName extends LobbyRoute("set_lobby_name")
  case object SetClockConfig extends LobbyRoute("set_clock_config")
  case object SetColor extends LobbyRoute("set_color")
  case object StartGame extends LobbyRoute("start_game")
}
