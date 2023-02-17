package com.tosware.nkm.models.game.pick.blindpick

import com.tosware.nkm.models.game.character.CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.Player.PlayerId

case class BlindPickStateView(
  config: BlindPickConfig,
  characterSelection: Map[PlayerId, Set[CharacterMetadataId]],
  pickPhase: BlindPickPhase,
)
