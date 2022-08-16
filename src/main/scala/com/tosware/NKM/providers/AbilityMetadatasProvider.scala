package com.tosware.NKM.providers

import com.tosware.NKM.models.game.AbilityMetadata
import com.tosware.NKM.models.game.abilities.aqua.{NaturesBeauty, Purification, Resurrection}
import com.tosware.NKM.models.game.abilities.hecate.{Aster, MasterThrone, PowerOfExistence}
import com.tosware.NKM.models.game.abilities.llenn.PChan
import com.tosware.NKM.models.game.abilities.sinon.{SnipersSight, TacticalEscape}
import com.tosware.NKM.serializers.NKMJsonProtocol

case class AbilityMetadatasProvider() extends NKMJsonProtocol {
  def getAbilityMetadatas: Seq[AbilityMetadata] = Seq(
    NaturesBeauty.metadata,
    Purification.metadata,
    Resurrection.metadata,

    MasterThrone.metadata,
    Aster.metadata,
    PowerOfExistence.metadata,

    SnipersSight.metadata,
    TacticalEscape.metadata,

    PChan.metadata,
  )
}
