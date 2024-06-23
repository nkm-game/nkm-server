package com.tosware.nkm.models.lobby.ws

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class LobbyResponse(val value: String) extends StringEnumEntry

object LobbyResponse extends StringEnum[LobbyResponse] {
  val values = findValues
  case object Ping extends LobbyResponse("ping")
  case object Auth extends LobbyResponse("auth")
  case object Observe extends LobbyResponse("observe")
  case object GetLobbies extends LobbyResponse("lobbies")
  case object GetLobby extends LobbyResponse("lobby")
  case object CreateLobby extends LobbyResponse("create_lobby")
  case object JoinLobby extends LobbyResponse("join_lobby")
  case object LeaveLobby extends LobbyResponse("leave_lobby")
  case object SetHexMap extends LobbyResponse("set_hexmap")
  case object SetGameMode extends LobbyResponse("set_game_mode")
  case object SetPickType extends LobbyResponse("set_pick_type")
  case object SetNumberOfBans extends LobbyResponse("set_number_of_bans")
  case object SetNumberOfCharactersPerPlayer extends LobbyResponse("set_number_of_characters")
  case object SetLobbyName extends LobbyResponse("set_lobby_name")
  case object SetClockConfig extends LobbyResponse("set_clock_config")
  case object SetColor extends LobbyResponse("set_color")
  case object StartGame extends LobbyResponse("start_game")

  case object Error extends LobbyResponse("error")
}
