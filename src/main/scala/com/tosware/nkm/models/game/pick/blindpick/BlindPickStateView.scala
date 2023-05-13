package com.tosware.nkm.models.game.pick.blindpick

import com.tosware.nkm.*

case class BlindPickStateView(
  config: BlindPickConfig,
  characterSelection: Map[PlayerId, Set[CharacterMetadataId]],
  pickPhase: BlindPickPhase,
)
