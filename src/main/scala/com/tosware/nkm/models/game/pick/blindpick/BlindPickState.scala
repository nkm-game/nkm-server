package com.tosware.nkm.models.game.pick.blindpick

import com.tosware.nkm.*

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
  def pickingPlayers: Seq[PlayerId] =
    characterSelection.filter(_._2.size != config.numberOfCharactersPerPlayer).keys.toSeq

  def pickPhase: BlindPickPhase =
    if (characterSelection.values.exists(_.isEmpty)) {
      BlindPickPhase.Picking
    } else BlindPickPhase.Finished

  def pickNumber: Int = pickPhase match {
    case BlindPickPhase.Picking  => 0
    case BlindPickPhase.Finished => 1
  }

  def pickChecks(playerId: PlayerId, characters: Set[CharacterMetadataId]): Set[UseCheck] =
    Set(
      characters.subsetOf(
        config.availableCharacters
      ) ->
        s"""Some characters you want to pick are not available:
           | ${(characters -- config.availableCharacters).mkString(", ")}""".stripMargin,
      characterSelection.get(playerId).fold(true)(_.isEmpty) -> "You have already picked.",
      (characters.size == config.numberOfCharactersPerPlayer) ->
        s"You need to pick ${config.numberOfCharactersPerPlayer} characters.",
    )

  def pick(playerId: PlayerId, characters: Set[CharacterMetadataId]): BlindPickState =
    copy(characterSelection = characterSelection.updated(playerId, characters))

  def toView(forPlayerOpt: Option[PlayerId]): BlindPickStateView = {
    val filteredCharacterSelection =
      if (pickPhase == BlindPickPhase.Finished)
        characterSelection
      else forPlayerOpt match {
        case Some(forPlayer) =>
          // show only characters from player watching
          config.playersPicking.map(x => x -> Set[CharacterMetadataId]()).toMap
            .updated(forPlayer, characterSelection(forPlayer))
        case None =>
          Map.empty[PlayerId, Set[CharacterMetadataId]]
      }

    BlindPickStateView(config, filteredCharacterSelection, pickPhase)
  }
}
