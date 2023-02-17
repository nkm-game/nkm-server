package com.tosware.nkm.models.game.character_effect

case class CharacterEffectState
(
  cooldown: Int,
  variables: Map[String, String] = Map.empty,
)

