package com.tosware.NKM

import com.tosware.NKM.models.game.NKMCharacterMetadata
import com.tosware.NKM.serializers.NKMJsonProtocol

case class CharactersMetadataProvider() extends NKMJsonProtocol {
  def getCharactersMetadata: Seq[NKMCharacterMetadata] = 1 to 30 map (i => {
    NKMCharacterMetadata(
      name = s"Bot$i",
      initialHealthPoints = 50,
      initialAttackPoints = 5,
      initialBasicAttackRange = 3,
      initialSpeed = 5,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 10,
      Seq.empty,
    )
  })

}
