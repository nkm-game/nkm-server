package com.tosware.NKM.models.game.effects

import com.tosware.NKM.models.game.{CharacterEffect, CharacterEffectMetadata}

case class GroundEffect(metadata: CharacterEffectMetadata, cooldown: Int) extends CharacterEffect(metadata, cooldown)
