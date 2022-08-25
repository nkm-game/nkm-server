package com.tosware.nkm.providers

import com.tosware.nkm.models.game.AbilityMetadata
import com.tosware.nkm.models.game.abilities.aqua.{NaturesBeauty, Purification, Resurrection}
import com.tosware.nkm.models.game.abilities.hecate.{Aster, MasterThrone, PowerOfExistence}
import com.tosware.nkm.models.game.abilities.llenn.PChan
import com.tosware.nkm.models.game.abilities.sinon.{SnipersSight, TacticalEscape}
import com.tosware.nkm.serializers.NkmJsonProtocol

case class AbilityMetadatasProvider() extends NkmJsonProtocol {
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
