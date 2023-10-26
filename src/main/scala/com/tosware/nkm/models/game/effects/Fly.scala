package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character_effect.*

object Fly {
  val metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.Fly,
      initialEffectType = CharacterEffectType.Positive,
      description = "You can fly over walls and enemy characters.",
    )
}

object ZeroGravity {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.ZeroGravity,
      initialEffectType = CharacterEffectType.Positive,
      description =
        s"""Zero Gravity.
           |${Fly.metadata.description}""".stripMargin,
    )
}

case class Fly(effectId: CharacterEffectId, initialCooldown: Int, metadata: CharacterEffectMetadata = Fly.metadata)
    extends CharacterEffect(effectId)
