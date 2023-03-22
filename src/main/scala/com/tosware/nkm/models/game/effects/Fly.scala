package com.tosware.nkm.models.game.effects

import com.tosware.nkm._
import com.tosware.nkm.models.game.character_effect._

object Fly {
  val metadata: CharacterEffectMetadata =
  CharacterEffectMetadata(
    name = CharacterEffectName.Fly,
    initialEffectType = CharacterEffectType.Positive,
    description = "This character can fly, allowing them to pass walls and enemy characters.",
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

case class Fly(effectId: CharacterEffectId, initialCooldown: Int, metadata: CharacterEffectMetadata = Fly.metadata) extends CharacterEffect(effectId)
