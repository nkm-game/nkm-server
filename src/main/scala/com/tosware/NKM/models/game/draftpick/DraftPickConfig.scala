package com.tosware.NKM.models.game.draftpick

import com.tosware.NKM.models.game.GameStartDependencies
import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId

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
