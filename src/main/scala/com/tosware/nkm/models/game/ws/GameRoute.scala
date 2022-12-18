package com.tosware.nkm.models.game.ws

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class GameRoute(val value: String) extends StringEnumEntry

object GameRoute extends StringEnum[GameRoute] {
  val values = findValues

  // General
  case object Auth extends GameRoute("auth")
  case object Observe extends GameRoute("observe")
  case object GetState extends GameRoute("state")
  case object Pause extends GameRoute("pause")
  case object Surrender extends GameRoute("surrender")

  // Character Select
  case object BanCharacters extends GameRoute("ban_characters")
  case object PickCharacter extends GameRoute("pick_character")
  case object BlindPickCharacters extends GameRoute("blind_pick_characters")

  // Actions
  case object PlaceCharacters extends GameRoute("place_characters")
  case object EndTurn extends GameRoute("end_turn")
  case object Move extends GameRoute("move")
  case object BasicAttack extends GameRoute("basic_attack")
  case object UseAbilityWithoutTarget extends GameRoute("use_ability_without_target")
  case object UseAbilityOnCoordinates extends GameRoute("use_ability_on_coordinates")
  case object UseAbilityOnCharacter extends GameRoute("use_ability_on_character")

  // Chat
  case object SendChatMessage extends GameRoute("send_chat_msg")
  case object ExecuteCommand extends GameRoute("exec")
}


