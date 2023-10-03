package com.tosware.nkm.models.game.character

import com.tosware.nkm.*
import com.tosware.nkm.models.game.hex.HexCoordinates

case class NkmCharacterView(
    id: CharacterId,
    metadataId: CharacterMetadataId,
    state: Option[NkmCharacterStateView],
    ownerId: PlayerId,
    isDead: Boolean,
    canBasicMove: Boolean,
    canBasicAttack: Boolean,
    isOnMap: Boolean,
    basicAttackCellCoords: Set[HexCoordinates],
    basicAttackTargets: Set[HexCoordinates],
)
