package com.tosware.nkm.providers

import com.tosware.nkm.models.game.abilities.akame._
import com.tosware.nkm.models.game.abilities.aqua._
import com.tosware.nkm.models.game.abilities.ayatsuji_ayase._
import com.tosware.nkm.models.game.abilities.blank._
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina._
import com.tosware.nkm.models.game.abilities.crona._
import com.tosware.nkm.models.game.abilities.dekomori_sanae._
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi._
import com.tosware.nkm.models.game.abilities.hecate._
import com.tosware.nkm.models.game.abilities.kirito._
import com.tosware.nkm.models.game.abilities.liones_elizabeth._
import com.tosware.nkm.models.game.abilities.llenn._
import com.tosware.nkm.models.game.abilities.monkey_d_luffy._
import com.tosware.nkm.models.game.abilities.nibutani_shinka._
import com.tosware.nkm.models.game.abilities.ochaco_uraraka._
import com.tosware.nkm.models.game.abilities.roronoa_zoro._
import com.tosware.nkm.models.game.abilities.ryuko_matoi._
import com.tosware.nkm.models.game.abilities.satou_kazuma._
import com.tosware.nkm.models.game.abilities.shana._
import com.tosware.nkm.models.game.abilities.sinon._
import com.tosware.nkm.models.game.ability.AbilityMetadata
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
