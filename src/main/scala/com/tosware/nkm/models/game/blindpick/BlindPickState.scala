package com.tosware.nkm.models.game.blindpick

import com.tosware.nkm.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.nkm.models.game.Player.PlayerId

object BlindPickState {
  def empty(config: BlindPickConfig): BlindPickState = BlindPickState(
    config,
    config.playersPicking.map(x => x -> Set[CharacterMetadataId]()).toMap,
  )
}

case class BlindPickState(
                           config: BlindPickConfig,
                           characterSelection: Map[PlayerId, Set[CharacterMetadataId]],
                         ) {
  def pickingPlayers: Seq[PlayerId] = characterSelection.filter(_._2.size != config.numberOfCharactersPerPlayer).keys.toSeq

  def pickPhase: BlindPickPhase =
    if (characterSelection.values.exists(_.isEmpty)) {
      BlindPickPhase.Picking
    } else BlindPickPhase.Finished

  def pickNumber: Int = pickPhase match {
    case BlindPickPhase.Picking => 0
    case BlindPickPhase.Finished => 1
  }

  def validatePick(playerId: PlayerId, characters: Set[CharacterMetadataId]): Boolean = {
    if (!characters.subsetOf(config.availableCharacters)) return false
    if (!characterSelection.get(playerId).fold(false)(_.isEmpty)) return false
    if (characters.size != config.numberOfCharactersPerPlayer) return false
    true
  }

  def pick(playerId: PlayerId, characters: Set[CharacterMetadataId]): BlindPickState = {
    copy(characterSelection = characterSelection.updated(playerId, characters))
  }

  def toView(forPlayerOpt: Option[PlayerId]): BlindPickStateView = {
    val filteredCharacterSelection =
      if(pickPhase == BlindPickPhase.Finished)
        characterSelection
      else if(forPlayerOpt.isEmpty)
        Map.empty[PlayerId, Set[CharacterMetadataId]]
      else // show only characters from player watching
        config.playersPicking.map(x => x -> Set[CharacterMetadataId]()).toMap
          .updated(forPlayerOpt.get, characterSelection(forPlayerOpt.get))

    BlindPickStateView(config, filteredCharacterSelection, pickPhase)
  }
}

case class BlindPickStateView(
                           config: BlindPickConfig,
                           characterSelection: Map[PlayerId, Set[CharacterMetadataId]],
                           pickPhase: BlindPickPhase,
                         )
