package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.NkmCharacter.CharacterId

sealed trait RevealCondition
object RevealCondition {
  final case object BanningPhaseFinished extends RevealCondition
  final case object CharacterPlacingFinished extends RevealCondition
  final case object BlindPickFinished extends RevealCondition
  final case class RelatedCharacterRevealed(characterId: CharacterId) extends RevealCondition
  final case class RelatedTrapRevealed(effectId: String) extends RevealCondition
}

