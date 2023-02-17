package com.tosware.nkm.models.game.character

import CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates

case class NkmCharacterView
(
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
