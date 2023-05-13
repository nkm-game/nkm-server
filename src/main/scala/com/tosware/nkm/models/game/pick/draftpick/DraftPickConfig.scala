package com.tosware.nkm.models.game.pick.draftpick

import com.tosware.nkm.*
import com.tosware.nkm.models.game.GameStartDependencies

object DraftPickConfig {
  def generate(g: GameStartDependencies): DraftPickConfig = {
    DraftPickConfig(
      g.players.map(_.name),
      g.charactersMetadata.map(_.id),
      g.numberOfBansPerPlayer,
      g.numberOfCharactersPerPlayer
    )
  }
}

case class DraftPickConfig(
                            playersPicking: Seq[PlayerId],
                            availableCharacters: Set[CharacterMetadataId],
                            numberOfBansPerPlayer: Int,
                            numberOfCharactersPerPlayer: Int,
                          )
