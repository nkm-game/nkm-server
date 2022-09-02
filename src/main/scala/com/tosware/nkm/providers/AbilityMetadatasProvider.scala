package com.tosware.nkm.providers

import com.tosware.nkm.models.game.AbilityMetadata
import com.tosware.nkm.models.game.abilities.blank.{AceInTheHole, Castling, Check}
import com.tosware.nkm.models.game.abilities.aqua.{NaturesBeauty, Purification, Resurrection}
import com.tosware.nkm.models.game.abilities.hecate.{Aster, MasterThrone, PowerOfExistence}
import com.tosware.nkm.models.game.abilities.llenn.{GrenadeThrow, PChan, RunItDown}
import com.tosware.nkm.models.game.abilities.roronoa_zoro.{LackOfOrientation, OgreCutter, OneHundredEightPoundPhoenix}
import com.tosware.nkm.models.game.abilities.sinon.{PreciseShot, SnipersSight, TacticalEscape}
import com.tosware.nkm.serializers.NkmJsonProtocol

case class AbilityMetadatasProvider() extends NkmJsonProtocol {
  def getAbilityMetadatas: Seq[AbilityMetadata] = Seq(
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
