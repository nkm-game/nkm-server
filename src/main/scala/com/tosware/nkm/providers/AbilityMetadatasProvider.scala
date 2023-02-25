package com.tosware.nkm.providers

import com.tosware.nkm.models.game.abilities._
import com.tosware.nkm.models.game.ability.AbilityMetadata
import akame._
import aqua._
import blank._
import carmel_wilhelmina._
import ebisuzawa_kurumi._
import crona._
import hecate._
import kirito._
import liones_elizabeth._
import llenn._
import nibutani_shinka._
import roronoa_zoro._
import ryuko_matoi._
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

    ManipulatorOfObjects.metadata,
    BindingRibbons.metadata,
    TiamatsIntervention.metadata,

    BlackBlood.metadata,
    ScreechAlpha.metadata,
    Infection.metadata,

    Contact.metadata,
    Survivor.metadata,
    FinalSolution.metadata,

    MasterThrone.metadata,
    Aster.metadata,
    PowerOfExistence.metadata,

    Parry.metadata,
    Switch.metadata,
    StarburstStream.metadata,

    ImmenseHealingPowers.metadata,
    Invigorate.metadata,
    PowerOfTheGoddess.metadata,

    PChan.metadata,
    GrenadeThrow.metadata,
    RunItDown.metadata,

    Mabinogion.metadata,
    SummerBreeze.metadata,
    FairyOfLove.metadata,

    LackOfOrientation.metadata,
    OgreCutter.metadata,
    OneHundredEightPoundPhoenix.metadata,

    ScissorBlade.metadata,
    FiberDecapitation.metadata,
    GodrobeSenketsu.metadata,

    SnipersSight.metadata,
    TacticalEscape.metadata,
    PreciseShot.metadata,
  )
}
