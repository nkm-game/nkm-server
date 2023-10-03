package com.tosware.nkm.models.game.hex_effect

case class HexCellEffectState(
    cooldown: Int,
    variables: Map[String, String] = Map.empty,
)
