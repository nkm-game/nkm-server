package com.tosware.NKM.models.game.ws

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class GameResponseType(val value: String) extends StringEnumEntry

object GameResponseType extends StringEnum[GameResponseType] {
  val values = findValues

  // General
  case object Auth extends GameResponseType("auth")
  case object Observe extends GameResponseType("observe")
  case object State extends GameResponseType("state")
  case object Pause extends GameResponseType("pause")
  case object Surrender extends GameResponseType("surrender")

  // Character Select
  case object BanCharacters extends GameResponseType("ban_characters")
  case object PickCharacter extends GameResponseType("pick_character")
  case object BlindPickCharacters extends GameResponseType("blind_pick_characters")

  // Actions
  case object PlaceCharacters extends GameResponseType("place_characters")
  case object EndTurn extends GameResponseType("end_turn")
  case object Move extends GameResponseType("move")
  case object BasicAttack extends GameResponseType("basic_attack")
  case object UseAbility extends GameResponseType("use_ability")

  // Chat
  case object SendChatMessage extends GameResponseType("send_chat_msg")
  case object ExecuteCommand extends GameResponseType("exec")

  case object Error extends GameResponseType("error")
}


