package com.tosware.NKM.models.game.effects

import com.tosware.NKM.models.game.{CharacterEffect, CharacterEffectMetadata}


case class SnareEffect(metadata: CharacterEffectMetadata, cooldown: Int) extends CharacterEffect(metadata, cooldown)
