package com.tosware.nkm.models.game.pick.draftpick

import com.tosware.nkm.*

case class DraftPickStateView(
    config: DraftPickConfig,
    bans: Map[PlayerId, Option[Set[CharacterMetadataId]]],
    characterSelection: Map[PlayerId, Seq[CharacterMetadataId]],
    bannedCharacters: Set[CharacterMetadataId],
    pickedCharacters: Set[CharacterMetadataId],
    charactersAvailableToPick: Set[CharacterMetadataId],
    currentPlayerPicking: Option[PlayerId],
    pickPhase: DraftPickPhase,
)
