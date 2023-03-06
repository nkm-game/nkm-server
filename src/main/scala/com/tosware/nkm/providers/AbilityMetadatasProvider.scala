package com.tosware.nkm.providers

import com.tosware.nkm.models.game.abilities._
import com.tosware.nkm.models.game.ability.AbilityMetadata
import akame._
import aqua._
import ayatsuji_ayase._
import blank._
import carmel_wilhelmina._
import crona._
import dekomori_sanae._
import ebisuzawa_kurumi._
import hecate._
import kirito._
import liones_elizabeth._
import llenn._
import monkey_d_luffy._
import nibutani_shinka._
import ochaco_uraraka._
import roronoa_zoro._
import ryuko_matoi._
import satou_kazuma._
import shana._
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

    CrackTheSky.metadata,
    MarkOfTheWind.metadata,
    SamuraisSwiftness.metadata,

    AceInTheHole.metadata,
    Check.metadata,
    Castling.metadata,

    ManipulatorOfObjects.metadata,
    BindingRibbons.metadata,
    TiamatsIntervention.metadata,

    BlackBlood.metadata,
    ScreechAlpha.metadata,
    Infection.metadata,

    WickedEyesServant.metadata,
    MjolnirHammer.metadata,
    MjolnirDestinyImpulse.metadata,

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

    RubberHuman.metadata,
    RubberRubberFruit.metadata,
    GearSecond.metadata,

    Mabinogion.metadata,
    SummerBreeze.metadata,
    FairyOfLove.metadata,

    ZeroGravity.metadata,
    ReducedWeight.metadata,
    SkillRelease.metadata,

    LackOfOrientation.metadata,
    OgreCutter.metadata,
    OneHundredEightPoundPhoenix.metadata,

    ScissorBlade.metadata,
    FiberDecapitation.metadata,
    GodrobeSenketsu.metadata,

    HighLuck.metadata,
    DrainTouch.metadata,
    Steal.metadata,

    WingsOfCrimson.metadata,
    GreatBladeOfCrimson.metadata,
    FinalBattleSecretTechnique.metadata,

    SnipersSight.metadata,
    TacticalEscape.metadata,
    PreciseShot.metadata,
  )
}
