package com.tosware.nkm.models.game.ability

case class AbilityState(
    cooldown: Int = 0,
    isEnabled: Boolean = false,
    variables: Map[String, String] = Map.empty, // can be initialized in nkm.conf
)
