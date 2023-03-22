package com.tosware.nkm.models.game.character_effect

case class CharacterEffectState
(
  name: CharacterEffectName,
  cooldown: Int,
  variables: Map[String, String] = Map.empty,
)

