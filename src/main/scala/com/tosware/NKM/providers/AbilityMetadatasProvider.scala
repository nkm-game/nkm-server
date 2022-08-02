package com.tosware.NKM.providers

import com.tosware.NKM.models.game.AbilityMetadata
import com.tosware.NKM.models.game.abilities.aqua.NaturesBeauty
import com.tosware.NKM.serializers.NKMJsonProtocol

case class AbilityMetadatasProvider() extends NKMJsonProtocol {
  def getAbilityMetadatas: Seq[AbilityMetadata] = Seq(
    NaturesBeauty.metadata,
  )
}
