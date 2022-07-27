package com.tosware.NKM.providers

import com.tosware.NKM.models.game.NKMCharacterMetadata
import com.tosware.NKM.serializers.NKMJsonProtocol

case class CharacterMetadatasProvider() extends NKMJsonProtocol {
  def getCharacterMetadatas: Seq[NKMCharacterMetadata] = 1 to 30 map (i => {
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
