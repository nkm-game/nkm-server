package com.tosware.NKM.models.game.draftpick

import com.tosware.NKM.models.game.NKMCharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId

object DraftPickState {
  def empty(config: DraftPickConfig): DraftPickState = DraftPickState(
    config,
    config.playersPicking.map(x => x -> None).toMap,
    config.playersPicking.map(x => x -> Seq.empty).toMap,
  )
}

case class DraftPickState(config: DraftPickConfig,
                          bans: Map[PlayerId, Option[Set[CharacterMetadataId]]],
                          characterSelection: Map[PlayerId, Seq[CharacterMetadataId]],
                         ) {
  def bannedCharacters: Set[CharacterMetadataId] = bans.values.flatten.flatten.toSet

  def pickedCharacters: Set[CharacterMetadataId] = characterSelection.values.flatten.toSet

  def charactersAvailableToPick: Set[CharacterMetadataId] = config.availableCharacters -- bannedCharacters -- pickedCharacters

  def currentPlayerPicking: Option[PlayerId] = {
    if (pickPhase != DraftPickPhase.Picking) return None

    // round robin picking, for example: (4 players, 3 characters per player:)
    // 0 1 2 3
    // 3 2 1 0
    // 0 1 2 3
    val isInversedOrder = pickedCharacters.size % (config.playersPicking.size * 2) >= config.playersPicking.size
    val currentPlayerIndex =
      if (isInversedOrder)
        config.playersPicking.size - (pickedCharacters.size % config.playersPicking.size) - 1
      else
        pickedCharacters.size % config.playersPicking.size

    Some(config.playersPicking(currentPlayerIndex))
  }

  def pickPhase: DraftPickPhase = if (config.numberOfBansPerPlayer > 0 && bans.values.exists(_.isEmpty)) {
    DraftPickPhase.Banning
  } else if (characterSelection.values.exists(_.size != config.numberOfCharactersPerPlayer)) {
    DraftPickPhase.Picking
  } else DraftPickPhase.Finished

  def pickNumber: Int = pickPhase match {
    case DraftPickPhase.Banning => 0
    case DraftPickPhase.Picking => 1 + pickedCharacters.size
    case DraftPickPhase.Finished => 1 + pickedCharacters.size + 1
  }

  def validateBan(playerId: PlayerId, characters: Set[CharacterMetadataId]): Boolean = {
    if (!characters.forall(config.availableCharacters.contains)) return false
    if (bans(playerId).isDefined) return false
    if (characters.size > config.numberOfBansPerPlayer) return false
    true
  }

  def ban(playerId: PlayerId, characters: Set[CharacterMetadataId]): DraftPickState = {
    copy(bans = bans.updated(playerId, Some(characters)))
  }

  def validatePick(playerId: PlayerId, character: CharacterMetadataId): Boolean = {
    if (!charactersAvailableToPick.contains(character)) return false
    if (!currentPlayerPicking.contains(playerId)) return false
    true
  }

  def pick(playerId: PlayerId, character: CharacterMetadataId): DraftPickState = {
    copy(characterSelection = characterSelection.updated(playerId, characterSelection(playerId) :+ character))
  }

  def finishBanning(): DraftPickState = {
    copy(bans = bans.map { case (playerId, None) => playerId -> Some(Set()) })
  }

  def toView(forPlayerOpt: Option[PlayerId]): DraftPickStateView = {
    val bansFiltered =
      if(pickPhase == DraftPickPhase.Banning)
        if(forPlayerOpt.isEmpty)
          Map.empty[PlayerId, Option[Set[CharacterMetadataId]]]
        else // show only bans from player watching
          config.playersPicking.map(x => x -> None).toMap
            .updated(forPlayerOpt.get, bans(forPlayerOpt.get))
      else
        bans
    DraftPickStateView(config, bansFiltered, characterSelection, bannedCharacters, pickedCharacters, charactersAvailableToPick, currentPlayerPicking, pickPhase)
  }
}

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
