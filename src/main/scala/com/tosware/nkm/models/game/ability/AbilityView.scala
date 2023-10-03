package com.tosware.nkm.models.game.ability

import com.tosware.nkm.*
import com.tosware.nkm.models.game.hex.HexCoordinates

case class AbilityView(
    id: AbilityId,
    metadataId: AbilityMetadataId,
    parentCharacterId: CharacterId,
    state: AbilityState,
    rangeCellCoords: Set[HexCoordinates],
    targetsInRange: Set[HexCoordinates],
    canBeUsed: Boolean,
    canBeUsedFailureMessage: Option[String],
)
