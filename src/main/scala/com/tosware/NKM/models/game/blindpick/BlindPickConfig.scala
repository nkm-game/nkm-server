package com.tosware.NKM.models.game.blindpick

import com.tosware.NKM.models.game.GameStartDependencies
import com.tosware.NKM.models.game.CharacterMetadata.CharacterMetadataId
import com.tosware.NKM.models.game.Player.PlayerId


object BlindPickConfig {
  def generate(g: GameStartDependencies): BlindPickConfig = {
    BlindPickConfig(
      g.players.map(_.name),
      g.charactersMetadata.map(_.id),
      g.numberOfCharactersPerPlayer
    )
  }
}

case class BlindPickConfig(
                            playersPicking: Seq[PlayerId],
                            availableCharacters: Set[CharacterMetadataId],
                            numberOfCharactersPerPlayer: Int,
                          )
