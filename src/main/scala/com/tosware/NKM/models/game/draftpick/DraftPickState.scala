package com.tosware.NKM.models.game.draftpick

import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId

object DraftPickState {
  def empty(config: DraftPickConfig): DraftPickState = DraftPickState(
    config,
    config.playersPicking.map(x => x -> Set.empty[CharacterMetadataId]).toMap,
    config.playersPicking.map(x => x -> Seq.empty).toMap,
  )
}

case class DraftPickState(config: DraftPickConfig,
                          bans: Map[PlayerId, Set[CharacterMetadataId]],
                          characterSelection: Map[PlayerId, Seq[CharacterMetadataId]],
                         ) {

  def bannedCharacters: Set[CharacterMetadataId] = bans.values.flatten.toSet

  def pickedCharacters: Set[CharacterMetadataId] = characterSelection.values.flatten.toSet

  def charactersAvailableToPick: Set[CharacterMetadataId] = config.availableCharacters -- bannedCharacters -- pickedCharacters

  def pickPhase: DraftPickPhase = if (bans.values.exists(_.size != config.numberOfBansPerPlayer)) {
    DraftPickPhase.Banning
  } else if (characterSelection.values.exists(_.size != config.numberOfCharactersPerPlayer)) {
    DraftPickPhase.Picking
  } else DraftPickPhase.Finished

  def validateBan(playerId: PlayerId, characters: Set[CharacterMetadataId]): Boolean = {
    if (!characters.forall(charactersAvailableToPick.contains)) return false
    if (bans(playerId) != Set.empty) return false
    if (characters.size != config.numberOfBansPerPlayer) return false
    true
  }

  def ban(playerId: PlayerId, characters: Set[CharacterMetadataId]): DraftPickState = {
    copy(bans = bans.updated(playerId, characters))
  }

  def validatePick(playerId: PlayerId, character: CharacterMetadataId): Boolean = {
    if (!charactersAvailableToPick.contains(character)) return false
    // TODO: validate if it is his turn to pick
    true
  }

  def pick(playerId: PlayerId, character: CharacterMetadataId): DraftPickState = {
    copy(characterSelection = characterSelection.updated(playerId, characterSelection(playerId) :+ character))
  }

}

