package com.tosware.nkm.providers

import com.tosware.nkm.models.game.AbilityMetadata
import com.tosware.nkm.models.game.abilities._
import akame._
import blank._
import aqua._
import hecate._
import llenn._
import roronoa_zoro._
import sinon._
import com.tosware.nkm.serializers.NkmJsonProtocol

case class AbilityMetadatasProvider() extends NkmJsonProtocol {
  def getAbilityMetadatas: Seq[AbilityMetadata] = Seq(
    Murasame.metadata,
    Eliminate.metadata,
    LittleWarHorn.metadata,

    NaturesBeauty.metadata,
    Purification.metadata,
    Resurrection.metadata,

    AceInTheHole.metadata,
    Check.metadata,
    Castling.metadata,

    MasterThrone.metadata,
    Aster.metadata,
    PowerOfExistence.metadata,

    PChan.metadata,
    GrenadeThrow.metadata,
    RunItDown.metadata,

    LackOfOrientation.metadata,
    OgreCutter.metadata,
    OneHundredEightPoundPhoenix.metadata,

    SnipersSight.metadata,
    TacticalEscape.metadata,
    PreciseShot.metadata,
  )
}
